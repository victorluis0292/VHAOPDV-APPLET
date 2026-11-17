package Vista;

import Estilos.PanelCorte;
import Estilos.PanelCorte.Icono;
import Estilos.PanelCorte.TarjetaCorte;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JPanel;

public class menuCorteCaja {

    // Se conserva por si otras pantallas lo usan
    public static void aplicarEstilo(JButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(45, 137, 239));
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(30, 110, 220));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(45, 137, 239));
            }
        });
    }

    public static void agregarBotonesAJPanel(JPanel panel, String usuario) {

        // Tarjetas con el estilo PanelCorte
        TarjetaCorte btn1 = new TarjetaCorte("Ventas del Turno", "Total, efectivo, tarjeta y resumen", Icono.DESCARGA, false);
        TarjetaCorte btn2 = new TarjetaCorte("Operaciones", "Ingresos y egresos de caja", Icono.FLUJO, false);
        TarjetaCorte btn3 = new TarjetaCorte("Análisis", "Día, semana, mes y año", Icono.GRAFICA, false);
        TarjetaCorte btn4 = new TarjetaCorte("Arqueo de Caja", "Conteo de billetes y monedas", Icono.CAJA, false);
        TarjetaCorte btn5 = new TarjetaCorte("Corte Cajero", "Cierra el turno y arranca el siguiente desde cero", Icono.RECIBO, true);

        // Acciones (sin cambios)
        btn1.addActionListener(e -> {
            if (OpcVentasDelTurnoNA.instance == null || !OpcVentasDelTurnoNA.instance.isVisible()) {
                OpcVentasDelTurnoNA.instance = new OpcVentasDelTurnoNA();
                OpcVentasDelTurnoNA.instance.setVisible(true);
            } else {
                OpcVentasDelTurnoNA.instance.toFront();
            }
        });

        btn2.addActionListener(e -> {
            if (verMovimientosCaja.instance == null || !verMovimientosCaja.instance.isVisible()) {
                verMovimientosCaja.instance = new verMovimientosCaja();
                verMovimientosCaja.instance.setVisible(true);
            } else {
                verMovimientosCaja.instance.toFront();
            }
        });

        btn3.addActionListener(e -> {
            if (Analisis.instance == null || !Analisis.instance.isVisible()) {
                Analisis.instance = new Analisis();
                Analisis.instance.setVisible(true);
            } else {
                Analisis.instance.toFront();
            }
        });

        btn4.addActionListener(e -> {
            if (ArchedBox.instance == null || !ArchedBox.instance.isVisible()) {
                ArchedBox.instance = new ArchedBox();
                ArchedBox.instance.setVisible(true);
            } else {
                ArchedBox.instance.toFront();
            }
        });

        btn5.addActionListener(e -> {
            CorteCajeroView.getInstance(usuario, Sistema.getInstancia().getLoginView()).setVisible(true);
        });

        // Fondo, margen y layout del panel + acomodo de tarjetas
        PanelCorte.aplicarFondo(panel);
        PanelCorte.agregar(panel, btn1, 0, 0, 1);
        PanelCorte.agregar(panel, btn2, 0, 1, 1);
        PanelCorte.agregar(panel, btn3, 0, 2, 1);
        PanelCorte.agregar(panel, btn4, 1, 0, 1);
        PanelCorte.agregar(panel, btn5, 1, 1, 2);   // Corte Cajero ocupa dos columnas

        panel.revalidate();
        panel.repaint();
    }
}