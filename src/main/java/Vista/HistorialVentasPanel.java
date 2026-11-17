package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel de Historial de Ventas (por TURNO).
 * SOLO responsabilidad visual: pintar tabla, selector de turno y buscador.
 * Toda la lógica (listar, reimprimir, pdf, cancelar) vive en HistorialVentasController.
 */
public class HistorialVentasPanel extends JPanel {

    // Índices de columnas del modelo
    public static final int COL_FOLIO = 0;
    public static final int COL_ESTATUS = 4;
    public static final int COL_TICKET = 5;
    public static final int COL_ID_VENTA = 6; // oculta

    /** Elemento del combo de turnos. idTurno = 0 representa "Sin turno (anteriores)". */
    public static class TurnoItem {
        public final int idTurno;
        public final String texto;

        public TurnoItem(int idTurno, String texto) {
            this.idTurno = idTurno;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private JTable tableVentas;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnPdf;
    private JTextField txtFolioSeleccionado;
    private JComboBox<TurnoItem> cmbTurnos;
    private JTextField txtBuscarFolio;
    private JButton btnBuscarFolio;
    private JLabel lblResumenTurno;
    private JLabel lblTotalVendido;
    private boolean cargandoTurnos = false;

    public HistorialVentasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Historial de Ventas", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(18f));

        // ----- Selector de turno + buscador de folio -----
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelFiltros.add(new JLabel("Turno:"));
        cmbTurnos = new JComboBox<>();
        cmbTurnos.setPrototypeDisplayValue(new TurnoItem(-1, "#000 · usuario@correo.com · 00/00 00:00 – ABIERTO"));
        panelFiltros.add(cmbTurnos);

        panelFiltros.add(new JLabel("   Folio:"));
        txtBuscarFolio = new JTextField(7);
        soloNumeros(txtBuscarFolio, 9); // el folio es INT: solo dígitos, máx. 9
        txtBuscarFolio.setToolTipText("Escribe para filtrar en este turno. Enter = buscar en todos los turnos");
        btnBuscarFolio = new JButton("Buscar");
        panelFiltros.add(txtBuscarFolio);
        panelFiltros.add(btnBuscarFolio);

        lblResumenTurno = new JLabel(" ", JLabel.CENTER);
        lblResumenTurno.setForeground(Color.GRAY);

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(titulo, BorderLayout.NORTH);
        panelNorte.add(panelFiltros, BorderLayout.CENTER);
        panelNorte.add(lblResumenTurno, BorderLayout.SOUTH);
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

        // Filtro local por folio (en vivo, solo sobre el turno cargado)
        sorter = new TableRowSorter<>(modelo);
        tableVentas.setRowSorter(sorter);
        txtBuscarFolio.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltroFolio(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltroFolio(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltroFolio(); }
        });

        JScrollPane scroll = new JScrollPane(tableVentas);
        add(scroll, BorderLayout.CENTER);

        // ----- Parte inferior: controles a la izquierda, total a la derecha -----
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPdf = new JButton("Generar PDF");
        txtFolioSeleccionado = new JTextField(8);
        txtFolioSeleccionado.setEditable(false);
        panelControles.add(new JLabel("Folio seleccionado:"));
        panelControles.add(txtFolioSeleccionado);
        panelControles.add(btnPdf);

        lblTotalVendido = new JLabel("Total vendido: $0.00");
        lblTotalVendido.setFont(lblTotalVendido.getFont().deriveFont(Font.BOLD, 16f));
        lblTotalVendido.setForeground(new Color(30, 130, 50));
        lblTotalVendido.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelControles, BorderLayout.WEST);
        panelSur.add(lblTotalVendido, BorderLayout.EAST);
        add(panelSur, BorderLayout.SOUTH);

        tableVentas.getSelectionModel().addListSelectionListener(e -> {
            int fila = tableVentas.getSelectedRow();
            if (fila >= 0) {
                txtFolioSeleccionado.setText(String.valueOf(tableVentas.getValueAt(fila, COL_FOLIO)));
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

    /** Restringe un JTextField a dígitos (también al pegar texto) y a una longitud máxima. */
    private static void soloNumeros(JTextField campo, int maxDigitos) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
                    throws BadLocationException {
                replace(fb, offset, 0, texto, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String texto, AttributeSet attr)
                    throws BadLocationException {
                if (texto == null || texto.isEmpty()) {
                    super.replace(fb, offset, length, texto, attr);
                    return;
                }
                String soloDigitos = texto.replaceAll("\\D", "");
                int espacio = maxDigitos - (fb.getDocument().getLength() - length);
                if (espacio <= 0 || soloDigitos.isEmpty()) {
                    return; // ignora letras, símbolos o exceso de longitud
                }
                if (soloDigitos.length() > espacio) {
                    soloDigitos = soloDigitos.substring(0, espacio);
                }
                super.replace(fb, offset, length, soloDigitos, attr);
            }
        });
    }

    private void aplicarFiltroFolio() {
        String txt = txtBuscarFolio.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(Pattern.quote(txt), COL_FOLIO));
        }
    }

    // ===================== Getters =====================

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

    public JComboBox<TurnoItem> getCmbTurnos() {
        return cmbTurnos;
    }

    public JTextField getTxtBuscarFolio() {
        return txtBuscarFolio;
    }

    public JButton getBtnBuscarFolio() {
        return btnBuscarFolio;
    }

    // ===================== API para el controller =====================

    /** Llena el combo de turnos SIN disparar el listener del combo. */
    public void setTurnos(List<TurnoItem> turnos, int idTurnoASeleccionar) {
        cargandoTurnos = true;
        try {
            cmbTurnos.removeAllItems();
            TurnoItem seleccionado = null;
            for (TurnoItem t : turnos) {
                cmbTurnos.addItem(t);
                if (t.idTurno == idTurnoASeleccionar) {
                    seleccionado = t;
                }
            }
            if (seleccionado != null) {
                cmbTurnos.setSelectedItem(seleccionado);
            } else if (cmbTurnos.getItemCount() > 0) {
                cmbTurnos.setSelectedIndex(0);
            }
        } finally {
            cargandoTurnos = false;
        }
    }

    /** true mientras setTurnos() está llenando el combo (usar en el ActionListener del combo). */
    public boolean isCargandoTurnos() {
        return cargandoTurnos;
    }

    /** Turno elegido en el combo, o null si no hay. */
    public TurnoItem getTurnoSeleccionado() {
        return (TurnoItem) cmbTurnos.getSelectedItem();
    }

    /** Texto bajo el selector, ej. "Cajero: efren@gmail.com · Fondo: $900.00 · 3 ventas". */
    public void setResumenTurno(String texto) {
        lblResumenTurno.setText(texto == null || texto.isEmpty() ? " " : texto);
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

    /** Selecciona (y hace visible) la fila de un folio ya cargado. Devuelve false si no está. */
    public boolean seleccionarFolio(String folio) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (String.valueOf(modelo.getValueAt(i, COL_FOLIO)).equals(folio)) {
                int vista = tableVentas.convertRowIndexToView(i);
                if (vista >= 0) {
                    tableVentas.setRowSelectionInterval(vista, vista);
                    tableVentas.scrollRectToVisible(tableVentas.getCellRect(vista, 0, true));
                    return true;
                }
            }
        }
        return false;
    }

    /** Muestra el total de ventas exitosas (sin canceladas). */
    public void setTotalVendido(double total) {
        lblTotalVendido.setText(String.format("Total vendido: $%,.2f", total));
    }
}