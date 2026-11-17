package Estilos;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Estilo de tarjetas SOLO para la pantalla "Corte de caja". */
public class PanelCorte {

    public static final Color FONDO  = new Color(245, 244, 251);
    public static final Color MORADO = new Color(74, 10, 214);
    private static final Color BORDE = new Color(230, 227, 244);
    private static final Color BORDE_HOVER = new Color(185, 166, 245);

    public enum Icono { DESCARGA, FLUJO, GRAFICA, CAJA, RECIBO }

    /** Fondo, margen y layout del panel que contiene las tarjetas. */
    public static void aplicarFondo(JPanel panel) {
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setLayout(new GridBagLayout());
    }

    /** Agrega una tarjeta en (fila, columna) ocupando "ancho" columnas. */
    public static void agregar(JPanel panel, TarjetaCorte t, int fila, int col, int ancho) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = fila;
        gbc.gridwidth = ancho;
        gbc.weightx = ancho;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(t, gbc);
    }

    /** Tarjeta redondeada con icono, título y descripción. */
    public static class TarjetaCorte extends JButton {
        private final String titulo, descripcion;
        private final Icono icono;
        private final boolean primaria;
        private boolean hover = false;

        public TarjetaCorte(String titulo, String descripcion, Icono icono, boolean primaria) {
            super(titulo);
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.icono = icono;
            this.primaria = primaria;

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 150));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 24, 24);

            // Fondo y borde
            g2.setColor(primaria ? MORADO : Color.WHITE);
            g2.fill(forma);
            if (primaria) {
                g2.setColor(hover ? new Color(100, 50, 235) : MORADO);
            } else {
                g2.setColor(hover ? BORDE_HOVER : BORDE);
            }
            g2.draw(forma);

            // Insignia del icono
            int pad = 24, b = 48;
            Color trazo, tinte;
            switch (icono) {
                case DESCARGA: trazo = new Color(30, 142, 78);  tinte = new Color(230, 246, 236); break;
                case FLUJO:    trazo = MORADO;                  tinte = new Color(234, 229, 253); break;
                case GRAFICA:  trazo = new Color(31, 111, 219); tinte = new Color(227, 238, 253); break;
                case CAJA:     trazo = new Color(194, 106, 0);  tinte = new Color(255, 241, 221); break;
                default:       trazo = Color.WHITE;             tinte = new Color(255, 255, 255, 46); break;
            }
            g2.setColor(tinte);
            g2.fill(new RoundRectangle2D.Float(pad, pad, b, b, 14, 14));
            dibujarIcono(g2, pad + b / 2f, pad + b / 2f, trazo);

            // Textos
            FontMetrics fmT, fmD;
            Font fT = new Font("Segoe UI", Font.BOLD, 18);
            Font fD = new Font("Segoe UI", Font.PLAIN, 13);
            fmT = g2.getFontMetrics(fT);
            fmD = g2.getFontMetrics(fD);

            int yDesc = h - pad - fmD.getDescent();
            int yTitulo = yDesc - fmD.getAscent() - 6 - fmT.getDescent();

            g2.setFont(fT);
            g2.setColor(primaria ? Color.WHITE : new Color(30, 26, 51));
            g2.drawString(titulo, pad, yTitulo);

            g2.setFont(fD);
            g2.setColor(primaria ? new Color(220, 208, 255) : new Color(91, 86, 117));
            g2.drawString(descripcion, pad, yDesc);

            g2.dispose();
        }

        private void dibujarIcono(Graphics2D g2, float cx, float cy, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (icono) {
                case DESCARGA: {
                    g2.draw(new Line2D.Float(cx, cy - 9, cx, cy + 3));
                    Path2D p = new Path2D.Float();
                    p.moveTo(cx - 6, cy - 3); p.lineTo(cx, cy + 3); p.lineTo(cx + 6, cy - 3);
                    g2.draw(p);
                    g2.draw(new Line2D.Float(cx - 9, cy + 9, cx + 9, cy + 9));
                    break;
                }
                case FLUJO: {
                    g2.draw(new Line2D.Float(cx - 5, cy + 9, cx - 5, cy - 9));
                    Path2D a = new Path2D.Float();
                    a.moveTo(cx - 9, cy - 5); a.lineTo(cx - 5, cy - 9); a.lineTo(cx - 1, cy - 5);
                    g2.draw(a);
                    g2.draw(new Line2D.Float(cx + 5, cy - 9, cx + 5, cy + 9));
                    Path2D d = new Path2D.Float();
                    d.moveTo(cx + 1, cy + 5); d.lineTo(cx + 5, cy + 9); d.lineTo(cx + 9, cy + 5);
                    g2.draw(d);
                    break;
                }
                case GRAFICA: {
                    g2.draw(new Line2D.Float(cx - 8, cy + 8, cx - 8, cy));
                    g2.draw(new Line2D.Float(cx, cy + 8, cx, cy - 9));
                    g2.draw(new Line2D.Float(cx + 8, cy + 8, cx + 8, cy - 3));
                    break;
                }
                case CAJA: {
                    g2.draw(new RoundRectangle2D.Float(cx - 10, cy - 6, 20, 12, 4, 4));
                    g2.draw(new java.awt.geom.Ellipse2D.Float(cx - 3, cy - 3, 6, 6));
                    break;
                }
                default: { // RECIBO
                    Path2D r = new Path2D.Float();
                    r.moveTo(cx - 8, cy - 10); r.lineTo(cx + 8, cy - 10); r.lineTo(cx + 8, cy + 10);
                    r.lineTo(cx + 4, cy + 7);  r.lineTo(cx, cy + 10);     r.lineTo(cx - 4, cy + 7);
                    r.lineTo(cx - 8, cy + 10); r.closePath();
                    g2.draw(r);
                    g2.draw(new Line2D.Float(cx - 4, cy - 4, cx + 4, cy - 4));
                    g2.draw(new Line2D.Float(cx - 4, cy, cx + 4, cy));
                    break;
                }
            }
        }
    }
}