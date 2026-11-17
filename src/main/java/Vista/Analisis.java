package Vista;

import Modelo.Conexion;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import Modelo.CashInBoxDAO;
import java.math.BigDecimal;
import Estilos.Estilos;
import Controlador.HistorialVentasController;
/**
 * Reporte general con filtrado por empresa (optimizado)
 */
public class Analisis extends JFrame {
    public static Analisis instance;
    private JLabel lblFechaActual, lblVentas, lblIngresos, lblSaldoInicial, lblEgresos, lblGanancia, lblTotal;
    private JLabel valorVentas, valorIngresos, valorSaldoInicial, valorEgresos, valorGanancia, valorTotal;
    private JButton btnHoy, btnSemana, btnMes, btnAnio, btnRango;
    private JDateChooser midate;
    private JComboBox<String> tipoReporteCombo;

    public Analisis() {
        setTitle("Análisis");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        instance = this;
        initComponents();
    }

    private void initComponents() {
        JPanel panelFechas = crearPanelFechas();
        JPanel panelDatos = crearPanelDatos();

        setLayout(new BorderLayout());
        add(panelFechas, BorderLayout.NORTH);
        add(panelDatos, BorderLayout.CENTER);

        cargarDatos();
    }

    private JPanel crearPanelFechas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        panel.setBackground(Color.WHITE);

        lblFechaActual = new JLabel(getFechaActualFormateada(), SwingConstants.LEFT);
        lblFechaActual.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblFechaActual.setForeground(new Color(40, 40, 40));

        JPanel panelBotones = new JPanel(new GridLayout(1, 5, 10, 10));
        panelBotones.setBackground(Color.WHITE);
        btnHoy = crearBoton("Hoy");
        btnSemana = crearBoton("Semana");
        btnMes = crearBoton("Mes");
        btnAnio = crearBoton("Año");
        btnRango = crearBoton("Rango");
        panelBotones.add(btnHoy); panelBotones.add(btnSemana);
        panelBotones.add(btnMes); panelBotones.add(btnAnio); panelBotones.add(btnRango);

        JPanel contenedorCentro = new JPanel();
        contenedorCentro.setLayout(new BoxLayout(contenedorCentro, BoxLayout.Y_AXIS));
        contenedorCentro.setBackground(Color.WHITE);
        JPanel espacio = new JPanel(); espacio.setPreferredSize(new Dimension(1, 10)); espacio.setOpaque(false);
        contenedorCentro.add(espacio);
        contenedorCentro.add(panelBotones);

        panel.add(lblFechaActual, BorderLayout.NORTH);
        panel.add(contenedorCentro, BorderLayout.CENTER);

        midate = new JDateChooser(); midate.setDate(new Date());
        tipoReporteCombo = new JComboBox<>(new String[]{"Día", "Semana", "Mes", "Año"});
        tipoReporteCombo.setSelectedItem("Día");

        // Acciones botones
        btnHoy.addActionListener(e -> setTipoReporte("Día"));
        btnSemana.addActionListener(e -> setTipoReporte("Semana"));
        btnMes.addActionListener(e -> setTipoReporte("Mes"));
        btnAnio.addActionListener(e -> setTipoReporte("Año"));
        btnRango.addActionListener(e -> {
            JFrame padre = (JFrame) SwingUtilities.getWindowAncestor(panelBotones);
            FormRangoFechasDelCorte dialog = new FormRangoFechasDelCorte(padre, (inicio, fin) -> {
                cargarDatosRango(inicio, fin);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                lblFechaActual.setText("Del " + sdf.format(inicio) + " al " + sdf.format(fin));
            });
            dialog.setVisible(true);
        });

        return panel;
    }

    private void setTipoReporte(String tipo) {
        midate.setDate(new Date());
        tipoReporteCombo.setSelectedItem(tipo);
        lblFechaActual.setText(getFechaActualFormateada());
        cargarDatos();
    }

    private JPanel crearPanelDatos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;

        lblVentas = new JLabel("Has Vendido:"); valorVentas = new JLabel("$0.00");
        lblIngresos = new JLabel("Ingreso :"); valorIngresos = new JLabel("$0.00");
        lblSaldoInicial = new JLabel("Saldo Inicial en caja:"); valorSaldoInicial = new JLabel("$0.00");
        lblEgresos = new JLabel("Has Gastado:"); valorEgresos = new JLabel("$0.00");
        lblGanancia = new JLabel("Ganancia:"); valorGanancia = new JLabel("$0.00");
        lblTotal = new JLabel("Total en caja:"); valorTotal = new JLabel("$0.00");

        JButton btnDetalleVentas = new JButton("Detalle...");
        JButton btnDetalleIngresos = new JButton("Detalle...");
        JButton btnDetalleTotal = new JButton("Detalle...");

        Estilos.estiloEtiquetaGreen(lblVentas); Estilos.estiloEtiquetaGreen(valorVentas);
        Estilos.estiloEtiquetaRed(lblEgresos); Estilos.estiloEtiquetaRed(valorEgresos);
        Estilos.estiloEtiquetaBlue(lblGanancia); Estilos.estiloEtiquetaBlue(valorGanancia);

        agregarLinea(panel, gbc, 0, lblVentas, valorVentas, btnDetalleVentas);
        agregarLinea(panel, gbc, 1, lblIngresos, valorIngresos, btnDetalleIngresos);
        agregarLinea(panel, gbc, 2, lblSaldoInicial, valorSaldoInicial, null);
        agregarLinea(panel, gbc, 3, lblEgresos, valorEgresos, null);
        agregarLinea(panel, gbc, 4, lblGanancia, valorGanancia, null);
        agregarLinea(panel, gbc, 5, lblTotal, valorTotal, btnDetalleTotal);

        btnDetalleVentas.addActionListener(e -> new VentasDiaForm().setVisible(true));
        btnDetalleIngresos.addActionListener(e -> new verMovimientosCaja().setVisible(true));
        btnDetalleTotal.addActionListener(e -> new OpcVentasDelTurnoNA().setVisible(true));

        return panel;
    }

   private void cargarDatos() {
    try {
        if (midate.getDate() == null) {
            System.out.println("No se ha seleccionado ninguna fecha.");
            return;
        }

        // Convertir fecha de JDateChooser a LocalDate
        LocalDate fecha = new java.sql.Date(midate.getDate().getTime()).toLocalDate();
        System.out.println("Fecha seleccionada: " + fecha);

        // Tipo de reporte
        String tipo = (String) tipoReporteCombo.getSelectedItem();
        System.out.println("Tipo de reporte: " + tipo);

        // Empresa activa
        int idEmpresa = Sistema.getIdEmpresaActiva();
        System.out.println("ID de la empresa activa: " + idEmpresa);

        // Calcular fechas de inicio y fin según tipo
        Date inicio = null, fin = null;

        switch (tipo) {
            case "Día":
                inicio = fin = midate.getDate();
                break;
            case "Semana":
                WeekFields wf = WeekFields.of(Locale.getDefault());
                LocalDate firstDay = fecha.with(wf.dayOfWeek(), 1);
                LocalDate lastDay = fecha.with(wf.dayOfWeek(), 7);
                inicio = java.sql.Date.valueOf(firstDay);
                fin = java.sql.Date.valueOf(lastDay);
                break;
            case "Mes":
                inicio = java.sql.Date.valueOf(fecha.withDayOfMonth(1));
                fin = java.sql.Date.valueOf(fecha.withDayOfMonth(fecha.lengthOfMonth()));
                break;
            case "Año":
                inicio = java.sql.Date.valueOf(fecha.withDayOfYear(1));
                fin = java.sql.Date.valueOf(fecha.withDayOfYear(fecha.lengthOfYear()));
                break;
            default:
                System.out.println("Tipo de reporte desconocido: " + tipo);
                break;
        }

        System.out.println("Fecha inicio: " + inicio + ", Fecha fin: " + fin);

        // Cargar los datos del rango
        cargarDatosRango(inicio, fin);
        System.out.println("Datos cargados correctamente.");
    } catch (Exception e) {
        System.out.println("ERROR en cargarDatos(): " + e.getMessage());
        e.printStackTrace(); // Muestra la traza completa del error en consola
        JOptionPane.showMessageDialog(this, "Error al calcular fechas o cargar datos: " + e.getMessage());
    }
}

  private void cargarDatosRango(Date inicio, Date fin) {
    int idEmpresa = Sistema.getIdEmpresaActiva();
    CashInBoxDAO cashDao = new CashInBoxDAO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    try {
        String inicioStr = sdf.format(inicio);
        String finStr = sdf.format(fin);

        System.out.println("Cargando datos para la empresa ID: " + idEmpresa);
        System.out.println("Rango de fechas: " + inicioStr + " a " + finStr);

        // Obtener ventas
        double ventas = 0;
        try {
            ventas = obtenerVentasRango(inicioStr, finStr, idEmpresa);
            System.out.println("Ventas obtenidas: " + ventas);
        } catch (Exception e) {
            System.out.println("ERROR al obtener ventas: " + e.getMessage());
            e.printStackTrace();
        }

        // Obtener ingresos
        double ingresos = 0;
        try {
            ingresos = obtenerIngresosRango(inicioStr, finStr, idEmpresa);
            System.out.println("Ingresos obtenidos: " + ingresos);
        } catch (Exception e) {
            System.out.println("ERROR al obtener ingresos: " + e.getMessage());
            e.printStackTrace();
        }

        // Obtener saldo inicial
        double saldoInicial = 0;
        try {
            saldoInicial = cashDao.obtenerSaldoInicialRango(inicioStr, finStr, idEmpresa).doubleValue();
            System.out.println("Saldo inicial obtenido: " + saldoInicial);
        } catch (Exception e) {
            System.out.println("ERROR al obtener saldo inicial: " + e.getMessage());
            e.printStackTrace();
        }

        // Obtener egresos
        double egresos = 0;
        try {
            egresos = obtenerEgresosRango(inicioStr, finStr, idEmpresa);
            System.out.println("Egresos obtenidos: " + egresos);
        } catch (Exception e) {
            System.out.println("ERROR al obtener egresos: " + e.getMessage());
            e.printStackTrace();
        }

        // Obtener ganancia
        double ganancia = 0;
        try {
            ganancia = obtenerGananciaRango(inicioStr, finStr, idEmpresa);
            System.out.println("Ganancia obtenida: " + ganancia);
        } catch (Exception e) {
            System.out.println("ERROR al obtener ganancia: " + e.getMessage());
            e.printStackTrace();
        }

        // Calcular total en caja
        double total = ventas + ingresos + saldoInicial - egresos;
        System.out.println("Total calculado en caja: " + total);

        // Formatear valores en los labels
        valorVentas.setText(String.format("$%,.2f", ventas));
        valorIngresos.setText(String.format("$%,.2f", ingresos));
        valorSaldoInicial.setText(String.format("$%,.2f", saldoInicial));
        valorEgresos.setText(String.format("$%,.2f", egresos));
        valorGanancia.setText(String.format("$%,.2f", ganancia));
        valorTotal.setText(String.format("$%,.2f", total));

        System.out.println("Datos mostrados correctamente en la interfaz.");

    } catch (Exception e) {
        System.out.println("ERROR general en cargarDatosRango(): " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
    }
}

    // ---------------- MÉTODOS GENÉRICOS DE CONSULTA ----------------
  private double consultaSQL(String sql, Object... params) throws SQLException {
    double resultado = 0;
    try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) resultado = rs.getDouble(1);
        }
    }
    System.out.println("[LOG SQL] Consulta: " + sql + " | Params: " + java.util.Arrays.toString(params) + " | Resultado: " + resultado);
    return resultado;
}

    private double obtenerVentasRango(String inicio, String fin, int idEmpresa) throws SQLException {
    return consultaSQL("SELECT COALESCE(SUM(total),0) FROM ventas "
            + "WHERE DATE(fecha) BETWEEN ? AND ? AND id_empresa=? AND cancelada = 0",
            inicio, fin, idEmpresa);
}
    private double obtenerIngresosRango(String inicio, String fin, int idEmpresa) throws SQLException {
        return consultaSQL("SELECT COALESCE(SUM(monto),0) FROM movimientos_caja WHERE DATE(fecha) BETWEEN ? AND ? AND tipo='Ingreso' AND id_empresa=?", inicio, fin, idEmpresa);
    }
    private double obtenerEgresosRango(String inicio, String fin, int idEmpresa) throws SQLException {
        return consultaSQL("SELECT COALESCE(SUM(monto),0) FROM movimientos_caja WHERE DATE(fecha) BETWEEN ? AND ? AND tipo='Egreso' AND id_empresa=?", inicio, fin, idEmpresa);
    }
    private double obtenerGananciaRango(String inicio, String fin, int idEmpresa) throws SQLException {
    double ganancia = 0;
    String sql = "SELECT d.cantidad, d.precio, p.preciocompra FROM detalle d " +
                 "JOIN ventas v ON d.id_venta=v.id " +
                 "JOIN productos p ON d.id_pro=p.id " +
                 "WHERE DATE(v.fecha) BETWEEN ? AND ? AND v.id_empresa=? AND v.cancelada = 0";
    try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, inicio); ps.setString(2, fin); ps.setInt(3, idEmpresa);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ganancia += (rs.getDouble("precio") - rs.getDouble("preciocompra")) * rs.getDouble("cantidad");
            }
        }
    }
    return ganancia;
}

    private void agregarLinea(JPanel panel, GridBagConstraints gbc, int fila, JLabel etiqueta, JLabel valor, JButton boton) {
        gbc.gridy = fila; gbc.gridx = 0; panel.add(etiqueta, gbc);
        gbc.gridx = 1; panel.add(valor, gbc);
        if (boton != null) { gbc.gridx = 2; panel.add(boton, gbc);}
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(30, 144, 255));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private String getFechaActualFormateada() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }
}
