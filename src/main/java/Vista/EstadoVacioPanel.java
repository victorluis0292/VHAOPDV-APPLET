package Vista;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class EstadoVacioPanel extends JPanel {

    private final JLabel lblImagen = new JLabel();
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblSubtitulo = new JLabel();

    public EstadoVacioPanel(String rutaImagen, int tamanoImagen, String titulo, String subtitulo) {
        setLayout(new GridBagLayout());   // centra el contenido
        setBackground(Color.WHITE);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(70, 70, 70));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitulo.setForeground(new Color(130, 130, 130));
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenido.add(lblImagen);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(lblSubtitulo);
        add(contenido);

        setImagen(rutaImagen, tamanoImagen);
        setTitulo(titulo);
        setSubtitulo(subtitulo);
    }

    /** Carga la imagen del classpath, conservando su proporción (lado mayor = tamano). */
    public final void setImagen(String ruta, int tamano) {
        ImageIcon icono = null;
        if (ruta != null) {
            URL url = getClass().getResource(ruta);
            if (url != null) {
                ImageIcon original = new ImageIcon(url);
                int w = original.getIconWidth();
                int h = original.getIconHeight();
                if (w > 0 && h > 0) {
                    double escala = Math.min((double) tamano / w, (double) tamano / h);
                    Image img = original.getImage().getScaledInstance(
                            Math.max(1, (int) (w * escala)),
                            Math.max(1, (int) (h * escala)),
                            Image.SCALE_SMOOTH);
                    icono = new ImageIcon(img);
                }
            }
        }
        lblImagen.setIcon(icono);
        lblImagen.setVisible(icono != null);   // si falta la imagen, solo se ve el texto
    }

    public final void setTitulo(String titulo) {
        lblTitulo.setText(titulo == null ? "" : titulo);
    }

    public final void setSubtitulo(String subtitulo) {
        boolean hay = subtitulo != null && !subtitulo.isEmpty();
        lblSubtitulo.setText(hay ? subtitulo : " ");
        lblSubtitulo.setVisible(hay);
    }
}