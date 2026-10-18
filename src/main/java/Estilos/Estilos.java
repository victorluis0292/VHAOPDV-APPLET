// Archivo: Estilos.java
package Estilos;

import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
public class Estilos {

    public static void estiloBoton(JButton boton) {
        boton.setBackground(new Color(0, 153, 255));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
    }

    public static void estiloTabla(JTable tabla) {
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.setRowHeight(25);
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
    }
public static void estiloTablas(JTable tabla) {
    tabla.setFont(new Font("Arial", Font.PLAIN, 20));
    tabla.setRowHeight(25);

    JTableHeader header = tabla.getTableHeader();
    header.setFont(new Font("Arial", Font.BOLD, 17));
    header.setBackground(new Color(0, 102, 204));
    header.setForeground(Color.WHITE);
    ajustarAnchosPorNombre(tabla);

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String nombre = table.getValueAt(row, 2).toString();

            if (nombre.toUpperCase().startsWith("ABONO REALIZADO")) {
                c.setBackground(new Color(204, 255, 229)); // verde claro
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(230, 244, 255));
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    };

    for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
    if ("Eliminar".equals(tabla.getColumnName(i))) continue;   // no pisar el botón
    tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
}

    tabla.setShowGrid(false);
    tabla.setIntercellSpacing(new java.awt.Dimension(0, 0));
    tabla.setFocusable(true);
    tabla.setRowSelectionAllowed(true);

    Component parent = tabla.getParent();
    if (parent != null && parent.getParent() instanceof JScrollPane) {
        JScrollPane scroll = (JScrollPane) parent.getParent();
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
    }
}

public static void ajustarAnchosPorNombre(JTable tabla) {
    TableColumnModel columnModel = tabla.getColumnModel();
    
    // Recorre todas las columnas que tiene la tabla
    for (int i = 0; i < tabla.getColumnCount(); i++) {
        // Obtiene el nombre de la columna en la posición actual
        String nombreColumna = tabla.getColumnName(i);
        
        // Usamos un switch para asignar el ancho según el nombre
        switch (nombreColumna) {
            case "Nombre":
                    columnModel.getColumn(i).setPreferredWidth(450);
                    break;
            case "Fecha":
                    columnModel.getColumn(i).setPreferredWidth(120);
                break;
            case "ID":
                columnModel.getColumn(i).setPreferredWidth(60);
                break;
            case "ID Prod":
                columnModel.getColumn(i).setPreferredWidth(70);
                break;
            case "Cantidad":
            case "Precio":
            case "Total":
            case "DNI":
                columnModel.getColumn(i).setPreferredWidth(80);
                break;
            // Puedes añadir más casos para otras columnas si lo necesitas
        }
    }
}


    public static void estiloEtiqueta(JLabel etiqueta) {
        etiqueta.setFont(new Font("Arial", Font.PLAIN, 16));
        etiqueta.setForeground(Color.DARK_GRAY);
    }
    
    public static void estiloEtiqueta2(JLabel etiqueta) {
        etiqueta.setFont(new Font("Arial", Font.BOLD, 18));
        etiqueta.setForeground(Color.DARK_GRAY);
    }
    //para campos no editables
  public static void estiloEtiqueta3(JTextField campo) {
     campo.setFont(new Font("Segoe UI", Font.BOLD, 22));  // Aquí está en negrita
    campo.setForeground(Color.BLACK);
    campo.setBackground(Color.WHITE);  // Fondo blanco aunque no sea editable
    campo.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    campo.setCaretColor(Color.BLACK);
    // Esto ayuda a que no cambie el color cuando está no editable
    campo.setOpaque(true);
}

     public static void estiloEtiquetaGreen(JLabel etiqueta) {
           etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 18));
    etiqueta.setForeground(new Color(34, 139, 34)); // Verde moderno
    }
      public static void estiloEtiquetaRed(JLabel etiqueta) {
          etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 18));
          etiqueta.setForeground(new Color(220, 53, 69)); // Rojo moderno (tipo Bootstrap Danger)
    }
      
      public static void estiloEtiquetaBlue(JLabel etiqueta) {
    etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 18));
    etiqueta.setForeground(new Color(30, 144, 255)); // Azul moderno (DodgerBlue)
}

    public static void estiloTitulo(JLabel titulo) {
  //se implementa asi  ejemplo ->  Estilos.estiloEtiquetaGreen(lblVentas);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(new Color(0, 102, 204));
    }

    public static void estiloCampoTexto(JTextField campo) {
        campo.setFont(new Font("Arial", Font.PLAIN, 13));
        campo.setForeground(Color.BLACK);
    }
    //pantalla registro de empresa
public static void estiloCampo(JTextField campo) {
    campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    campo.setBackground(new Color(245, 245, 245)); // gris claro
    campo.setForeground(new Color(33, 33, 33)); // texto gris oscuro
    campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8) // padding interno
    ));
}
   public static void estiloBotonVerdeLima(JButton boton) {
    boton.setFont(new Font("Segoe UI", Font.BOLD, 16));
    boton.setForeground(Color.WHITE); // Letra blanca
    boton.setBackground(new Color(144, 199, 78)); // Verde lima
    boton.setFocusPainted(false);
    boton.setBorderPainted(false);
}
public static void estiloBotonAzul(JButton boton) {
    boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    boton.setBackground(new Color(0, 120, 215)); // azul moderno
    boton.setForeground(Color.WHITE);
    boton.setFocusPainted(false);
    boton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    boton.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            boton.setBackground(new Color(0, 150, 255));
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
            boton.setBackground(new Color(0, 120, 215));
        }
    });
}

     public static void estiloCalendario(JDateChooser calendario) {
    // Fuente general
    Font fuente = new Font("Segoe UI", Font.PLAIN, 14);
    calendario.setFont(fuente);

    // Editor de texto
    JTextField editor = (JTextField) calendario.getDateEditor().getUiComponent();
    editor.setFont(fuente);
    editor.setForeground(new Color(33, 37, 41));
    editor.setBackground(new Color(250, 250, 250));
    editor.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true), // borde redondeado
        BorderFactory.createEmptyBorder(5, 8, 5, 8) // padding interno
    ));

    // Fondo del calendario (por si afecta el botón o área)
    calendario.setBackground(new Color(245, 245, 245));
    calendario.setOpaque(false);

     
     
     }

    
         // Panel reutilizable con fondo blanco, curvatura y sombra suave
    public static class PanelConEstilo extends JPanel {

        public PanelConEstilo() {
            setOpaque(false); // Para permitir transparencia en la sombra
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 5, y = 5;
            int width = getWidth() - 10;
            int height = getHeight() - 10;

            // Sombra suave
            g2.setColor(new Color(0, 0, 0, 30)); // Sombra semi-transparente
            g2.fillRoundRect(x + 3, y + 3, width, height, 20, 20);

            // Fondo blanco con bordes redondeados
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, width, height, 20, 20);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    

  // estilos de menú lateral izquierdo con fondo blanco
public static void estiloBotonMenuLateral(JButton boton) {
    boton.setBackground(Color.WHITE); // Fondo blanco limpio
    boton.setForeground(Color.BLACK); // Texto negro moderno
    boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    boton.setFocusPainted(false);
    boton.setBorderPainted(false);
    boton.setHorizontalAlignment(SwingConstants.LEFT); // Alinea texto a la izquierda
    boton.setIconTextGap(15); // Espacio entre ícono y texto
    boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    boton.setMargin(new Insets(10, 20, 10, 10)); // Margen interno
}
public static void estiloBotonVerduleria(JButton boton) {
    boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    boton.setBackground(new Color(46, 139, 87)); // ✅ Verde inicial
    boton.setForeground(Color.WHITE);
    boton.setFocusPainted(false);
    boton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Hover effect
    boton.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            boton.setBackground(new Color(60, 179, 113)); // Verde más claro
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
            boton.setBackground(new Color(46, 139, 87)); // ✅ Vuelve al verde inicial
        }
    });
}


public static void estiloVerdura(Window frame, JButton boton, JTextField campo,
                                 JLabel etiquetaTitulo, JLabel etiquetaPrecio,
                                 JComboBox<String> combo) {
    // Fondo general de la ventana
    if (frame instanceof JDialog) {
        ((JDialog) frame).getContentPane().setBackground(new Color(240, 255, 240));
    } else if (frame instanceof JFrame) {
        ((JFrame) frame).getContentPane().setBackground(new Color(240, 255, 240));
    }

    // Botón principal
    boton.setFont(new Font("Segoe UI", Font.BOLD, 16));
    boton.setForeground(Color.WHITE);
    boton.setBackground(new Color(46, 139, 87));
    boton.setFocusPainted(false);
    boton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

    // Campo de texto
    campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    campo.setForeground(new Color(33, 37, 41));
    campo.setBackground(Color.WHITE);
    campo.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(46, 139, 87), 1, true),
        BorderFactory.createEmptyBorder(5, 8, 5, 8)
    ));

    // ComboBox
    combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    combo.setBackground(Color.WHITE);
    combo.setForeground(new Color(33, 37, 41));
    combo.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 1));
    combo.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Etiqueta título
    etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
    etiquetaTitulo.setForeground(new Color(34, 139, 34));

    // Etiqueta precio
    etiquetaPrecio.setFont(new Font("Segoe UI", Font.BOLD, 18));
    etiquetaPrecio.setForeground(new Color(0, 102, 204));
}



}


    

