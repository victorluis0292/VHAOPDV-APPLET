package Vista;
import Estilos.Estilos;
import com.toedter.calendar.JDateChooser; // estilo calendar

import Modelo.Conexion;
import static Vista.OpcVentasDelTurnoNA.instance;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import Modelo.TurnoModel;
/**
 *
 * @author vic
 */
public class verMovimientosCaja extends javax.swing.JFrame {
      public static verMovimientosCaja instance;
    private static final SimpleDateFormat FECHA_FORMATO = new SimpleDateFormat("yyyy-MM-dd");
    private JButton btnNuevoMovimiento;

    /**
     * Creates new form Corte
     */
  public verMovimientosCaja() {

        initComponents();
                      instance = this;  //para que se apegue a solo abrir una sola ventana si ya esta abierta

        Estilos.estiloTabla(TablaEgresos);
         Estilos.estiloEtiqueta(MontoTotalIngresosV);
          Estilos.estiloEtiqueta(TotalMontoIngresos);
           Estilos.estiloEtiqueta(MontoTotalV);
          Estilos.estiloEtiqueta(TotalMonto);
          Estilos.estiloCalendario(Midate);
        cargarDatos();
        // Listener para recargar datos al cambiar la fecha
        Midate.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                cargarDatos();
            }
        });
    }
private void cargarDatos() {
    DefaultTableModel modelo = new DefaultTableModel();
    modelo.addColumn("Tipo");
    modelo.addColumn("Operacion");
    modelo.addColumn("Monto");
    modelo.addColumn("Concepto");
    modelo.addColumn("Nota");
    modelo.addColumn("Fecha");

    TurnoModel turno = Controlador.TurnoController.getTurnoGlobal();
    if (turno == null) {
        TablaEgresos.setModel(modelo);
        TotalMonto.setText("0.00");
        TotalMontoIngresos.setText("0.00");
        return;
    }

    int idEmpresa = Sistema.getIdEmpresaActiva();

    double totalEgresos = 0;
    double totalIngresos = 0;

    String sql = "SELECT tipo, concepto, nota, monto, gasto, fecha " +
                 "FROM movimientos_caja " +
                 "WHERE id_turno = ? AND id_empresa = ? " +
                 "ORDER BY id DESC";

    try (Connection con = new Conexion().getConnection()) {
        if (con == null) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turno.getId());
            ps.setInt(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String gasto = rs.getString("gasto");
                    if (gasto == null || gasto.trim().isEmpty()) {
                        gasto = "N/A";
                    }
                    String concepto = rs.getString("concepto");
                    String nota = rs.getString("nota");
                    double monto = rs.getDouble("monto");
                    java.sql.Date fecha = rs.getDate("fecha");

                    modelo.addRow(new Object[]{
                        tipo,
                        gasto,
                        String.format("%.2f", monto),
                        concepto,
                        nota,
                        FECHA_FORMATO.format(fecha)
                    });

                    if ("Ingreso".equalsIgnoreCase(tipo)) {
                        totalIngresos += monto;
                    } else if ("Egreso".equalsIgnoreCase(tipo)) {
                        totalEgresos += monto;
                    }
                }
            }
        }

        TablaEgresos.setModel(modelo);
        TotalMonto.setText(String.format("%.2f", totalEgresos));
        TotalMontoIngresos.setText(String.format("%.2f", totalIngresos));

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar datos:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void abrirMovimientosCaja() {
    MovimientosCaja form;

    // Si ya hay una abierta, la reutiliza en vez de abrir otra
    if (MovimientosCaja.instance != null && MovimientosCaja.instance.isDisplayable()) {
        form = MovimientosCaja.instance;
    } else {
        form = new MovimientosCaja();
        new Controlador.MovimientosCajaController(form);
        // Si la cierran, también refresca
        form.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { cargarDatos(); }
        });
    }

    // Al guardar, vuelve a la fecha de hoy y recarga la tabla
    form.onGuardado = () -> {
        Midate.setDate(new java.util.Date());
        cargarDatos();
    };
    form.setVisible(true);
    form.toFront();
}
  
    private void initComponents() {
        jScrollPane1 = new JScrollPane();
        TablaEgresos = new JTable();
       
        Midate = new JDateChooser();
        MontoTotalV = new JLabel();
        TotalMonto = new JLabel();
  MontoTotalIngresosV = new JLabel();
        TotalMontoIngresos = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ver Movimientos de Caja");

        TablaEgresos.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Tipo","Gasto", "Monto", "Concepto", "Nota", "Fecha"}
        ));
        jScrollPane1.setViewportView(TablaEgresos);

        Midate.setDateFormatString("yyyy-MM-dd");
        Midate.setDate(new java.util.Date());

        MontoTotalV.setText("Total Egresos:");
        TotalMonto.setText("0.00");

        MontoTotalIngresosV.setText("Total Ingresos:");
        TotalMontoIngresos.setText("0.00");
        
        btnNuevoMovimiento = new JButton("Nuevo movimiento");
        btnNuevoMovimiento.addActionListener(e -> abrirMovimientosCaja());

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    .addGroup(layout.createSequentialGroup()
        .addGap(20, 20, 20)
        .addComponent(Midate, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
        .addGap(40, 40, 40)
        .addComponent(MontoTotalV)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(TotalMonto, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        .addGap(20, 20, 20)
        .addComponent(MontoTotalIngresosV)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(TotalMontoIngresos, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        .addGap(20, 20, 20)
        .addComponent(btnNuevoMovimiento)
        .addContainerGap(30, Short.MAX_VALUE))
    .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
        .addContainerGap())
);

        layout.setVerticalGroup(
    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    .addGroup(layout.createSequentialGroup()
        .addGap(20, 20, 20)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(Midate, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
            .addComponent(MontoTotalV)
            .addComponent(TotalMonto)
            .addComponent(MontoTotalIngresosV)
            .addComponent(TotalMontoIngresos)
            .addComponent(btnNuevoMovimiento))
        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
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
        TotalMonto = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaEgresos = new javax.swing.JTable();
        Midate = new com.toedter.calendar.JDateChooser();
        MontoTotalIngresosV = new javax.swing.JLabel();
        TotalMontoIngresos = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        MontoTotalV.setText("Monto Total:");

        TotalMonto.setText("TotalPagoProv");

        TablaEgresos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Gastos", "$total"
            }
        ));
        jScrollPane1.setViewportView(TablaEgresos);

        Midate.setDateFormatString("d/MM/yyyy HH:mm");

        MontoTotalIngresosV.setText("Monto Total:");

        TotalMontoIngresos.setText("TotalPagoProv");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(89, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MontoTotalV)
                            .addComponent(MontoTotalIngresosV))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TotalMontoIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TotalMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MontoTotalIngresosV)
                            .addComponent(TotalMontoIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MontoTotalV)
                            .addComponent(TotalMonto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Midate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
*/
    /**
     * @param args the command line arguments
     */
       public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error al cargar Look and Feel: " + e.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> new verMovimientosCaja().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public com.toedter.calendar.JDateChooser Midate;
    private javax.swing.JLabel MontoTotalIngresosV;
    private javax.swing.JLabel MontoTotalV;
    private javax.swing.JTable TablaEgresos;
    private javax.swing.JLabel TotalMonto;
    private javax.swing.JLabel TotalMontoIngresos;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
