
package Estilos;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class EstiloDialogoNuevoCliente {

    // Colores del diseño
    public static final Color FONDO = Color.WHITE;
    public static final Color VERDE = new Color(39, 153, 89);
    public static final Color VERDE_HOVER = new Color(30, 125, 72);
    public static final Color TEXTO = new Color(45, 55, 72);
    public static final Color TEXTO_SECUNDARIO = new Color(120, 130, 145);
    public static final Color BORDE = new Color(215, 222, 232);
    public static final Color CAMPO_FONDO = new Color(249, 250, 252);

    public static JPanel crearEncabezado() {

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 5));
        encabezado.setBackground(FONDO);
        encabezado.setBorder(
                new EmptyBorder(20, 25, 12, 25)
        );

        JLabel titulo = new JLabel("Agregar nuevo cliente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titulo.setForeground(TEXTO);

        JLabel subtitulo = new JLabel(
                "Completa los datos para registrar al cliente"
        );
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(TEXTO_SECUNDARIO);

        encabezado.add(titulo);
        encabezado.add(subtitulo);

        return encabezado;
    }

    public static void estilizarFormulario(JPanel formulario) {

        formulario.setBackground(FONDO);
        formulario.setBorder(
                new EmptyBorder(10, 25, 15, 25)
        );
    }

    public static void estilizarEtiqueta(JLabel etiqueta) {

        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiqueta.setForeground(TEXTO);
    }

    public static void estilizarCampo(JTextField campo) {

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setForeground(TEXTO);
        campo.setBackground(CAMPO_FONDO);
        campo.setCaretColor(VERDE);

        campo.setBorder(
                new CompoundBorder(
                        new LineBorder(BORDE, 1, true),
                        new EmptyBorder(7, 10, 7, 10)
                )
        );

        campo.setPreferredSize(new Dimension(220, 38));
    }

    public static void estilizarBotonGuardar(JButton boton) {

        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(VERDE);

        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);

        boton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        boton.setPreferredSize(new Dimension(145, 40));

        boton.setBorder(
                new EmptyBorder(8, 20, 8, 20)
        );

        boton.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(VERDE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(VERDE);
            }
        });
    }

    public static JPanel crearPanelBotones() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 25, 15)
        );

        panel.setBackground(FONDO);
        panel.setBorder(
                new MatteBorder(1, 0, 0, 0, BORDE)
        );

        return panel;
    }
}
