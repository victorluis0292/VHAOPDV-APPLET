package Vista;

import Controlador.TurnoController;
import Modelo.CashInBoxDAO;
import Modelo.Conexion;
import Modelo.TurnoModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class OpcVentasDelTurno extends javax.swing.JFrame {
    public static OpcVentasDelTurno instance;
    protected JPanel panelSuperior;
    protected JLabel lblVentasDia, lblEfectivo, lblTarjeta;
    protected JLabel lblHasVendido, lblIngreso, lblSaldoInicial, lblHasGastado, lblGanancia, lblTotalCaja;

    // Columna del turno en movimientos_caja (cámbiala si en tu base se llama distinto)
    private static final String COL_TURNO_MOV = "id_turno";

    public OpcVentasDelTurno() {
        instance = this;
        initComponents();
        refrescarDatos();
    }

    private double sumar(Connection con, String sql, int turnoId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turnoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    private void limpiarResumen() {
        lblEfectivo.setText("");
        lblTarjeta.setText("");
        lblHasVendido.setText("");
        lblIngreso.setText("");
        lblSaldoInicial.setText("");
        lblHasGastado.setText("");
        lblGanancia.setText("");
        lblTotalCaja.setText("");
    }

    public void cargarVentasDelTurno() {
        try (Connection con = new Conexion().getConnection()) {
            TurnoModel turno = TurnoController.getTurnoGlobal();
            if (turno == null) {
                limpiarResumen();
                lblVentasDia.setText("No hay turno activo.");
                return;
            }
            int turnoId = turno.getId();

            double ventasTotal = sumar(con,
                "SELECT COALESCE(SUM(total),0) FROM ventas WHERE id_turno = ? AND cancelada = 0", turnoId);
            double ventasEfectivo = sumar(con,
                "SELECT COALESCE(SUM(total),0) FROM ventas WHERE id_turno = ? AND tipopago = 'Efectivo' AND cancelada = 0", turnoId);
            double ventasTarjeta = sumar(con,
                "SELECT COALESCE(SUM(total),0) FROM ventas WHERE id_turno = ? AND tipopago = 'Tarjeta' AND cancelada = 0", turnoId);

            double ingresos = sumar(con,
                "SELECT COALESCE(SUM(monto),0) FROM movimientos_caja WHERE " + COL_TURNO_MOV + " = ? AND tipo = 'Ingreso'", turnoId);
            double egresos = sumar(con,
                "SELECT COALESCE(SUM(monto),0) FROM movimientos_caja WHERE " + COL_TURNO_MOV + " = ? AND tipo = 'Egreso'", turnoId);

            double ganancia = sumar(con,
                "SELECT COALESCE(SUM((d.precio - p.preciocompra) * d.cantidad),0) " +
                "FROM detalle d JOIN ventas v ON d.id_venta = v.id " +
                "JOIN productos p ON d.id_pro = p.id " +
                "WHERE v.id_turno = ? AND v.cancelada = 0", turnoId);

            // Saldo inicial del turno (se lee de la tabla turnos)
double saldoInicial = sumar(con,
    "SELECT COALESCE(saldo_inicial,0) FROM turnos WHERE id = ?", turnoId);

            // Solo efectivo físico: la venta con tarjeta no está en la caja
            double totalCaja = ventasEfectivo + ingresos + saldoInicial - egresos;

            lblVentasDia.setText("Total Ventas del turno: $" + String.format("%.2f", ventasTotal));
            lblEfectivo.setText("Venta en efectivo: $" + String.format("%.2f", ventasEfectivo));
            lblTarjeta.setText("Venta en tarjeta: $" + String.format("%.2f", ventasTarjeta));

            lblHasVendido.setText("Has Vendido: $" + String.format("%,.2f", ventasTotal));
            lblIngreso.setText("Ingreso: $" + String.format("%,.2f", ingresos));
            lblSaldoInicial.setText("Saldo inicial en caja: $" + String.format("%,.2f", saldoInicial));
            lblHasGastado.setText("Has Gastado: $" + String.format("%,.2f", egresos));
            lblGanancia.setText("Ganancia: $" + String.format("%,.2f", ganancia));
            lblTotalCaja.setText("Total en caja: $" + String.format("%,.2f", totalCaja));

        } catch (Exception e) {
            limpiarResumen();
            lblVentasDia.setText("Error al consultar ventas del turno");
            e.printStackTrace();
        }
    }

    public void refrescarDatos() {
        cargarVentasDelTurno();
        panelSuperior.revalidate();
        panelSuperior.repaint();
    }

    private JLabel crearLabel(int tam, Color color) {
        JLabel l = new JLabel();
        l.setFont(new Font("Arial", Font.BOLD, tam));
        if (color != null) l.setForeground(color);
        return l;
    }

    public void initComponents() {
        panelSuperior = new JPanel();

        lblVentasDia = crearLabel(24, null);
        lblEfectivo  = crearLabel(20, null);
        lblTarjeta   = crearLabel(20, null);

        lblHasVendido   = crearLabel(18, new Color(46, 139, 46));
        lblIngreso      = crearLabel(16, null);
        lblSaldoInicial = crearLabel(16, null);
        lblHasGastado   = crearLabel(18, new Color(220, 53, 69));
        lblGanancia     = crearLabel(18, new Color(30, 144, 255));
        lblTotalCaja    = crearLabel(16, null);

        lblVentasDia.setText("Total Ventas del turno: $0.00");
        lblEfectivo.setText("Venta en efectivo: $0.00");
        lblTarjeta.setText("Venta en tarjeta: $0.00");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ventas del Turno");

        JLabel separador = new JLabel(" ");

        GroupLayout layout = new GroupLayout(panelSuperior);
        panelSuperior.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(30)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblVentasDia)
                    .addComponent(lblEfectivo)
                    .addComponent(lblTarjeta)
                    .addComponent(separador)
                    .addComponent(lblHasVendido)
                    .addComponent(lblIngreso)
                    .addComponent(lblSaldoInicial)
                    .addComponent(lblHasGastado)
                    .addComponent(lblGanancia)
                    .addComponent(lblTotalCaja))
                .addGap(30)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(30)
                .addComponent(lblVentasDia)
                .addGap(15)
                .addComponent(lblEfectivo)
                .addGap(15)
                .addComponent(lblTarjeta)
                .addGap(10)
                .addComponent(separador)
                .addGap(10)
                .addComponent(lblHasVendido)
                .addGap(12)
                .addComponent(lblIngreso)
                .addGap(12)
                .addComponent(lblSaldoInicial)
                .addGap(12)
                .addComponent(lblHasGastado)
                .addGap(12)
                .addComponent(lblGanancia)
                .addGap(12)
                .addComponent(lblTotalCaja)
                .addGap(30)
        );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelSuperior, BorderLayout.CENTER);

        pack();
        setSize(500, getHeight());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            OpcVentasDelTurno ventana = new OpcVentasDelTurno();
            ventana.setVisible(true);
            ventana.refrescarDatos();
        });
    }
}