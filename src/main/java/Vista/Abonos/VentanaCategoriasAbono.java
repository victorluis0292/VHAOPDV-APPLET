package Vista.Abonos;

import Estilos.Estilos;
import Modelo.Conexion;
import Vista.ConsultaCreditoCliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentanaCategoriasAbono extends JDialog {

    private final ConsultaCreditoCliente padre;
    private final String dni;
    private final int idEmpresa;
    private final String nombreCliente;
    private final Runnable alTerminar;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Categoría", "Productos", "Total fiado", "Abonado", "Restante"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<String> categorias = new ArrayList<>();

    public VentanaCategoriasAbono(ConsultaCreditoCliente padre, String dni, int idEmpresa,
                                  String nombreCliente, Runnable alTerminar) {
        super(padre, "Abonar a capital - " + nombreCliente, true);
        this.padre = padre;
        this.dni = dni;
        this.idEmpresa = idEmpresa;
        this.nombreCliente = nombreCliente;
        this.alTerminar = alTerminar;

        setSize(620, 340);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new JLabel("Elige la categoría a la que se va a abonar (Enter o doble clic)."),
                BorderLayout.NORTH);

        Estilos.estiloTablas(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(26);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btn = new JButton("Abonar a esta categoría");
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> abrirCategoria());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.add(btn);
        add(sur, BorderLayout.SOUTH);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) abrirCategoria();
            }
        });
        tabla.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrir");
        tabla.getActionMap().put("abrir", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirCategoria();
            }
        });

        cargar();
        if (modelo.getRowCount() > 0) tabla.setRowSelectionInterval(0, 0);
        SwingUtilities.invokeLater(tabla::requestFocusInWindow);
    }

    private void cargar() {
        modelo.setRowCount(0);
        categorias.clear();
        String sql =
            "SELECT p.categoria AS categoria, " +
            "COUNT(*) AS productos, SUM(d.total) AS total, SUM(IFNULL(ab.abonado, 0)) AS abonado " +
            "FROM detalle_creditocliente d " +
            "JOIN productos p ON p.id = d.id_pro " +
            "LEFT JOIN (SELECT id_detalle, SUM(monto) AS abonado FROM abonos_credito " +
            "           WHERE aplicado = 0 GROUP BY id_detalle) ab ON ab.id_detalle = d.id " +
            "WHERE d.dni = ? AND d.id_empresa = ? " +
            "GROUP BY p.categoria ORDER BY p.categoria";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double total = rs.getDouble("total");
                    double abonado = rs.getDouble("abonado");
                    categorias.add(rs.getString("categoria"));
                    modelo.addRow(new Object[]{
                        rs.getString("categoria"),
                        rs.getInt("productos"),
                        String.format("%.2f", total),
                        String.format("%.2f", abonado),
                        String.format("%.2f", Math.max(0, total - abonado))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void abrirCategoria() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        double restante = Double.parseDouble(modelo.getValueAt(fila, 4).toString().replace(",", "."));
        if (restante <= 0) {
            JOptionPane.showMessageDialog(this, "Esa categoría ya está liquidada.");
            return;
        }
        String categoria = categorias.get(fila);

        new VentanaAbonoCategoria(padre, this, dni, idEmpresa, nombreCliente, categoria).setVisible(true);

        cargar();
        if (alTerminar != null) alTerminar.run();
    }
}