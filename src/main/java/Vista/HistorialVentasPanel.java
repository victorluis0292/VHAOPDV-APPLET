package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.FlowLayout;

/**
 * Panel de Historial de Ventas.
 * SOLO responsabilidad visual: pintar tabla y botones.
 * Toda la lógica (listar, reimprimir, pdf, cancelar) vive en HistorialVentasController.
 */
public class HistorialVentasPanel extends JPanel {

    // Índices de columnas del modelo
    public static final int COL_ESTATUS = 4;
    public static final int COL_TICKET = 5;
    public static final int COL_ID_VENTA = 6; // oculta

    private JTable tableVentas;
    private DefaultTableModel modelo;
    private JButton btnPdf;
    private JTextField txtFolioSeleccionado;
    private JButton btnDiaAnterior;
    private JButton btnDiaSiguiente;
    private JLabel lblFechaActual;

    public HistorialVentasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Historial de Ventas", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(18f));

        JPanel panelNavDia = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnDiaAnterior = new JButton("◀ Día anterior");
        lblFechaActual = new JLabel("Hoy");
        lblFechaActual.setFont(lblFechaActual.getFont().deriveFont(14f));
        btnDiaSiguiente = new JButton("Día siguiente ▶");
        panelNavDia.add(btnDiaAnterior);
        panelNavDia.add(lblFechaActual);
        panelNavDia.add(btnDiaSiguiente);

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(titulo, BorderLayout.NORTH);
        panelNorte.add(panelNavDia, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Columnas visibles: Folio, Cliente, Vendedor, Total, Estatus, Ticket
        // Columna oculta (6): id interno de la venta
        modelo = new DefaultTableModel(
                new Object[]{"Folio", "Cliente", "Vendedor", "Total", "Estatus", "Ticket", "IdVenta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == COL_TICKET; // solo el botón "Reimprimir"
            }
        };

        tableVentas = new JTable(modelo);
        tableVentas.setRowHeight(28);
        tableVentas.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tableVentas);
        add(scroll, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPdf = new JButton("Generar PDF");
        
        txtFolioSeleccionado = new JTextField(8);
        txtFolioSeleccionado.setEditable(false);
        panelSur.add(new JLabel("Folio seleccionado:"));
        panelSur.add(txtFolioSeleccionado);
        panelSur.add(btnPdf);
        
        add(panelSur, BorderLayout.SOUTH);

        tableVentas.getSelectionModel().addListSelectionListener(e -> {
            int fila = tableVentas.getSelectedRow();
            if (fila >= 0) {
                txtFolioSeleccionado.setText(String.valueOf(tableVentas.getValueAt(fila, 0)));
            }
        });

        // Ocultar columna IdVenta (índice 6) sin quitarla del modelo
        tableVentas.getColumnModel().getColumn(COL_ID_VENTA).setMinWidth(0);
        tableVentas.getColumnModel().getColumn(COL_ID_VENTA).setMaxWidth(0);
        tableVentas.getColumnModel().getColumn(COL_ID_VENTA).setWidth(0);

        tableVentas.getColumnModel().getColumn(COL_TICKET).setMaxWidth(120);
        tableVentas.getColumnModel().getColumn(COL_TICKET).setMinWidth(100);

        tableVentas.getColumnModel().getColumn(COL_ESTATUS).setMaxWidth(110);
        tableVentas.getColumnModel().getColumn(COL_ESTATUS).setMinWidth(90);

        // Estatus con color: verde = Exitosa, rojo = Cancelada
        tableVentas.getColumnModel().getColumn(COL_ESTATUS).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel,
                                                           boolean hasFocus, int r, int c) {
                super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSel) {
                    if ("Cancelada".equals(val)) {
                        setForeground(new Color(200, 30, 30));
                    } else {
                        setForeground(new Color(30, 130, 50));
                    }
                }
                return this;
            }
        });
    }

    public JTable getTableVentas() {
        return tableVentas;
    }

    public DefaultTableModel getModelo() {
        return modelo;
    }

    public JButton getBtnPdf() {
        return btnPdf;
    }

   

    public JTextField getTxtFolioSeleccionado() {
        return txtFolioSeleccionado;
    }

    public JButton getBtnDiaAnterior() {
        return btnDiaAnterior;
    }

    public JButton getBtnDiaSiguiente() {
        return btnDiaSiguiente;
    }

    public JLabel getLblFechaActual() {
        return lblFechaActual;
    }

    /** Limpia todas las filas de la tabla. */
    public void limpiar() {
        modelo.setRowCount(0);
    }

    /** Agrega una fila. idVentaOculto va en la columna 6 (oculta). */
    public void agregarFila(Object folio, String cliente, String vendedor, double total,
                            String estatus, int idVentaOculto) {
        modelo.addRow(new Object[]{folio, cliente, vendedor, total, estatus, "Reimprimir", idVentaOculto});
    }
}