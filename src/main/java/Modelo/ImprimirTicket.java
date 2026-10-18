package Modelo;

import javax.print.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ImprimirTicket {

    private static SimpleDateFormat getFechaFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        return sdf;
    }

    private static int obtenerFolio(int idVenta) {
        int folio = 0;
        try (Connection con = Conexion.getConnection()) {
            String sql = "SELECT folio FROM ventas WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        folio = rs.getInt("folio");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[TICKET] Error al obtener folio: " + e.getMessage());
        }
        return folio;
    }

    // Helper para obtener datos de la empresa ligada a la venta
    private static String[] obtenerDatosEmpresa(Connection con, int idVenta) {
        String[] datos = new String[]{"Mi Negocio", "Mi Dirección", "Mi Teléfono"};
        String sqlEmpresa = "SELECT e.nombre, e.direccion, e.telefono " +
                            "FROM empresa e JOIN ventas v ON v.id_empresa = e.id_empresa WHERE v.id = ?";
        try (PreparedStatement psEmp = con.prepareStatement(sqlEmpresa)) {
            psEmp.setInt(1, idVenta);
            try (ResultSet rs = psEmp.executeQuery()) {
                if (rs.next()) {
                    datos[0] = rs.getString("nombre") != null ? rs.getString("nombre") : datos[0];
                    datos[1] = rs.getString("direccion") != null ? rs.getString("direccion") : datos[1];
                    datos[2] = rs.getString("telefono") != null ? rs.getString("telefono") : datos[2];
                }
            }
        } catch (SQLException e) {
            System.err.println("[TICKET] Error al obtener datos de empresa: " + e.getMessage());
        }
        return datos;
    }

    // NUEVO: agrega al ticket las promociones aplicadas a la venta (si hay)
    private static void agregarPromociones(StringBuilder sb, Connection con, int idVenta) {
        String sql = "SELECT descripcion, descuento FROM venta_promociones WHERE id_venta = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String descripcion = rs.getString("descripcion");
                    double descuento = rs.getDouble("descuento");

                    sb.append(descripcion).append("\n");
                    sb.append(String.format("%-18s %10s\n", "Ahorro:",
                            "-$" + String.format("%.2f", descuento)));
                }
            }
        } catch (SQLException e) {
            // si falla, el ticket sale igual pero sin las líneas de promo
            System.err.println("[TICKET] Error al leer promociones: " + e.getMessage());
        }
    }

    public static void imprimir(String contenidoTicket) {
        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService thermalPrinter = null;

            for (PrintService service : services) {
                if (service.getName().contains("POS-58")) {
                    thermalPrinter = service;
                    break;
                }
            }

            if (thermalPrinter == null) {
                System.out.println("Impresora POS-58 no encontrada.");
                return;
            }

            byte[] bytes = contenidoTicket.getBytes(StandardCharsets.ISO_8859_1);
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);

            DocPrintJob job = thermalPrinter.createPrintJob();
            job.print(doc, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // TICKET EFECTIVO
    // =============================================
    public static String generarTicketEfectivo(int idVenta, double pago, double cambio, String tipoPago, Date fechaVenta) {
        StringBuilder sb = new StringBuilder();
        int folio = obtenerFolio(idVenta);

        try (Connection con = Conexion.getConnection()) {

            String[] datosEmpresa = obtenerDatosEmpresa(con, idVenta);
            String nombreNegocio = datosEmpresa[0];
            String direccion = datosEmpresa[1];
            String telefono = datosEmpresa[2];

            sb.append("     ").append(nombreNegocio).append("\n");
            sb.append(centrarConSaltos(direccion, 32));
            sb.append("Tel: ").append(telefono).append("\n");
            sb.append("------------------------------\n");
            sb.append("Folio: ").append(folio).append("\n");
            sb.append("Fecha: ").append(getFechaFormat().format(fechaVenta)).append("\n");
            String sqlVenta = "SELECT v.total, c.nombre AS cliente " +
                    "FROM ventas v LEFT JOIN clientes c ON v.cliente = c.id WHERE v.id = ?";
            double totalVenta = 0;
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta)) {
                psVenta.setInt(1, idVenta);
                try (ResultSet rsVenta = psVenta.executeQuery()) {
                    if (rsVenta.next()) {
                        String cliente = rsVenta.getString("cliente");
                        sb.append("Cliente: ").append(cliente != null ? cliente : "Público en general").append("\n");
                        totalVenta = rsVenta.getDouble("total");
                    }
                }
            }

            sb.append("------------------------------\n");

            String sqlDetalle = "SELECT p.nombre, d.cantidad, d.precio, pr.nombre AS proveedor " +
                    "FROM detalle d " +
                    "JOIN productos p ON d.id_pro = p.id " +
                    "JOIN proveedor pr ON p.proveedor = pr.id " +
                    "WHERE d.id_venta = ?";
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                psDetalle.setInt(1, idVenta);
                try (ResultSet rsDetalle = psDetalle.executeQuery()) {
                    while (rsDetalle.next()) {
                        String nombreProd = rsDetalle.getString("nombre");
                        double cantidad = rsDetalle.getDouble("cantidad");
                        double precioUnit = rsDetalle.getDouble("precio");
                        String proveedor = rsDetalle.getString("proveedor");
                        double subTotal = cantidad * precioUnit;

                        sb.append(nombreProd).append("\n");

                        if ("verduleria".equalsIgnoreCase(proveedor)) {
                            sb.append(String.format("%.3fkg x$%.2f/kg   $%.2f\n", cantidad, precioUnit, subTotal));
                        } else {
                            sb.append(formatearLineaProducto(cantidad, precioUnit, subTotal));
                        }
                    }
                }
            }

            // NUEVO: promociones aplicadas
            agregarPromociones(sb, con, idVenta);

            sb.append("------------------------------\n");
            sb.append("Tipo: ").append(tipoPago).append("\n");
            sb.append(String.format("%-12s $%8.2f\n", "TOTAL:", totalVenta));
            sb.append(String.format("%-12s $%8.2f\n", "PAGA CON:", pago));
            sb.append(String.format("%-12s $%8.2f\n", "CAMBIO:", cambio));
            sb.append("------------------------------\n");
            sb.append("¡Gracias por su compra!\n");
            sb.append("USAMOS VHAO PUNTO DE VENTAS\n\n\n");

        } catch (SQLException e) {
            e.printStackTrace();
            sb.append("Error al generar ticket.\n");
        }

        return sb.toString();
    }

    // =============================================
    // TICKET TARJETA / MIXTO
    // =============================================
    public static String generarTicketTarjeta(int idVenta, double comision, String tipoPago, double subtotal, Date fechaVenta) {
        StringBuilder sb = new StringBuilder();
        int folio = obtenerFolio(idVenta);

        try (Connection con = Conexion.getConnection()) {

            String[] datosEmpresa = obtenerDatosEmpresa(con, idVenta);
            String nombreNegocio = datosEmpresa[0];
            String direccion = datosEmpresa[1];
            String telefono = datosEmpresa[2];

            sb.append("     ").append(nombreNegocio).append("\n");
            sb.append(centrarConSaltos(direccion, 32));
            sb.append("Telefono: ").append(telefono).append("\n");
            sb.append("----------------------\n");
            sb.append("Folio: ").append(folio).append("\n");
            sb.append("Fecha: ").append(getFechaFormat().format(fechaVenta)).append("\n");

            String sqlVenta = "SELECT v.total, c.nombre AS cliente " +
                    "FROM ventas v LEFT JOIN clientes c ON v.cliente = c.id WHERE v.id = ?";
            double totalVenta = 0;
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta)) {
                psVenta.setInt(1, idVenta);
                try (ResultSet rsVenta = psVenta.executeQuery()) {
                    if (rsVenta.next()) {
                        String cliente = rsVenta.getString("cliente");
                        sb.append("Cliente: ").append(cliente != null ? cliente : "Público en general").append("\n");
                        totalVenta = rsVenta.getDouble("total");
                    }
                }
            }

            sb.append("----------------------\n");

            String sqlDetalle = "SELECT p.nombre, d.cantidad, d.precio " +
                    "FROM detalle d JOIN productos p ON d.id_pro = p.id WHERE d.id_venta = ?";
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                psDetalle.setInt(1, idVenta);
                try (ResultSet rsDetalle = psDetalle.executeQuery()) {
                    while (rsDetalle.next()) {
                        String nombreProd = rsDetalle.getString("nombre");
                        double cantidad = rsDetalle.getDouble("cantidad");
                        double precioUnit = rsDetalle.getDouble("precio");
                        double subTotal = cantidad * precioUnit;

                        sb.append(nombreProd).append("\n");
                        sb.append(formatearLineaProducto(cantidad, precioUnit, subTotal));
                    }
                }
            }

            // NUEVO: promociones aplicadas
            agregarPromociones(sb, con, idVenta);

            sb.append("----------------------\n");
            sb.append("Tipo: ").append(tipoPago).append("\n");
            sb.append(String.format("%-12s $%8.2f\n", "SUBTOTAL:", subtotal));
            sb.append(String.format("%-12s $%8.2f\n", "COMISION:", comision));
            sb.append(String.format("%-12s $%8.2f\n", "TOTAL:", totalVenta));
            sb.append("----------------------\n");
            sb.append("¡Gracias por su compra!\n");
            sb.append("USAMOS VHAO PUNTO DE VENTAS\n\n\n");

        } catch (SQLException e) {
            e.printStackTrace();
            sb.append("Error al generar ticket.\n");
        }

        return sb.toString();
    }

    // =============================================
    // TICKET CRÉDITO (sin promociones)
    // =============================================
    public static String generarTicketCredito(int idVenta, double total, String tipoPago,
                                         List<String[]> productos, List<String[]> abonos,
                                         double pagaCon, double cambio, String cliente, Date fechaVenta) {
        StringBuilder sb = new StringBuilder();
        int folio = obtenerFolio(idVenta);

        String nombreNegocio = "Mi Negocio";
        String direccion = "Mi Dirección";
        String telefono = "Mi Teléfono";

        try (Connection con = Conexion.getConnection()) {
            String[] datosEmpresa = obtenerDatosEmpresa(con, idVenta);
            nombreNegocio = datosEmpresa[0];
            direccion = datosEmpresa[1];
            telefono = datosEmpresa[2];
        } catch (SQLException e) {
            System.err.println("[TICKET] Error al abrir conexion: " + e.getMessage());
        }

        sb.append("     ").append(nombreNegocio).append("\n");
        sb.append(centrarConSaltos(direccion, 32));
        sb.append("Tel: ").append(telefono).append("\n");
        sb.append("------------------------------\n");
        sb.append("Folio: ").append(folio).append("\n");
        sb.append("Fecha: ").append(getFechaFormat().format(fechaVenta)).append("\n");
        sb.append("Cliente: ").append(cliente != null ? cliente : "Público en general").append("\n");
        sb.append("------------------------------\n");

        for (String[] prod : productos) {
            String nombre = prod[0];
            double cant = Double.parseDouble(prod[1]);
            double precio = Double.parseDouble(prod[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        for (String[] ab : abonos) {
            String nombre = ab[0]; // "ABONO REALIZADO"
            double cant = Double.parseDouble(ab[1]);
            double precio = Double.parseDouble(ab[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("%-12s $%8.2f\n", "TOTAL:", total));
        sb.append(String.format("%-12s $%8.2f\n", "PAGA CON:", pagaCon));
        sb.append(String.format("%-12s $%8.2f\n", "CAMBIO:", cambio));
        sb.append("Tipo: ").append(tipoPago).append("\n");
        sb.append("------------------------------\n");
        sb.append("¡Gracias por su compra!\n");
        sb.append("USAMOS VHAO PUNTO DE VENTAS\n\n\n");

        return sb.toString();
    }

    // =============================================
    // TICKET CRÉDITO CON TARJETA (sin promociones)
    // =============================================
    public static String generarTicketCreditoConTarjeta(int idVenta, String cliente, double subtotal,
                                                    double comision, double total,
                                                    List<String[]> productos, List<String[]> abonos, Date fechaVenta) {
        StringBuilder sb = new StringBuilder();
        int folio = obtenerFolio(idVenta);

        String nombreNegocio = "Mi Negocio";
        String direccion = "Mi Dirección";
        String telefono = "Mi Teléfono";

        try (Connection con = Conexion.getConnection()) {
            String[] datosEmpresa = obtenerDatosEmpresa(con, idVenta);
            nombreNegocio = datosEmpresa[0];
            direccion = datosEmpresa[1];
            telefono = datosEmpresa[2];
        } catch (SQLException e) {
            System.err.println("[TICKET] Error al abrir conexion: " + e.getMessage());
        }

        sb.append("     ").append(nombreNegocio).append("\n");
        sb.append(centrarConSaltos(direccion, 32));
        sb.append("Tel: ").append(telefono).append("\n");
        sb.append("------------------------------\n");
        sb.append("Folio: ").append(folio).append("\n");
        sb.append("Fecha: ").append(getFechaFormat().format(fechaVenta)).append("\n");
        sb.append("Cliente: ").append(cliente != null ? cliente : "Público en general").append("\n");
        sb.append("------------------------------\n");

        for (String[] prod : productos) {
            String nombre = prod[0];
            double cant = Double.parseDouble(prod[1]);
            double precio = Double.parseDouble(prod[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        for (String[] ab : abonos) {
            String nombre = ab[0]; // "ABONO REALIZADO"
            double cant = Double.parseDouble(ab[1]);
            double precio = Double.parseDouble(ab[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("%-12s $%.2f\n", "SUBTOTAL:", subtotal));
        sb.append(String.format("%-12s $%.2f\n", "COMISION:", comision));
        sb.append(String.format("%-12s $%.2f\n", "TOTAL:", total));
        sb.append("Tipo: Tarjeta Crédito\n");
        sb.append("------------------------------\n");
        sb.append("¡Gracias por su compra!\n\n");

        return sb.toString();
    }

    // =============================================
    // TICKET CRÉDITO MIXTO (sin promociones)
    // =============================================
    public static String generarTicketCreditoMixto(int idVenta, String cliente, double subtotal,
                                               double comision, double total,
                                               List<String[]> productos, List<String[]> abonos, Date fechaVenta) {
        StringBuilder sb = new StringBuilder();
        int folio = obtenerFolio(idVenta);

        String nombreNegocio = "Mi Negocio";
        String direccion = "Mi Dirección";
        String telefono = "Mi Teléfono";

        try (Connection con = Conexion.getConnection()) {
            String[] datosEmpresa = obtenerDatosEmpresa(con, idVenta);
            nombreNegocio = datosEmpresa[0];
            direccion = datosEmpresa[1];
            telefono = datosEmpresa[2];
        } catch (SQLException e) {
            System.err.println("[TICKET] Error al abrir conexion: " + e.getMessage());
        }

        sb.append("     ").append(nombreNegocio).append("\n");
        sb.append(centrarConSaltos(direccion, 32));
        sb.append("Tel: ").append(telefono).append("\n");
        sb.append("------------------------------\n");
        sb.append("Folio: ").append(folio).append("\n");
        sb.append("Fecha: ").append(getFechaFormat().format(fechaVenta)).append("\n");
        sb.append("Cliente: ").append(cliente != null ? cliente : "Público en general").append("\n");
        sb.append("------------------------------\n");

        for (String[] prod : productos) {
            String nombre = prod[0];
            double cant = Double.parseDouble(prod[1]);
            double precio = Double.parseDouble(prod[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        for (String[] ab : abonos) {
            String nombre = ab[0]; // "ABONO REALIZADO"
            double cant = Double.parseDouble(ab[1]);
            double precio = Double.parseDouble(ab[2]);
            double sub = cant * precio;

            sb.append(nombre).append("\n");
            sb.append(formatearLineaProducto(cant, precio, sub));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("%-12s $%.2f\n", "SUBTOTAL:", subtotal));
        sb.append(String.format("%-12s $%.2f\n", "COMISION:", comision));
        sb.append(String.format("%-12s $%.2f\n", "TOTAL:", total));
        sb.append("Tipo: Crédito Mixto\n");
        sb.append("------------------------------\n");
        sb.append("¡Gracias por su compra!\n\n");

        return sb.toString();
    }

    // =============================================
    // UTILITY
    // =============================================
    private static String centrarConSaltos(String texto, int ancho) {
        StringBuilder sb = new StringBuilder();
        if (texto.length() < ancho) {
            int espacios = (ancho - texto.length()) / 2;
            for (int i = 0; i < espacios; i++) sb.append(" ");
            sb.append(texto).append("\n");
        } else {
            sb.append(texto).append("\n");
        }
        return sb.toString();
    }

    private static String formatearLineaProducto(double cantidad, double precioUnit, double subTotal) {
        String cantidadStr;
        if (cantidad == Math.floor(cantidad)) {
            cantidadStr = String.valueOf((int) cantidad);
        } else {
            cantidadStr = String.format("%.3f", cantidad);
        }
        return String.format("%sx$%.2f   $%.2f\n", cantidadStr, precioUnit, subTotal);
    }
    
    private static String formatearFechaVenta(String fechaHora) {
    if (fechaHora == null || fechaHora.trim().isEmpty()) {
        return "";
    }

    try {
        java.time.LocalDateTime fecha =
                java.time.LocalDateTime.parse(
                        fechaHora.trim(),
                        java.time.format.DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"));

        return fecha.format(
                java.time.format.DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm"));

    } catch (Exception e) {
        System.err.println(
                "[TICKET] Fecha inválida: " + fechaHora);
        return fechaHora;
    }
}
}