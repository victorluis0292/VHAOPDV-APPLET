package Modelo;

import Controlador.TurnoController;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import Vista.Sistema;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class VentaDao {
    private Conexion cn = new Conexion();

    // Obtiene el último ID de venta global
    public int IdVenta() {
        int id = 0;
        String sql = "SELECT MAX(id) FROM ventas";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error en IdVenta: " + e);
        }
        return id;
    }

    // Registra una venta principal
    public int RegistrarVenta(Venta venta) throws Exception {
        if (venta.getIdEmpresa() <= 0) {
            venta.setIdEmpresa(Sistema.getIdEmpresaActiva());
        }
        if (venta.getIdTurno() <= 0 && TurnoController.getTurnoGlobal() != null) {
            venta.setIdTurno(TurnoController.getTurnoGlobal().getId());
        }

        if (venta.getIdEmpresa() <= 0)
            throw new Exception("❌ No se puede registrar la venta: id_empresa inválido.");
        if (venta.getIdTurno() <= 0)
            throw new Exception("❌ No se puede registrar la venta: id_turno inválido.");

        int folio = obtenerFolio(venta.getIdEmpresa());
        venta.setFolio(folio);

        int idGenerado = 0;
        String sql = "INSERT INTO ventas (id_empresa, cliente, vendedor, total, fecha, tipopago, fecha_hora, id_turno, pagacon, cambio, comision, subtotal, folio) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");
        java.sql.Date fechaSQL = new java.sql.Date(formatoEntrada.parse(venta.getFecha()).getTime());

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, venta.getIdEmpresa());
            ps.setInt(2, venta.getCliente());
            ps.setString(3, venta.getVendedor());
            ps.setDouble(4, venta.getTotal());
            ps.setDate(5, fechaSQL);
            ps.setString(6, venta.getTipopago());
            LocalDateTime fechaHoraLocal = LocalDateTime.now(
             ZoneId.of("America/Monterrey")
            );

ps.setObject(7, fechaHoraLocal);
            ps.setInt(8, venta.getIdTurno());
            ps.setDouble(9, venta.getPagaCon());
            ps.setDouble(10, venta.getCambio());
            ps.setDouble(11, venta.getComision());
            ps.setDouble(12, venta.getSubtotal());
            ps.setInt(13, folio);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) idGenerado = rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new Exception("❌ Error al registrar venta: " + e.getMessage(), e);
        }

        if (idGenerado == 0)
            throw new Exception("❌ No se pudo registrar la venta. ID generado es 0.");

        return idGenerado;
    }

    private long obtenerFechaHoraLocal() {
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Monterrey"));
        return cal.getTimeInMillis();
    }

    // Registra detalle de venta
    public int RegistrarDetalle(Detalle detalle) {
        int filas = 0;
        String sql = "INSERT INTO detalle (id_pro, cantidad, precio, id_venta) VALUES (?,?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, detalle.getId_pro());
            ps.setDouble(2, detalle.getCantidad());
            ps.setDouble(3, detalle.getPrecio());
            ps.setInt(4, detalle.getId());
            filas = ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en RegistrarDetalle: " + e);
        }
        return filas;
    }

    // Registra detalle de crédito de cliente
    public int RegistrarDetalleCreditoCliente(Detalle detalle) {
        int filas = 0;
        String sql = "INSERT INTO detalle_creditocliente (id_pro, cantidad, precio, total, id_venta, cliente, nombre, dni, fecha, id_empresa) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, detalle.getId_pro());
            ps.setDouble(2, detalle.getCantidad());
            ps.setDouble(3, detalle.getPrecio());
            ps.setDouble(4, detalle.getTotal());
            ps.setInt(5, detalle.getId());
            ps.setString(6, detalle.getCliente());
            ps.setString(7, detalle.getNombre());
            ps.setInt(8, detalle.getDni());
            ps.setTimestamp(9, new Timestamp(obtenerFechaHoraLocal()));
            ps.setInt(10, Sistema.getIdEmpresaActiva());

            filas = ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en RegistrarDetalleCreditoCliente: " + e);
        }
        return filas;
    }
// Método puente para mantener compatibilidad con CreditoClienteController y CobroService
    public int RegistrarDetalleCreditocliente(Detalle detalle) {
        return RegistrarDetalleCreditoCliente(detalle);
    }
    // Eliminar créditos de cliente
    public boolean eliminarCreditosDelCliente(int dni, int idEmpresa) {
        String sql = "DELETE FROM detalle_creditocliente WHERE dni = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dni);
            ps.setInt(2, idEmpresa);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar créditos: " + e.getMessage());
            return false;
        }
    }

    // Actualiza el stock del producto tras la venta
    public boolean ActualizarStock(double cantidadVendida, int idProducto, int idEmpresaActiva) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, cantidadVendida);
            ps.setInt(2, idProducto);
            ps.setInt(3, idEmpresaActiva);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e);
            return false;
        }
    }

    // Listar ventas por empresa
    public List<Venta> ListarVentas(int idEmpresa) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT c.id AS id_cli, c.nombre, v.* "
                   + "FROM clientes c INNER JOIN ventas v ON c.id = v.cliente "
                   + "WHERE v.id_empresa = ? ORDER BY v.id DESC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setIdEmpresa(rs.getInt("id_empresa"));
                    v.setNombre_cli(rs.getString("nombre"));
                    v.setVendedor(rs.getString("vendedor"));
                    v.setTotal(rs.getDouble("total"));
                    v.setFolio(rs.getInt("folio"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return lista;
    }

    // Listar ventas por empresa y por día específico
    public List<Venta> ListarVentasPorFecha(int idEmpresa, java.sql.Date fecha) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT c.id AS id_cli, c.nombre, v.* "
                   + "FROM clientes c INNER JOIN ventas v ON c.id = v.cliente "
                   + "WHERE v.id_empresa = ? AND DATE(v.fecha_hora) = ? ORDER BY v.id DESC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setDate(2, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setIdEmpresa(rs.getInt("id_empresa"));
                    v.setNombre_cli(rs.getString("nombre"));
                    v.setVendedor(rs.getString("vendedor"));
                    v.setTotal(rs.getDouble("total"));
                    v.setFolio(rs.getInt("folio"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return lista;
    }

    // Buscar venta por ID
    public Venta BuscarVenta(int idVenta) {
        Venta venta = null;
        String sql = "SELECT v.id, v.folio, v.id_empresa, v.cliente, v.vendedor, " +
                     "v.total, v.subtotal, v.pagacon, v.cambio, v.comision, v.fecha, v.tipopago, v.fecha_hora, v.id_turno, " +
                     "c.nombre AS nombre_cliente, e.nombre AS nombre_empresa " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente = c.id " +
                     "LEFT JOIN empresa e ON v.id_empresa = e.id_empresa " +
                     "WHERE v.id = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    venta = new Venta();
                    venta.setId(rs.getInt("id"));
                    venta.setFolio(rs.getInt("folio"));
                    venta.setIdEmpresa(rs.getInt("id_empresa"));
                    venta.setCliente(rs.getInt("cliente"));
                    venta.setVendedor(rs.getString("vendedor"));
                    venta.setTotal(rs.getDouble("total"));
                    venta.setSubtotal(rs.getDouble("subtotal"));
                    venta.setPagaCon(rs.getDouble("pagacon"));
                    venta.setCambio(rs.getDouble("cambio"));
                    venta.setComision(rs.getDouble("comision"));
                    venta.setFecha(rs.getString("fecha"));
                    venta.setTipopago(rs.getString("tipopago"));
                    java.time.LocalDateTime fechaHora =
    rs.getObject("fecha_hora", java.time.LocalDateTime.class);

if (fechaHora != null) {
    venta.setFechaHora(
        fechaHora.format(
            java.time.format.DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
            )
        )
    );
}
                    venta.setIdTurno(rs.getInt("id_turno"));
                    venta.setNombre_cli(rs.getString("nombre_cliente"));
                    venta.setNombreEmpresa(rs.getString("nombre_empresa"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al buscar venta: " + e.getMessage());
        }
        return venta;
    }

    public int obtenerIdClientePorDniEmpresa(int dni, int idEmpresa) {
        String sql = "SELECT id FROM clientes WHERE dni = ? AND id_empresa = ? LIMIT 1";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dni);
            ps.setInt(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo cliente por DNI y empresa: " + e.getMessage());
        }
        return -1;
    }

    public int obtenerFolio(int idEmpresa) {
        int folio = 1;
        String sql = "SELECT MAX(folio) AS max_folio FROM ventas WHERE id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("max_folio") > 0) {
                    folio = rs.getInt("max_folio") + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return folio;
    }

    public boolean ActualizarStockEntrada(int stockNuevo, int idProducto, int idEmpresa) {
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE productos SET stock = ? WHERE id = ? AND id_empresa = ?")) {
            ps.setInt(1, stockNuevo);
            ps.setInt(2, idProducto);
            ps.setInt(3, idEmpresa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int obtenerDniPorIdCliente(int idCliente) {
        int dni = -1;
        String sql = "SELECT dni FROM clientes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dni = rs.getInt("dni");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dni;
    }

    public String obtenerNombreClientePorId(int idCliente) {
        String nombre = "";
        String sql = "SELECT nombre FROM clientes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nombre;
    }

    public boolean eliminarAbonoCreditoPorId(int idAbono) {
        String sql = "DELETE FROM abonos_credito WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAbono);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarProdCreditoPorId(int idDetalle, int idEmpresa) {
        String sql = "DELETE FROM detalle_creditocliente WHERE id = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDetalle);
            ps.setInt(2, idEmpresa);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Venta BuscarVentaPorFolio(int folio, int idEmpresa) {
        Venta v = null;
        String sql = "SELECT v.id, v.folio, v.id_empresa, v.cliente, v.vendedor, " +
                     "v.total, v.subtotal, v.pagacon, v.cambio, v.comision, v.fecha, v.tipopago, v.fecha_hora, v.id_turno, " +
                     "c.nombre AS nombre_cliente, e.nombre AS nombre_empresa " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente = c.id " +
                     "LEFT JOIN empresa e ON v.id_empresa = e.id_empresa " +
                     "WHERE v.folio = ? AND v.id_empresa = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, folio);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setFolio(rs.getInt("folio"));
                    v.setIdEmpresa(rs.getInt("id_empresa"));
                    v.setCliente(rs.getInt("cliente"));
                    v.setVendedor(rs.getString("vendedor"));
                    v.setTotal(rs.getDouble("total"));
                    v.setSubtotal(rs.getDouble("subtotal"));
                    v.setPagaCon(rs.getDouble("pagacon"));
                    v.setCambio(rs.getDouble("cambio"));
                    v.setComision(rs.getDouble("comision"));
                    v.setFecha(rs.getString("fecha"));
                    v.setTipopago(rs.getString("tipopago"));
                    java.time.LocalDateTime fechaHora =
    rs.getObject("fecha_hora", java.time.LocalDateTime.class);

if (fechaHora != null) {
    v.setFechaHora(
        fechaHora.format(
            java.time.format.DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
            )
        )
    );
}
                    v.setIdTurno(rs.getInt("id_turno"));
                    v.setNombre_cli(rs.getString("nombre_cliente"));
                    v.setNombreEmpresa(rs.getString("nombre_empresa"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public List<Object[]> listarDetalleCreditoPorVenta(int idVenta) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS producto, d.cantidad, d.precio " +
                   "FROM detalle d " +
                   "JOIN productos p ON d.id_pro = p.id " +
                   "WHERE d.id_venta = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("producto"),
                        rs.getDouble("cantidad"),
                        rs.getDouble("precio")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void asegurarClienteMostrador() {
        String sqlCheck = "SELECT COUNT(*) FROM clientes WHERE id = 1";
        String sqlInsert = "INSERT INTO clientes (id, dni, nombre, telefono, direccion, id_empresa) "
                         + "VALUES (1, '00000000', 'Cliente Mostrador', '', '', ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement checkPs = con.prepareStatement(sqlCheck);
             ResultSet rs = checkPs.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertPs = con.prepareStatement(sqlInsert)) {
                    insertPs.setInt(1, Sistema.getIdEmpresaActiva());
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Genera PDF de venta
    public void pdfV(int idVenta, String usuario) {
        try (Connection con = cn.getConnection()) {
            Venta venta = BuscarVenta(idVenta);
            if (venta == null) {
                System.err.println("No se encontró la venta con ID=" + idVenta);
                return;
            }

            String nombreEmpresa = "DESCONOCIDA";
            String rucEmpresa = "";
            String telefonoEmpresa = "";
            String direccionEmpresa = "";

            String sqlEmpresa = "SELECT * FROM empresa WHERE id_empresa = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlEmpresa)) {
                ps.setInt(1, venta.getIdEmpresa());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nombreEmpresa = rs.getString("nombre");
                        rucEmpresa = rs.getString("ruc");
                        telefonoEmpresa = rs.getString("telefono");
                        direccionEmpresa = rs.getString("direccion");
                    }
                }
            }

            String fechaDB = venta.getFechaHora();
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String fechaFormateada = fechaDB;
            try {
                fechaFormateada = formatoSalida.format(formatoEntrada.parse(fechaDB));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String url = System.getProperty("user.home") + "/Desktop";
            String nombreArchivo = "venta_" + venta.getFolio() + ".pdf";
            File salida = new File(url + File.separator + nombreArchivo);

            try (FileOutputStream archivo = new FileOutputStream(salida)) {
                Document doc = new Document(PageSize.A4);
                PdfWriter.getInstance(doc, archivo);
                doc.open();

                Font negrita = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                doc.add(new Paragraph("EMPRESA: " + nombreEmpresa, negrita));
                if (!rucEmpresa.isEmpty()) doc.add(new Paragraph("RFC: " + rucEmpresa));
                if (!telefonoEmpresa.isEmpty()) doc.add(new Paragraph("Tel: " + telefonoEmpresa));
                if (!direccionEmpresa.isEmpty()) doc.add(new Paragraph("Dirección: " + direccionEmpresa));
                doc.add(new Paragraph("FOLIO: " + venta.getFolio(), negrita));
                doc.add(new Paragraph("Vendedor: " + usuario));
                doc.add(new Paragraph("Fecha: " + fechaFormateada));
                doc.add(new Paragraph("Tipo pago: " + venta.getTipopago()));
                doc.add(Chunk.NEWLINE);

                PdfPTable tabla = new PdfPTable(4);
                tabla.setWidthPercentage(100);
                tabla.setWidths(new float[]{10f, 50f, 20f, 20f});
                tabla.addCell("Cant.");
                tabla.addCell("Descripción");
                tabla.addCell("P. Unit.");
                tabla.addCell("P. Total");

                NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

                String sqlDetalle = "SELECT d.cantidad, d.precio, p.nombre " +
                                    "FROM detalle d INNER JOIN productos p ON d.id_pro = p.id " +
                                    "WHERE d.id_venta = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            double cantidad = rs.getDouble("cantidad");
                            double precio = rs.getDouble("precio");
                            double subTotal = cantidad * precio;
                            tabla.addCell(String.format("%.3f", cantidad));
                            tabla.addCell(rs.getString("nombre"));
                            tabla.addCell(moneda.format(precio));
                            tabla.addCell(moneda.format(subTotal));
                        }
                    }
                }

                doc.add(tabla);
               //promociones//
                 for (Object[] p : listarPromocionesPorVenta(idVenta)) {
                 doc.add(new Paragraph("Promo P-" + p[0] + ": " + p[1]
                 + "  -" + moneda.format((double) p[3])));
                 }
              //promociones //
                
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph("TOTAL: " + moneda.format(venta.getTotal()), negrita));
                doc.close();
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(salida);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage());
        }
    }

    public boolean marcarDetalleCreditoComoPagado(int idDetalleOriginal, int idVentaPago, int idEmpresa) {
        String sql = "UPDATE detalle_creditocliente SET pagado = 1, id_venta = ? WHERE id = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVentaPago);
            ps.setInt(2, idDetalleOriginal);
            ps.setInt(3, idEmpresa);
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error en marcarDetalleCreditoComoPagado: " + e);
            return false;
        }
    }
    
    // promociones//
    // Guarda la promoción aplicada a una venta (se llama después de RegistrarVenta)
public int RegistrarVentaPromocion(int idVenta, int idPromocion, String descripcion,
                                   int paquetes, double descuento) {
    int filas = 0;
    String sql = "INSERT INTO venta_promociones (id_venta, id_promocion, descripcion, paquetes, descuento) "
               + "VALUES (?, ?, ?, ?, ?)";
    try (Connection con = cn.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idVenta);
        ps.setInt(2, idPromocion);
        ps.setString(3, descripcion);
        ps.setInt(4, paquetes);
        ps.setDouble(5, descuento);
        filas = ps.executeUpdate();
    } catch (SQLException e) {
        System.err.println("Error en RegistrarVentaPromocion: " + e);
    }
    return filas;
}

// Promociones de una venta, para ticket, PDF e historial
public List<Object[]> listarPromocionesPorVenta(int idVenta) {
    List<Object[]> lista = new ArrayList<>();
    String sql = "SELECT id_promocion, descripcion, paquetes, descuento "
               + "FROM venta_promociones WHERE id_venta = ?";
    try (Connection con = cn.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idVenta);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_promocion"),
                    rs.getString("descripcion"),
                    rs.getInt("paquetes"),
                    rs.getDouble("descuento")
                });
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}
}