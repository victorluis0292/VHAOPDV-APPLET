package Vista.Abonos;

import Estilos.Estilos;
import Modelo.Conexion;
import Vista.ConsultaCreditoCliente;
import Vista.Sistema;
import Vista.ventanaCobrar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VentanaAbonoCategoria extends JDialog {

    private static class Prod {
        final int id; final String nombre; final double total, abonado;
        Prod(int id, String nombre, double total, double abonado) {
            this.id = id; this.nombre = nombre; this.total = total; this.abonado = abonado;
        }
        double restante() { return Math.max(0, Math.round((total - abonado) * 100.0) / 100.0); }
    }

    private static class Reparto {
        final int idDetalle; final String producto; final double restanteAntes, aplicado;
        Reparto(int idDetalle, String producto, double restanteAntes, double aplicado) {
            this.idDetalle = idDetalle; this.producto = producto;
            this.restanteAntes = restanteAntes; this.aplicado = aplicado;
        }
    }

    private final ConsultaCreditoCliente padre;
    private final String dni, nombreCliente, categoria;
    private final int idEmpresa;
    private final List<Prod> productos = new ArrayList<>();
    private double fiadoCat = 0, abonadoCat = 0;

    private JLabel lblFiado, lblAbonado, lblRestante, lblPct, lblReparto;
    private JProgressBar barra;
    private DefaultTableModel modeloProd, modeloHist;
    private JTextField txtMonto, txtNota;

    public VentanaAbonoCategoria(ConsultaCreditoCliente padre, JDialog owner, String dni,
                                 int idEmpresa, String nombreCliente, String categoria) {
        super(owner, "Abono a categoría", true);
        this.padre = padre;
        this.dni = dni;
        this.idEmpresa = idEmpresa;
        this.nombreCliente = nombreCliente;
        this.categoria = categoria;

        setSize(720, 660);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 16, 12, 16));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        cargar();
        SwingUtilities.invokeLater(() -> txtMonto.requestFocusInWindow());
    }

    // ---------- UI ----------
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        JPanel fila1 = new JPanel(new BorderLayout());
        JLabel t = new JLabel(categoria);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel c = new JLabel("Cliente: " + nombreCliente + " (" + dni + ")");
        c.setForeground(Color.DARK_GRAY);
        fila1.add(t, BorderLayout.WEST);
        fila1.add(c, BorderLayout.EAST);

        JPanel fila2 = new JPanel(new GridLayout(1, 3));
        lblFiado = new JLabel("Fiado: 0.00");
        lblAbonado = new JLabel("Abonado: 0.00", SwingConstants.CENTER);
        lblRestante = new JLabel("RESTANTE: 0.00", SwingConstants.RIGHT);
        lblRestante.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblRestante.setForeground(new Color(200, 0, 0));
        fila2.add(lblFiado);
        fila2.add(lblAbonado);
        fila2.add(lblRestante);

        barra = new JProgressBar(0, 100);
        barra.setForeground(new Color(10, 143, 60));
        lblPct = new JLabel("0% pagado", SwingConstants.CENTER);
        lblPct.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(fila1);
        p.add(fila2);
        p.add(barra);
        p.add(lblPct);
        p.setBorder(new EmptyBorder(0, 0, 8, 0));
        return p;
    }

    private JPanel crearCentro() {
        modeloProd = new DefaultTableModel(
                new String[]{"Producto", "Precio", "Abonado", "Restante", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tProd = new JTable(modeloProd);
        Estilos.estiloTablas(tProd);

        modeloHist = new DefaultTableModel(
                new String[]{"Fecha", "Monto", "Producto", "Nota"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tHist = new JTable(modeloHist);
        Estilos.estiloTablas(tHist);

        JPanel p = new JPanel(new GridLayout(2, 1, 0, 8));
        p.add(conTitulo("Productos incluidos en esta categoría", new JScrollPane(tProd)));
        p.add(conTitulo("Historial de abonos de la categoría", new JScrollPane(tHist)));
        return p;
    }

    private JPanel conTitulo(String titulo, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPie() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 8));
        p.setBorder(new EmptyBorder(10, 0, 0, 0));

        txtMonto = new JTextField(10);
        txtNota = new JTextField(20);
        soloNumeros(txtMonto);
        txtMonto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarReparto(); }
            public void removeUpdate(DocumentEvent e) { actualizarReparto(); }
            public void changedUpdate(DocumentEvent e) { actualizarReparto(); }
        });

        JPanel campos = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        campos.add(new JLabel("Abonar:"));
        campos.add(txtMonto);
        campos.add(new JLabel("Nota:"));
        campos.add(txtNota);

        lblReparto = new JLabel(" ");
        lblReparto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblReparto.setOpaque(true);
        lblReparto.setBackground(new Color(240, 243, 247));
        lblReparto.setBorder(new EmptyBorder(6, 10, 6, 10));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        JButton btnRegistrar = new JButton("Registrar abono a categoría");
        btnRegistrar.setBackground(new Color(0, 153, 76));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> intentarCobro());
        txtMonto.addActionListener(e -> intentarCobro());   // Enter, como en el abono por producto

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botones.add(btnCancelar);
        botones.add(btnRegistrar);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        p.add(campos);
        p.add(lblReparto);
        p.add(botones);
        return p;
    }

    private void soloNumeros(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String s, AttributeSet a)
                    throws BadLocationException {
                replace(fb, off, 0, s, a);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String t, AttributeSet a)
                    throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, off) + (t == null ? "" : t) + actual.substring(off + len);
                if (nuevo.matches("\\d*(\\.\\d{0,2})?")) super.replace(fb, off, len, t, a);
            }
        });
    }

    // ---------- Datos ----------
    private void cargar() {
        productos.clear();
        modeloProd.setRowCount(0);
        modeloHist.setRowCount(0);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

        String sqlProd =
            "SELECT d.id, d.nombre, d.total, IFNULL(ab.abonado, 0) AS abonado, d.fecha " +
            "FROM detalle_creditocliente d JOIN productos p ON p.id = d.id_pro " +
            "LEFT JOIN (SELECT id_detalle, SUM(monto) AS abonado FROM abonos_credito " +
            "           WHERE aplicado = 0 GROUP BY id_detalle) ab ON ab.id_detalle = d.id " +
            "WHERE d.dni = ? AND d.id_empresa = ? AND p.categoria = ? " +
            "ORDER BY d.fecha ASC, d.id ASC";

        String sqlHist =
            "SELECT a.fecha, a.monto, d.nombre, a.nota FROM abonos_credito a " +
            "JOIN detalle_creditocliente d ON d.id = a.id_detalle " +
            "JOIN productos p ON p.id = d.id_pro " +
            "WHERE a.dni = ? AND a.id_empresa = ? AND a.aplicado = 0 AND p.categoria = ? " +
            "ORDER BY a.fecha DESC, a.id DESC";

        double fiado = 0, abonado = 0;
        try (Connection con = Conexion.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlProd)) {
                ps.setString(1, dni);
                ps.setInt(2, idEmpresa);
                ps.setString(3, categoria);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Prod pr = new Prod(rs.getInt("id"), rs.getString("nombre"),
                                rs.getDouble("total"), rs.getDouble("abonado"));
                        productos.add(pr);
                        fiado += pr.total;
                        abonado += pr.abonado;
                        Timestamp f = rs.getTimestamp("fecha");
                        modeloProd.addRow(new Object[]{pr.nombre,
                            String.format("%.2f", pr.total), String.format("%.2f", pr.abonado),
                            String.format("%.2f", pr.restante()), f == null ? "" : fmt.format(f)});
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlHist)) {
                ps.setString(1, dni);
                ps.setInt(2, idEmpresa);
                ps.setString(3, categoria);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp f = rs.getTimestamp("fecha");
                        modeloHist.addRow(new Object[]{f == null ? "" : fmt.format(f),
                            String.format("%.2f", rs.getDouble("monto")),
                            rs.getString("nombre"), rs.getString("nota")});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        fiadoCat = fiado;
        abonadoCat = abonado;
        double restante = Math.max(0, fiado - abonado);
        lblFiado.setText(String.format("Fiado: %.2f", fiado));
        lblAbonado.setText(String.format("Abonado: %.2f", abonado));
        lblRestante.setText(String.format("RESTANTE: %.2f", restante));
        int pct = fiado <= 0 ? 0 : (int) Math.min(100, Math.round(abonado * 100.0 / fiado));
        barra.setValue(pct);
        lblPct.setText(pct + "% pagado");
    }

    private double restanteTotal() {
        double s = 0;
        for (Prod p : productos) s += p.restante();
        return Math.round(s * 100.0) / 100.0;
    }

    // ---------- Reparto: más antiguo primero ----------
    private List<Reparto> calcularReparto(double monto) {
        List<Reparto> lista = new ArrayList<>();
        double porRepartir = monto;
        for (Prod p : productos) {                    // ya vienen ordenados por fecha, id
            if (porRepartir <= 0.0049) break;
            double rest = p.restante();
            if (rest <= 0) continue;
            double aplica = Math.round(Math.min(rest, porRepartir) * 100.0) / 100.0;
            lista.add(new Reparto(p.id, p.nombre, rest, aplica));
            porRepartir -= aplica;
        }
        return lista;
    }

    private double leerMonto() {
        try {
            return Double.parseDouble(txtMonto.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void actualizarReparto() {
        double monto = leerMonto();
        if (monto <= 0) {
            lblReparto.setText(" ");
            return;
        }
        if (monto > restanteTotal()) {
            lblReparto.setForeground(new Color(200, 0, 0));
            lblReparto.setText(String.format("El abono pasa del restante de la categoría (%.2f).", restanteTotal()));
            return;
        }
        StringBuilder sb = new StringBuilder("<html>Se aplica primero al más antiguo: ");
        List<Reparto> rep = calcularReparto(monto);
        for (int i = 0; i < rep.size(); i++) {
            Reparto r = rep.get(i);
            double queda = Math.round((r.restanteAntes - r.aplicado) * 100.0) / 100.0;
            sb.append(r.producto).append(String.format(" %.2f", r.aplicado))
              .append(queda <= 0 ? " (liquidado)" : String.format(" (queda %.2f)", queda));
            if (i < rep.size() - 1) sb.append(", ");
        }
        lblReparto.setForeground(Color.DARK_GRAY);
        lblReparto.setText(sb.append("</html>").toString());
    }

    // ---------- Cobro (mismo flujo que el abono por producto) ----------
    private void intentarCobro() {
        cargar();   // datos frescos antes de repartir

        if (txtNota.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes rellenar el campo Nota.");
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
        if (monto > restanteTotal()) {
            JOptionPane.showMessageDialog(this,
                    String.format("El abono no puede ser mayor al restante: %.2f", restanteTotal()));
            return;
        }

        abrirCobro(monto, calcularReparto(monto));
    }

    private void abrirCobro(double monto, List<Reparto> reparto) {
        List<double[]> partes = new ArrayList<>();   // {idDetalle, montoAplicado}
        for (Reparto r : reparto) partes.add(new double[]{r.idDetalle, r.aplicado});

        ventanaCobrar cobro = new ventanaCobrar(padre);
        cobro.configurarAbonoCategoria(categoria, dni, Sistema.getIdEmpresaActiva(),
                partes, txtNota.getText().trim(),
                () -> {                 // al terminar el cobro: refrescar y cerrar
                    padre.listar();
                    dispose();
                });
        cobro.setDetalleAbonoTicket(fiadoCat, abonadoCat);
        cobro.setTotal(monto);
        cobro.setLocationRelativeTo(this);
        cobro.setVisible(true);
    }
}