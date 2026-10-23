package Estilos;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Estilos de los banners de fiado (Crédito Cliente) en la pestaña de venta.
 *
 * BannersCreditoCliente solo llama a estos métodos; aquí viven
 * colores, fuentes, tamaños y el dibujo de la tarjeta.
 */
public final class EstilosBannersCreditocliente {

    private EstilosBannersCreditocliente() { }

    // ============================================================
    // TAMAÑO
    // ============================================================

    public static final int ANCHO = 160;
    public static final int ALTO  = 56;
    public static final int SEPARACION = 8;

    // ============================================================
    // COLORES
    // ============================================================

    private static final Color BORDE          = new Color(226, 232, 240);
    private static final Color FONDO_HOVER    = new Color(248, 250, 252);
    private static final Color TXT_PRINCIPAL  = new Color(30, 41, 59);
    private static final Color TXT_SUAVE      = new Color(100, 116, 139);
    private static final Color TXT_MONTO      = new Color(15, 23, 42);
    private static final Color TXT_TITULO     = new Color(71, 85, 105);

    private static final Color VERDE  = new Color(22, 163, 74);
    private static final Color AMBAR  = new Color(217, 119, 6);
    private static final Color ROJO   = new Color(220, 38, 38);

    // Umbrales de urgencia reglas (en días)
    private static final int DIAS_AMBAR = 6;
    private static final int DIAS_ROJO  = 8;

    // ============================================================
    // FUENTES
    // ============================================================

    private static final Font F_TITULO = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_NOMBRE = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_FOLIO  = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font F_MONTO  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_CHIP   = new Font("Segoe UI", Font.BOLD, 10);

    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    // ============================================================
    // API PÚBLICA
    // ============================================================

    /** Estilo del texto "Fiados pendientes (N)". */
    public static void estiloTitulo(JLabel lbl) {
        lbl.setFont(F_TITULO);
        lbl.setForeground(TXT_TITULO);
    }

    /** Para el panel contenedor y el viewport: sin fondo propio. */
    public static void estiloContenedor(JComponent c) {
        c.setOpaque(false);
        c.setBorder(null);
    }

    /**
 * Permite deslizar los banners arrastrando con el mouse sobre cualquier
 * parte del área (fondo o tarjetas). Usa coordenadas de pantalla porque
 * el contenido se mueve mientras se arrastra.
 */
public static void habilitarArrastre(final JScrollPane scroll, Component c) {

    MouseAdapter arrastre = new MouseAdapter() {

        private int inicioX;
        private int valorInicial;

        @Override
        public void mousePressed(MouseEvent e) {
            inicioX = e.getXOnScreen();
            valorInicial = scroll.getHorizontalScrollBar().getValue();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int dx = e.getXOnScreen() - inicioX;
            scroll.getHorizontalScrollBar().setValue(valorInicial - dx);
        }
    };

    c.addMouseListener(arrastre);
    c.addMouseMotionListener(arrastre);
}
    /** Color según antigüedad de la deuda. */
    public static Color colorUrgencia(long dias) {
        if (dias > DIAS_ROJO)  return ROJO;
        if (dias > DIAS_AMBAR) return AMBAR;
        return VERDE;
    }

    /** "hoy", "ayer", "N días". */
    public static String textoAntiguedad(long dias) {
        if (dias <= 0) return "hoy";
        if (dias == 1) return "ayer";
        return dias + " días";
    }

    /** Crea una tarjeta lista para agregar al contenedor. */
    public static JPanel crearTarjeta(String nombre, String folio,
                                      double saldo, long dias,
                                      Runnable onClick) {
        return new Tarjeta(nombre, folio, saldo, dias, onClick);
    }

    // ============================================================
    // TARJETA (dibujo)
    // ============================================================

    private static final class Tarjeta extends JPanel {

        private static final int ARC = 14;

        private final String nombre;
        private final String folio;
        private final String monto;
        private final String antiguedad;
        private final Color acento;
        private boolean hover;
        private int pressX;
        
        
        Tarjeta(String nombre, String folio, double saldo,
                long dias, Runnable onClick) {

            this.nombre = nombre == null ? "" : nombre;
            this.folio = folio == null ? "" : folio;
            this.monto = MONEDA.format(saldo);
            this.antiguedad = textoAntiguedad(dias);
            this.acento = colorUrgencia(dias);

            setOpaque(false);
            setPreferredSize(new Dimension(ANCHO, ALTO));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(this.nombre + "  ·  " + monto + "  ·  " + antiguedad);

            addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hover = true;  repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                hover = false; repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                pressX = e.getXOnScreen();
            }
            @Override public void mouseReleased(MouseEvent e) {
                boolean fueArrastre = Math.abs(e.getXOnScreen() - pressX) > 5;
                if (!fueArrastre && onClick != null && contains(e.getPoint())) {
                    onClick.run();
                }
            }
        });
        }

        @Override
        protected void paintComponent(Graphics g0) {

            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth() - 3;   // 3 px reservados para la sombra
            int h = getHeight() - 3;

            // sombra
            for (int i = 0; i < 3; i++) {
                g.setColor(new Color(15, 23, 42, 10));
                g.fillRoundRect(i, i + 1, w, h, ARC, ARC);
            }

            // fondo + borde
            g.setColor(hover ? FONDO_HOVER : Color.WHITE);
            g.fillRoundRect(0, 0, w, h, ARC, ARC);
            g.setColor(BORDE);
            g.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

            // franja de color (recortada a la forma redondeada)
            // después
            Shape clipAnterior = g.getClip();
            g.clip(new RoundRectangle2D.Float(0, 0, w, h, ARC, ARC));   // intersecta, no reemplaza
            g.setColor(acento);
            g.fillRect(0, 0, 5, h);
            g.setClip(clipAnterior);

            int x = 14;

            // folio (arriba derecha)
            g.setFont(F_FOLIO);
            FontMetrics fmFolio = g.getFontMetrics();
            int folioW = fmFolio.stringWidth(folio);
            g.setColor(TXT_SUAVE);
            g.drawString(folio, w - 10 - folioW, 17);

            // nombre (arriba izquierda)
            g.setFont(F_NOMBRE);
            g.setColor(TXT_PRINCIPAL);
            int maxNombre = w - x - folioW - 20;
            g.drawString(truncar(nombre, g.getFontMetrics(), maxNombre), x, 17);

            // monto
            g.setFont(F_MONTO);
            g.setColor(TXT_MONTO);
            int montoW = g.getFontMetrics().stringWidth(monto);
            g.drawString(monto, x, 40);

            // píldora de antigüedad (se omite si no cabe)
            g.setFont(F_CHIP);
            FontMetrics fmChip = g.getFontMetrics();
            int chipW = fmChip.stringWidth(antiguedad) + 14;
            int chipH = 16;
            int chipX = w - 10 - chipW;
            int chipY = 27;

            if (x + montoW + 6 < chipX) {
                g.setColor(new Color(acento.getRed(), acento.getGreen(),
                        acento.getBlue(), 30));
                g.fillRoundRect(chipX, chipY, chipW, chipH, chipH, chipH);
                g.setColor(acento.darker());
                g.drawString(antiguedad, chipX + 7,
                        chipY + (chipH + fmChip.getAscent()
                                - fmChip.getDescent()) / 2);
            }

            g.dispose();
        }

        private static String truncar(String s, FontMetrics fm, int max) {
            if (fm.stringWidth(s) <= max) return s;
            while (!s.isEmpty() && fm.stringWidth(s + "…") > max) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "…";
        }
    }
}