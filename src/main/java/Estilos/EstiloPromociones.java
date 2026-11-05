package Estilos;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public final class EstiloPromociones {

    private EstiloPromociones() {}

    // ---------------- Colores ----------------
    public static final Color FONDO       = new Color(245, 247, 250);
    public static final Color AZUL        = new Color(0, 102, 204);
    public static final Color TEXTO       = new Color(70, 70, 70);
    public static final Color TEXTO_SUAVE = new Color(130, 130, 130);
    public static final Color BORDE       = new Color(210, 214, 220);
    public static final Color SELECCION   = new Color(220, 235, 250);
    public static final Color VERDE       = new Color(0, 128, 0);
    public static final Color NARANJA     = new Color(200, 120, 0);
    public static final Color ROJO        = new Color(200, 0, 0);

    // ---------------- Fuentes ----------------
    public static final Font F_TITULO   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font F_ETIQUETA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font F_TEXTO    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_CHICA    = new Font("Segoe UI", Font.PLAIN, 11);

    public static final int ALTO_FILA = 44;

    // ---------------- Ventana y textos ----------------
    public static void ventana(JDialog d) {
        d.getContentPane().setBackground(FONDO);
        ((JComponent) d.getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    public static void titulo(JLabel l) {
        l.setFont(F_TITULO);
        l.setForeground(AZUL);
    }

    public static void etiqueta(JLabel l) {
        l.setFont(F_ETIQUETA);
        l.setForeground(TEXTO);
    }

    public static void resumenConviene(JLabel l, String texto) {
        l.setForeground(VERDE);
        l.setText(texto);
    }

    public static void resumenNoConviene(JLabel l, String texto) {
        l.setForeground(ROJO);
        l.setText(texto);
    }

    public static void fecha(JDateChooser dc) {
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(0, 34));
    }

    public static void scroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
    }

    // ---------------- Listas ----------------
    /** Lista de productos ya agregados a la promoción. */
    public static void listaProductos(JList<?> l) {
        l.setFont(F_TEXTO);
        l.setSelectionBackground(SELECCION);
        l.setSelectionForeground(Color.BLACK);
    }

    /** Lista del popup de sugerencias (no toma el foco para no interrumpir la escritura). */
    public static void listaSugerencias(JList<ItemVisual> l) {
        l.setCellRenderer(new RendererItem());
        l.setFixedCellHeight(ALTO_FILA);
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        l.setFocusable(false);
    }

    public static JPopupMenu crearPopup(JScrollPane contenido) {
        JPopupMenu p = new JPopupMenu();
        p.setFocusable(false);
        p.setBorder(BorderFactory.createLineBorder(AZUL));
        p.add(contenido);
        return p;
    }

    // ---------------- Tabla ----------------
    public static DefaultTableCellRenderer rendererEstado() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String s = v == null ? "" : v.toString();
                    if (s.equals("Vigente")) comp.setForeground(VERDE);
                    else if (s.equals("Programada")) comp.setForeground(NARANJA);
                    else comp.setForeground(Color.GRAY);
                }
                return comp;
            }
        };
    }

    // ---------------- Componentes ----------------
    /** Lo que necesita el popup para dibujar un producto. */
    public interface ItemVisual {
        String titulo();
        String detalle();
        String precio();
        boolean vacio();   // true = fila "Sin resultados"
    }

    public static class RendererItem extends JPanel implements ListCellRenderer<ItemVisual> {
        private final JLabel lblTitulo = new JLabel();
        private final JLabel lblDetalle = new JLabel();
        private final JLabel lblPrecio = new JLabel();

        public RendererItem() {
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(4, 10, 4, 10));
            lblTitulo.setFont(F_ETIQUETA);
            lblDetalle.setFont(F_CHICA);
            lblDetalle.setForeground(TEXTO_SUAVE);
            lblPrecio.setFont(F_ETIQUETA);
            lblPrecio.setForeground(AZUL);

            JPanel textos = new JPanel(new GridLayout(2, 1));
            textos.setOpaque(false);
            textos.add(lblTitulo);
            textos.add(lblDetalle);
            add(textos, BorderLayout.CENTER);
            add(lblPrecio, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ItemVisual> list, ItemVisual v,
                                                      int index, boolean sel, boolean foc) {
            setOpaque(true);
            if (v == null || v.vacio()) {
                lblTitulo.setText("Sin resultados");
                lblTitulo.setForeground(TEXTO_SUAVE);
                lblDetalle.setText("Prueba con otro nombre o código");
                lblPrecio.setText("");
                setBackground(Color.WHITE);
                return this;
            }
            lblTitulo.setText(v.titulo());
            lblTitulo.setForeground(Color.BLACK);
            lblDetalle.setText(v.detalle());
            lblPrecio.setText(v.precio());
            setBackground(sel ? SELECCION : Color.WHITE);
            return this;
        }
    }

    /** Campo de búsqueda con lupa dibujada y texto de ayuda (sin imágenes externas). */
    public static class CampoBusqueda extends JTextField {
        private final String placeholder;

        public CampoBusqueda(String placeholder) {
            this.placeholder = placeholder;
            Estilos.estiloCampo(this);
            setFont(F_TEXTO);
            setBorder(BorderFactory.createCompoundBorder(getBorder(), new EmptyBorder(0, 26, 0, 6)));
            setPreferredSize(new Dimension(0, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // lupa
            int x = 10, y = (getHeight() - 14) / 2;
            g2.setColor(TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(x, y, 9, 9);
            g2.drawLine(x + 8, y + 8, x + 13, y + 13);

            // placeholder
            if (getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(new Color(160, 160, 160));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, getInsets().left,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}