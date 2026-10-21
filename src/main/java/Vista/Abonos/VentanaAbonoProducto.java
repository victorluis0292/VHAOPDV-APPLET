package Vista.Abonos;

import Modelo.Conexion;
import Modelo.Eventos;
import Vista.ConsultaCreditoCliente;
import Vista.Sistema;
import Vista.ventanaCobrar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class VentanaAbonoProducto extends JDialog {

    private final ConsultaCreditoCliente padre;
    private final int idDetalle;
    private final double precio;
    private final String dni;
    private double abonado = 0.0;

    private final DefaultTableModel modeloHistorial = new DefaultTableModel(
            new String[]{"Fecha", "Monto", "Nota"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JLabel lblAbonado = new JLabel();
    private final JLabel lblRestante = new JLabel();
    private final JProgressBar barra = new JProgressBar(0, 100);
    private JButton btnNuevoAbono;
    private final JTextField txtMonto = new JTextField();
    private final JTextField txtNota = new JTextField();

    public VentanaAbonoProducto(ConsultaCreditoCliente padre, int idDetalle, String nombreProducto,
                                double precio, String dni, String nombreCliente) {
        super(padre, "Abono a producto", true);
        this.padre = padre;
        this.idDetalle = idDetalle;
        this.precio = precio;
        this.dni = dni;

        cargarHistorial();

        double restante = Math.max(0, precio - abonado);
        int porcentaje = precio > 0 ? (int) Math.min(100, Math.round(abonado * 100.0 / precio)) : 0;

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // ---------- ENCABEZADO ----------
        JPanel norte = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        JLabel lblProducto = new JLabel(nombreProducto);
        lblProducto.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel lblCliente = new JLabel("Cliente: " + nombreCliente + " (" + dni + ")");
        lblCliente.setFont(new Font("Arial", Font.PLAIN, 16));
        filaTitulo.add(lblProducto, BorderLayout.WEST);
        filaTitulo.add(lblCliente, BorderLayout.EAST);
        norte.add(filaTitulo);

        JPanel filaMontos = new JPanel(new GridLayout(1, 3, 10, 0));
        JLabel lblPrecio = new JLabel("Precio: " + String.format("%.2f", precio));
        lblPrecio.setFont(new Font("Arial", Font.PLAIN, 20));
        lblAbonado.setText("Abonado: " + String.format("%.2f", abonado));
        lblAbonado.setFont(new Font("Arial", Font.PLAIN, 20));
        lblRestante.setText("RESTANTE: " + String.format("%.2f", restante));
        lblRestante.setFont(new Font("Arial", Font.BOLD, 22));
        lblRestante.setForeground(restante <= 0 ? new Color(0, 130, 60) : new Color(200, 40, 40));
        filaMontos.add(lblPrecio);
        filaMontos.add(lblAbonado);
        filaMontos.add(lblRestante);
        norte.add(filaMontos);

        barra.setValue(porcentaje);
        barra.setStringPainted(true);
        barra.setString(porcentaje + "% pagado");
        barra.setForeground(new Color(0, 153, 76));
        norte.add(barra);

        add(norte, BorderLayout.NORTH);

        // ---------- HISTORIAL ----------
        JTable tabla = new JTable(modeloHistorial);
        tabla.setFont(new Font("Arial", Font.PLAIN, 16));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        tabla.getTableHeader().setBackground(new Color(0, 102, 204));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(250);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Historial de este producto"));
        scroll.setPreferredSize(new Dimension(520, 180));
        add(scroll, BorderLayout.CENTER);

        // ---------- NUEVO ABONO + BOTONES ----------
        JPanel sur = new JPanel();
        sur.setLayout(new BoxLayout(sur, BoxLayout.Y_AXIS));

        JPanel filaAbono = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel lblMonto = new JLabel("Abonar:");
        lblMonto.setFont(new Font("Arial", Font.BOLD, 18));
        txtMonto.setFont(new Font("Arial", Font.PLAIN, 20));
        txtMonto.setPreferredSize(new Dimension(110, 32));
        new Eventos().aplicarSoloDecimal(txtMonto);

        JLabel lblNota = new JLabel("Nota:");
        lblNota.setFont(new Font("Arial", Font.BOLD, 18));
        txtNota.setFont(new Font("Arial", Font.PLAIN, 16));
        txtNota.setPreferredSize(new Dimension(220, 32));

        filaAbono.add(lblMonto);
        filaAbono.add(txtMonto);
        filaAbono.add(lblNota);
        filaAbono.add(txtNota);
        sur.add(filaAbono);

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        btnNuevoAbono = new JButton("Registrar nuevo abono");
        btnNuevoAbono.setBackground(new Color(0, 153, 76));
        btnNuevoAbono.setForeground(Color.WHITE);
        btnNuevoAbono.setFocusPainted(false);
        btnNuevoAbono.setFont(new Font("Tahoma", Font.BOLD, 13));
        btnNuevoAbono.addActionListener(e -> intentarCobro(nombreProducto, restante));

        boolean pagado = restante <= 0;
        btnNuevoAbono.setEnabled(!pagado);
        txtMonto.setEnabled(!pagado);
        txtNota.setEnabled(!pagado);
        txtMonto.addActionListener(e -> intentarCobro(nombreProducto, restante));  // Enter

        filaBotones.add(btnCancelar);
        filaBotones.add(btnNuevoAbono);
        sur.add(filaBotones);

        add(sur, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setMinimumSize(new Dimension(620, 480));   // por si el pack() sale chico
        setLocationRelativeTo(padre);
    }

  private void intentarCobro(String producto, double restante) {

    if (txtNota.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Debes rellenar el campo Nota.");
        txtNota.requestFocus();
        return;
    }

    double monto;
    try {
        monto = Double.parseDouble(txtMonto.getText().trim());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Escribe el monto a abonar.");
        txtMonto.requestFocus();
        return;
    }

    if (monto <= 0) {
        JOptionPane.showMessageDialog(this, "El abono debe ser mayor a cero.");
        return;
    }

    if (monto > restante) {
        JOptionPane.showMessageDialog(this,
                String.format("El abono no puede ser mayor al restante: %.2f", restante));
        return;
    }

    abrirCobro(producto, monto);
}
    // Abre el cobro en modo abono: al terminar registra la venta y el abono
    private void abrirCobro(String producto, double monto) {
        ventanaCobrar cobro = new ventanaCobrar(padre);
        cobro.configurarAbono(producto, dni, Sistema.getIdEmpresaActiva(),
                idDetalle, txtNota.getText().trim(),
                () -> {                 // al terminar el cobro: refrescar y cerrar el detalle
                    padre.listar();
                    dispose();
                });
        cobro.setDetalleAbonoTicket(precio, abonado);
        cobro.setTotal(monto);
        cobro.setLocationRelativeTo(this);
        cobro.setVisible(true);
    }

    // Lee los abonos ligados a este producto (columna id_detalle)
    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        abonado = 0.0;

        String sql = "SELECT fecha, monto, nota FROM abonos_credito "
                   + "WHERE id_detalle = ? AND aplicado = 0 ORDER BY fecha ASC, id ASC";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp f = rs.getTimestamp("fecha");
                    double monto = rs.getDouble("monto");
                    String nota = rs.getString("nota");
                    abonado += monto;
                    modeloHistorial.addRow(new Object[]{
                        f != null ? f.toLocalDateTime().format(fmt) : "",
                        String.format("%.2f", monto),
                        nota == null ? "" : nota
                    });
                }
            }
        } catch (SQLException e) {
            // Si aún no existe la columna id_detalle, el historial queda vacío
            System.out.println("[VentanaAbonoProducto] No se pudo leer el historial: " + e.getMessage());
        }
    }
}