package Utilidades;

import javax.swing.*;
import java.awt.*;

/**
 * LoaderUpdateSystemPDV
 * Ventana modal que muestra un mensaje y una barra de progreso
 * durante el proceso de actualización o verificación del sistema.
 * Arranca en modo indeterminado y pasa a porcentaje real cuando se
 * llama a setProgreso() con el tamaño total conocido.
 */
public class LoaderUpdateSystemPDV extends JDialog {

    private final JLabel lblMensaje;
    private final JLabel lblDetalle;   // NUEVO: "45% · 88.0 / 194.0 MB"
    private final JProgressBar barra;

    public LoaderUpdateSystemPDV(Frame parent, String mensaje) {
        super(parent, "Actualizando sistema", true);

        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Mensaje principal
        lblMensaje = new JLabel(mensaje, SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMensaje.setForeground(new Color(60, 60, 60));
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(20, 40, 4, 40));

        // NUEVO: detalle de avance (porcentaje y MB)
        lblDetalle = new JLabel(" ", SwingConstants.CENTER);
        lblDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalle.setForeground(new Color(110, 110, 110));
        lblDetalle.setBorder(BorderFactory.createEmptyBorder(0, 40, 12, 40));

        // Barra de carga (indeterminada hasta conocer el tamaño total)
        barra = new JProgressBar(0, 100);
        barra.setIndeterminate(true);
        barra.setBorderPainted(false);
        barra.setBackground(Color.WHITE);
        barra.setPreferredSize(new Dimension(300, 10));

        // Mensaje + detalle en un solo bloque
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Color.WHITE);
        centro.add(lblMensaje, BorderLayout.CENTER);
        centro.add(lblDetalle, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true));
        panel.add(centro, BorderLayout.CENTER);
        panel.add(barra, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        pack();
        setSize(320, 150);              // un poco más alto por la línea de detalle
        setLocationRelativeTo(parent);
    }

    public LoaderUpdateSystemPDV() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Actualiza el mensaje principal. */
    public void setMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> lblMensaje.setText(mensaje));
    }

    /**
     * NUEVO: actualiza el avance de la descarga.
     * Si total <= 0 (tamaño desconocido) la barra queda indeterminada
     * y solo se muestran los MB descargados.
     */
    public void setProgreso(long actual, long total) {
        SwingUtilities.invokeLater(() -> {
            if (total <= 0) {
                barra.setIndeterminate(true);
                lblDetalle.setText(String.format("%.1f MB descargados", actual / 1048576.0));
                return;
            }
            int pct = (int) (actual * 100 / total);
            barra.setIndeterminate(false);
            barra.setValue(pct);
            lblDetalle.setText(String.format("%d%%  ·  %.1f / %.1f MB",
                    pct, actual / 1048576.0, total / 1048576.0));
        });
    }

    /** Muestra el loader de forma segura desde el hilo de Swing. */
    public void mostrar() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    /** Cierra el loader de forma segura desde el hilo de Swing. */
    public void cerrar() {
        SwingUtilities.invokeLater(this::dispose);
    }
}