package Vista;

import Modelo.Conexion;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 *
 * @author vic
 */
public class VentasDiaForm extends javax.swing.JFrame {
    //private JLabel emptyStateLabel;
    private JPanel panelEmptyState;

    /**
     * Creates new form Corte
     */
    public VentasDiaForm() {
       initComponentss();
        cargarDatos();
Midate.getDateEditor().getUiComponent().setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 18));

        // Listener para cuando se cambia la fecha en el JDateChooser
        Midate.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                cargarDatos();
            }
        });
    }
private void cargarDatos() {
    // Crear modelo de tabla con columnas
    DefaultTableModel modelo = new DefaultTableModel();
    modelo.addColumn("Proveedor");
    modelo.addColumn("Total");

    // Obtener fecha seleccionada en formato yyyy-MM-dd
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String fechaSeleccionada = (Midate.getDate() != null) ? sdf.format(Midate.getDate()) : sdf.format(new java.util.Date());

    int idEmpresa = Sistema.getIdEmpresaActiva();
    double totalGeneral = 0;

    String sql =
        "SELECT t.Proveedor, SUM(t.monto) AS Total FROM ( " +

        // 1) Venta bruta por línea (precio de lista)
        "  SELECT p.nombre AS Proveedor, d.cantidad * d.precio AS monto " +
        "  FROM detalle d " +
        "  INNER JOIN ventas v ON d.id_venta = v.id " +
        "  INNER JOIN productos pr ON d.id_pro = pr.id " +
        "  INNER JOIN proveedor p ON pr.proveedor = p.id " +
        "  WHERE v.fecha = ? AND v.id_empresa = ? AND v.cancelada = 0 " +

        "  UNION ALL " +

        // 2) Descuento de promos, en negativo, repartido entre las líneas de esa promo
        "  SELECT p.nombre AS Proveedor, " +
        "         -(vp.descuento * (d.cantidad * d.precio) / NULLIF(base.bruto, 0)) AS monto " +
        "  FROM venta_promociones vp " +
        "  INNER JOIN ventas v ON vp.id_venta = v.id " +
        "  INNER JOIN promocion_productos pp ON pp.id_promocion = vp.id_promocion " +
        "  INNER JOIN detalle d ON d.id_venta = vp.id_venta AND d.id_pro = pp.id_producto " +
        "  INNER JOIN ( " +
        "      SELECT vp2.id AS id_vp, SUM(d2.cantidad * d2.precio) AS bruto " +
        "      FROM venta_promociones vp2 " +
        "      INNER JOIN promocion_productos pp2 ON pp2.id_promocion = vp2.id_promocion " +
        "      INNER JOIN detalle d2 ON d2.id_venta = vp2.id_venta AND d2.id_pro = pp2.id_producto " +
        "      GROUP BY vp2.id " +
        "  ) base ON base.id_vp = vp.id " +
        "  INNER JOIN productos pr ON d.id_pro = pr.id " +
        "  INNER JOIN proveedor p ON pr.proveedor = p.id " +
        "  WHERE v.fecha = ? AND v.id_empresa = ? AND v.cancelada = 0 " +
        ") t GROUP BY t.Proveedor";

    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        System.out.println("Fecha usada para la consulta: " + fechaSeleccionada);

        // 4 parámetros: cada mitad del UNION ALL lleva fecha + empresa
        ps.setString(1, fechaSeleccionada);
        ps.setInt(2, idEmpresa);
        ps.setString(3, fechaSeleccionada);
        ps.setInt(4, idEmpresa);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String proveedor = rs.getString("Proveedor");
                double total = rs.getDouble("Total");
                totalGeneral += total;
                modelo.addRow(new Object[]{proveedor, String.format("%.2f", total)});
            }
        }

        if (modelo.getRowCount() == 0) {
            jScrollPane1.setViewportView(panelEmptyState);
            jScrollPane1.revalidate();
            jScrollPane1.repaint();
            TotalVenta.setText("0.00");
        } else {
            TablaVentasProvedor.setModel(modelo);
            jScrollPane1.setViewportView(TablaVentasProvedor);
            TotalVenta.setText(String.format("%.2f", totalGeneral));
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
     @SuppressWarnings("unchecked")
   private void initComponentss() {
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaVentasProvedor = new javax.swing.JTable();
        Midate = new JDateChooser();
        MontoTotalV = new javax.swing.JLabel();
        TotalVenta = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Corte Ventas");

        TablaVentasProvedor.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 18));
        TablaVentasProvedor.setRowHeight(30);
        TablaVentasProvedor.getTableHeader().setFont(new java.awt.Font("Tahoma", Font.BOLD, 18));
        TablaVentasProvedor.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Proveedor", "Total"}
        ));

        jScrollPane1.setViewportView(TablaVentasProvedor);

        Midate.setDateFormatString("yyyy-MM-dd");
        Midate.setDate(new java.util.Date());

        MontoTotalV.setText("Monto Total:");
        MontoTotalV.setFont(new java.awt.Font("Tahoma", Font.BOLD, 18));

        TotalVenta.setText("0.00");
        TotalVenta.setForeground(new Color(51, 51, 255));
        TotalVenta.setFont(new java.awt.Font("Tahoma", Font.BOLD, 24));

        // Crear panel empty state con imagen y texto
        panelEmptyState = new JPanel(new BorderLayout());
        JLabel emptyStateLabel = new JLabel("Sin movimientos por el momento.", SwingConstants.CENTER);
        emptyStateLabel.setFont(new java.awt.Font("Tahoma", Font.BOLD, 20));

        // Cargar imagen desde recursos (debes tener la imagen en /img/grillosindatos.png dentro del classpath)
  URL imgUrl = getClass().getResource("/img/grillosindatos.png");
if (imgUrl == null) {
    System.out.println("No se encontró la imagen en /img/grillosindatos.png");
} else {
    ImageIcon icon = new ImageIcon(imgUrl);
    emptyStateLabel.setIcon(icon);
    emptyStateLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    emptyStateLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
}
        if (imgUrl != null) {
            emptyStateLabel.setIcon(new ImageIcon(imgUrl));
            emptyStateLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            emptyStateLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        } else {
            System.out.println("Imagen no encontrada en /img/grillosindatos.png");
        }
        panelEmptyState.add(emptyStateLabel, BorderLayout.CENTER);

        // Layout principal
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(MontoTotalV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MontoTotalV)
                    .addComponent(TotalVenta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pack();
        setLocationRelativeTo(null);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    /*
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MontoTotalV = new javax.swing.JLabel();
        TotalVenta = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaVentasProvedor = new javax.swing.JTable();
        Midate = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        MontoTotalV.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        MontoTotalV.setText("Monto Total:");

        TotalVenta.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        TotalVenta.setForeground(new java.awt.Color(51, 51, 255));
        TotalVenta.setText("TotalVenta");

        TablaVentasProvedor.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        TablaVentasProvedor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Provedores", "$total"
            }
        ));
        jScrollPane1.setViewportView(TablaVentasProvedor);

        Midate.setDateFormatString("d/MM/yyyy HH:mm");
        Midate.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(84, Short.MAX_VALUE)
                .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(66, 66, 66)
                .addComponent(MontoTotalV)
                .addGap(18, 18, 18)
                .addComponent(TotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
            .addGroup(layout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TotalVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                            .addComponent(MontoTotalV)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
*/
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error al cargar Look and Feel: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> new VentasDiaForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public com.toedter.calendar.JDateChooser Midate;
    private javax.swing.JLabel MontoTotalV;
    public javax.swing.JTable TablaVentasProvedor;
    private javax.swing.JLabel TotalVenta;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
