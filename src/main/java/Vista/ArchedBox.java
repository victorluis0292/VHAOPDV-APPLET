package Vista;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ArchedBox extends OpcVentasDelTurnoNA {
    public static ArchedBox instance;
    private JPanel panelInferior;
    private JLabel lblTotalCaja; // 🔵 Ahora como atributo

  public ArchedBox() {
    super.initComponents(); 
    instance = this; 
    initPanelInferior();

    JPanel contenedorPrincipal = new JPanel();
    contenedorPrincipal.setLayout(new BoxLayout(contenedorPrincipal, BoxLayout.Y_AXIS));
    contenedorPrincipal.add(panelSuperior);

  String usuario = Sistema.getUsuarioActivo();
BigDecimal totalEnCaja = Controlador.TurnoController.getInstance(usuario).calcularVentasDelTurno();

    lblTotalCaja = new JLabel("Total en caja (según reporte): $" + String.format("%.2f", totalEnCaja));
    lblTotalCaja.setFont(new Font("Arial", Font.BOLD, 16));
    lblTotalCaja.setForeground(new Color(0, 102, 204));

    JPanel panelTotalCaja = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelTotalCaja.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
    panelTotalCaja.add(lblTotalCaja);

    contenedorPrincipal.add(panelTotalCaja);
    contenedorPrincipal.add(panelInferior);

    getContentPane().removeAll();
    getContentPane().add(contenedorPrincipal);

    pack();
    setLocationRelativeTo(null);
    setTitle("Arqueo de Caja");
    refrescarDatos();
}

    private void initPanelInferior() {
        panelInferior = new JPanel();
        panelInferior.setBorder(BorderFactory.createTitledBorder("Detalle efectivo"));
        panelInferior.setLayout(new BorderLayout());

        JPanel panelTabla = new JPanel(new GridLayout(10, 3, 10, 5));

        // Encabezados
        panelTabla.add(new JLabel("Denominación", JLabel.CENTER));
        panelTabla.add(new JLabel("Cantidad", JLabel.CENTER));
        panelTabla.add(new JLabel("Monto", JLabel.CENTER));

        int[] denominaciones = {500, 200, 100, 50, 20, 10, 5, 2, 1};
        JTextField[] camposCantidad = new JTextField[denominaciones.length];
        JLabel[] camposMonto = new JLabel[denominaciones.length];

        JLabel lblTotal = new JLabel("0.00");
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));

        for (int i = 0; i < denominaciones.length; i++) {
            int valor = denominaciones[i];

            JLabel lblDenom = new JLabel(String.valueOf(valor), JLabel.CENTER);
            JTextField txtCantidad = new JTextField();
            JLabel lblMonto = new JLabel("0.00", JLabel.CENTER);
            lblMonto.setFont(new Font("SansSerif", Font.PLAIN, 14));

            camposCantidad[i] = txtCantidad;
            camposMonto[i] = lblMonto;

            txtCantidad.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }

                private void actualizar() {
                    try {
                        int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                        int monto = cantidad * valor;
                        lblMonto.setText(String.format("%.2f", (double) monto));
                    } catch (NumberFormatException ex) {
                        lblMonto.setText("0.00");
                    }

                    double total = 0;
                    for (int j = 0; j < denominaciones.length; j++) {
                        try {
                            int cant = Integer.parseInt(camposCantidad[j].getText().trim());
                            total += cant * denominaciones[j];
                        } catch (NumberFormatException ex) {
                            // Ignorar
                        }
                    }
                    lblTotal.setText(String.format("%.2f", total));
                }
            });

            panelTabla.add(lblDenom);
            panelTabla.add(txtCantidad);
            panelTabla.add(lblMonto);
        }

        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblTextoTotal = new JLabel("Total: ");
        lblTextoTotal.setFont(new Font("Arial", Font.BOLD, 16));
        panelTotal.add(lblTextoTotal);
        panelTotal.add(lblTotal);

        panelInferior.add(panelTabla, BorderLayout.CENTER);
        panelInferior.add(panelTotal, BorderLayout.SOUTH);
    }
//Método de actualización (si necesitas llamar luego):
  public void actualizarTotalCaja() {
    String usuario = Sistema.getUsuarioActivo();
    BigDecimal total = Controlador.TurnoController.getInstance(usuario).calcularVentasDelTurno();
    if (lblTotalCaja != null) {
        lblTotalCaja.setText("Total en caja (ventas del turno): $" + String.format("%.2f", total));
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArchedBox frame = new ArchedBox();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.refrescarDatos();
        });
    }
}
