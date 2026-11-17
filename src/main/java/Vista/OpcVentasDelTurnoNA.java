
package Vista;
//import Modelo.CashInBoxDAO;
import Controlador.TurnoController;
import Modelo.Conexion;
import Modelo.TurnoModel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import Modelo.CashInBoxDAO;

public class OpcVentasDelTurnoNA extends javax.swing.JFrame {
     public static OpcVentasDelTurnoNA instance;
     protected JPanel panelSuperior;
    protected JLabel lblVentasDia;
    protected JLabel lblEfectivo;
    protected JLabel lblTarjeta;

    public OpcVentasDelTurnoNA() {
        instance = this;  //para que se apegue a solo abrir una sola ventana si ya esta abierta
        initComponents();     // 🧱 Crea los paneles y etiquetas
        refrescarDatos();     // 📊 Carga los datos reales al arrancar
    }
public void cargarVentasDelTurno() {
    double ventasTotal = 0.0;
    double ventasEfectivo = 0.0;
    double ventasTarjeta = 0.0;

    try (Connection con = new Conexion().getConnection()) {
        TurnoModel turno = TurnoController.getTurnoGlobal();
        if (turno == null) {
            lblVentasDia.setText("No hay turno activo.");
            lblEfectivo.setText("");
            lblTarjeta.setText("");
            return;
        }

        int turnoId = turno.getId();

        // Total ventas
    
try (PreparedStatement ps = con.prepareStatement(
        "SELECT SUM(total) AS total_ventas FROM ventas WHERE id_turno = ? AND cancelada = 0")) {
    ps.setInt(1, turnoId);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            ventasTotal = rs.getDouble("total_ventas");
        }
    }
}

// Ventas en efectivo
try (PreparedStatement ps = con.prepareStatement(
        "SELECT SUM(total) AS total_efectivo FROM ventas WHERE id_turno = ? AND tipopago = 'Efectivo' AND cancelada = 0")) {
    ps.setInt(1, turnoId);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            ventasEfectivo = rs.getDouble("total_efectivo");
        }
    }
}

// Ventas con tarjeta
try (PreparedStatement ps = con.prepareStatement(
        "SELECT SUM(total) AS total_tarjeta FROM ventas WHERE id_turno = ? AND tipopago = 'Tarjeta' AND cancelada = 0")) {
    ps.setInt(1, turnoId);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            ventasTarjeta = rs.getDouble("total_tarjeta");
        }
    }
}
      

        lblEfectivo.setText("Venta en efectivo: $" + String.format("%.2f", ventasEfectivo));
        lblTarjeta.setText("Venta en tarjeta: $" + String.format("%.2f", ventasTarjeta));
        lblVentasDia.setText("Total Ventas del turno: $" + String.format("%.2f", ventasTotal));

    } catch (Exception e) {
        lblVentasDia.setText("Error al consultar ventas del turno");
        lblEfectivo.setText("");
        lblTarjeta.setText("");
        e.printStackTrace();
    }
}

public void refrescarDatos() {
    cargarVentasDelTurno(); // ya no es 'DelDia'
    panelSuperior.revalidate();
    panelSuperior.repaint();
}

 public void initComponents() {
        panelSuperior = new JPanel();
        lblVentasDia = new JLabel();
        lblEfectivo = new JLabel();
        lblTarjeta = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ventas del Turno");

        lblVentasDia.setFont(new Font("Arial", Font.BOLD, 24));
        lblVentasDia.setText("Total Ventas del día: $0.00");

        lblEfectivo.setFont(new Font("Arial", Font.BOLD, 20));
        lblEfectivo.setText("Venta en efectivo: $0.00");

        lblTarjeta.setFont(new Font("Arial", Font.BOLD, 20));
        lblTarjeta.setText("Venta en tarjeta: $0.00");

        GroupLayout layout = new GroupLayout(panelSuperior);
        panelSuperior.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(30)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblVentasDia)
                                .addComponent(lblEfectivo)
                                .addComponent(lblTarjeta))
                        .addGap(30)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(40)
                        .addComponent(lblVentasDia)
                        .addGap(20)
                        .addComponent(lblEfectivo)
                        .addGap(20)
                        .addComponent(lblTarjeta)
                        .addGap(20)
        );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelSuperior, BorderLayout.CENTER);

        pack();
        setSize(500, getHeight()); // ancho personalizado (por ejemplo 500)
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
*/
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(() -> {
        OpcVentasDelTurnoNA ventana = new OpcVentasDelTurnoNA();
        ventana.setVisible(true);
        ventana.refrescarDatos();  // ✅ Asegura que cargue y muestre datos
    });
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
