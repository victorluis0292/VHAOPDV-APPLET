package Vista;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DetalleVentaDialog extends JDialog {

    private final JButton btnCancelar = new JButton("Cancelar venta");
    private final JButton btnCerrar = new JButton("Cerrar");

    public DetalleVentaDialog(Window owner, int folio, String cliente, String vendedor,
                              double total, List<Object[]> productos, List<Object[]> promos) {
        super(owner, "Detalle de venta #" + folio, ModalityType.APPLICATION_MODAL);
        NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("<html><b>Folio:</b> " + folio + " &nbsp; <b>Cliente:</b> " + cliente
                + " &nbsp; <b>Vendedor:</b> " + vendedor + "</html>"), BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new Object[]{"Producto", "Cant.", "P. Unit.", "Importe"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] p : productos) {
            double cant = (double) p[1], precio = (double) p[2];
            m.addRow(new Object[]{p[0], String.format("%.3f", cant), moneda.format(precio), moneda.format(cant * precio)});
        }
        for (Object[] p : promos) {
            m.addRow(new Object[]{"Promo P-" + p[0] + ": " + p[1], "", "", "-" + moneda.format((double) p[3])});
        }
        add(new JScrollPane(new JTable(m)), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(new JLabel("TOTAL: " + moneda.format(total)), BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar.setForeground(Color.RED);
        botones.add(btnCancelar);
        botones.add(btnCerrar);
        sur.add(botones, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> dispose());
        setSize(600, 400);
        setLocationRelativeTo(owner);
    }

    public JButton getBtnCancelar() { return btnCancelar; }
}