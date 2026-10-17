package Vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Map;
import Modelo.Productos;
import Modelo.BasculaListener;
import javax.swing.Timer;
public class VerduleriaPanel extends JPanel {

    private static final Color VERDE        = new Color(46, 125, 50);
    private static final Color VERDE_OSCURO = new Color(27, 94, 32);
    private static final Color FONDO_CARD   = Color.WHITE;
    private static final Color FONDO_HOVER  = new Color(230, 245, 230);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);
    private final JPanel gridProductos = new JPanel(new GridLayout(0, 3, 8, 8));

    private final JLabel lblSubtituloHeader = new JLabel(" ");
    private JLabel lblImagenDetalle;
    private JLabel lblPrecioKgDetalle;
    private JLabel lblTotalDetalle;
    private JTextField txtCantidad;
    private JButton btnAceptar;
    // borde naranja
    private JPanel panelDetalleRoot; // referencia al panel raíz del detalle, para el borde
    private Timer timerParpadeo;
    private static final Color NARANJA = new Color(255, 140, 0);
    private boolean bordeVisible = true;    
        //

    private Productos productoActual;
    private static final double PASO_KG = 0.100;

    // --- Integración con báscula (puerto serial) ---
    private BasculaListener bascula;
    private boolean mostrandoDetalle = false;      // controla si actualizamos txtCantidad con el peso
    private boolean basculaConectada = false;      // true solo si bascula.conectar() tuvo éxito
    private boolean avisoBasculaMostrado = false;  // evita repetir el modal en cada producto

    public VerduleriaPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        add(construirHeader(), BorderLayout.NORTH);

        JScrollPane scrollGrid = new JScrollPane(gridProductos);
        scrollGrid.setBorder(null);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);

        panelContenedor.add(scrollGrid, "GRID");
        panelContenedor.add(construirPanelDetalle(), "DETALLE");
        add(panelContenedor, BorderLayout.CENTER);

        gridProductos.setBackground(Color.WHITE);
        gridProductos.setBorder(new EmptyBorder(10, 10, 10, 10));

        cardLayout.show(panelContenedor, "GRID");
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(VERDE);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTitulo = new JLabel("Verdulería");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);

        lblSubtituloHeader.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtituloHeader.setForeground(new Color(220, 255, 220));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(lblSubtituloHeader);

        header.add(textos, BorderLayout.WEST);
        return header;
    }

    // ---------------------------------------------------------
    // Carga / recarga de productos en la cuadrícula
    // ---------------------------------------------------------
    public void cargarProductos(List<String> nombresProductos, Map<String, Productos> mapaProductos) {
        gridProductos.removeAll();
        for (String nombre : nombresProductos) {
            Productos p = mapaProductos.get(nombre);
            if (p != null) gridProductos.add(crearTarjeta(p));
        }
        gridProductos.revalidate();
        gridProductos.repaint();
        mostrarGrid();
    }

    private JPanel crearTarjeta(Productos p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(FONDO_CARD);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        card.setPreferredSize(new Dimension(100, 100));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblImagen = new JLabel(cargarImagen(p, 75), SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(100, 55));

        JLabel lblNombre = new JLabel(p.getNombre(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 10));

        JLabel lblPrecio = new JLabel(String.format("$%.2f/kg", p.getPrecioKg()), SwingConstants.CENTER);
        lblPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPrecio.setForeground(new Color(0, 130, 0));

        JPanel texto = new JPanel(new GridLayout(2, 1));
        texto.setBackground(FONDO_CARD);
        texto.add(lblNombre);
        texto.add(lblPrecio);

        card.add(lblImagen, BorderLayout.CENTER);
        card.add(texto, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { mostrarDetalle(p); }
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(FONDO_HOVER); texto.setBackground(FONDO_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(FONDO_CARD); texto.setBackground(FONDO_CARD);
            }
        });

        return card;
    }

    // ---------------------------------------------------------
    // PANEL DETALLE
    // ---------------------------------------------------------
    private JPanel construirPanelDetalle() {
    JPanel detalle = new JPanel(new BorderLayout(10, 10));
    detalle.setBackground(Color.WHITE);
    panelDetalleRoot = detalle; // 👈 guardamos referencia para el parpadeo
    aplicarBorde(false); // arranca sin resaltar

        lblImagenDetalle = new JLabel("", SwingConstants.CENTER);
        lblImagenDetalle.setPreferredSize(new Dimension(140, 140));
        lblImagenDetalle.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        detalle.add(lblImagenDetalle, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 4));
        centro.setBackground(Color.WHITE);
        lblPrecioKgDetalle = new JLabel("Precio por kilo: $0.00", SwingConstants.CENTER);
        lblPrecioKgDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalDetalle = new JLabel("Total: $0.00", SwingConstants.CENTER);
        lblTotalDetalle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalDetalle.setForeground(VERDE_OSCURO);
        centro.add(lblPrecioKgDetalle);
        centro.add(lblTotalDetalle);
        detalle.add(centro, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(6, 6));
        abajo.setBackground(Color.WHITE);

        JPanel stepper = new JPanel(new BorderLayout(4, 0));
        stepper.setBackground(Color.WHITE);

        JButton btnMenos = crearBotonStepper("−");
        JButton btnMas = crearBotonStepper("+");

        txtCantidad = new JTextField("0.000", 6);
        txtCantidad.setHorizontalAlignment(JTextField.CENTER);
        txtCantidad.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnMenos.addActionListener(e -> cambiarCantidad(-PASO_KG));
        btnMas.addActionListener(e -> cambiarCantidad(PASO_KG));

        txtCantidad.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarTotal(); }
            public void removeUpdate(DocumentEvent e) { actualizarTotal(); }
            public void changedUpdate(DocumentEvent e) { actualizarTotal(); }
        });

        stepper.add(btnMenos, BorderLayout.WEST);
        stepper.add(txtCantidad, BorderLayout.CENTER);
        stepper.add(btnMas, BorderLayout.EAST);
        abajo.add(stepper, BorderLayout.NORTH);

        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> mostrarGrid());

        btnAceptar = new JButton("✔ Aceptar (Enter)");
        btnAceptar.setBackground(VERDE);
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setFocusPainted(false);

        JPanel botones = new JPanel(new GridLayout(1, 2, 6, 0));
        botones.setBackground(Color.WHITE);
        botones.add(btnVolver);
        botones.add(btnAceptar);
        abajo.add(botones, BorderLayout.SOUTH);

        detalle.add(abajo, BorderLayout.SOUTH);

        return detalle;
    }

    private JButton crearBotonStepper(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(new Color(240, 240, 240));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(36, 36));
        return b;
    }

    private void cambiarCantidad(double delta) {
        double actual = parseCantidad();
        double nueva = Math.max(0, actual + delta);
        txtCantidad.setText(String.format("%.3f", nueva));
    }

    private double parseCantidad() {
        try {
            return Double.parseDouble(txtCantidad.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void actualizarTotal() {
        if (productoActual == null) return;
        double cantidad = parseCantidad();
        double total = productoActual.getPrecioKg() * cantidad;
        lblTotalDetalle.setText(String.format("Total: $%.2f", total));
    }

    public void mostrarDetalle(Productos p) {
    this.productoActual = p;
    this.mostrandoDetalle = true;
    lblSubtituloHeader.setText(String.format("%s  ·  $%.2f/kg", p.getNombre(), p.getPrecioKg()));
    lblImagenDetalle.setIcon(cargarImagen(p, 130));
    lblPrecioKgDetalle.setText(String.format("Precio por kilo: $%.2f", p.getPrecioKg()));
    txtCantidad.setText("1.000");
    actualizarTotal();
    cardLayout.show(panelContenedor, "DETALLE");
    iniciarParpadeoDetalle(); // 👈 agregar esta línea

        if (!basculaConectada) {
    SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(
            this,
            "No se detectó ninguna báscula conectada.\n" +
            "Podrás ingresar el peso manualmente con los botones +/- o escribiéndolo directamente.",
            "Báscula no conectada",
            JOptionPane.WARNING_MESSAGE
        );
        txtCantidad.requestFocusInWindow();
        txtCantidad.selectAll();
    });
} else {
    SwingUtilities.invokeLater(() -> {
        txtCantidad.requestFocusInWindow();
        txtCantidad.selectAll();
    });
}

        getRootPaneAncestor();
    }

    /** Regresa a la cuadrícula de productos, sin ocultar el panel */
    public void mostrarGrid() {
    lblSubtituloHeader.setText(" ");
    productoActual = null;
    mostrandoDetalle = false;
    detenerParpadeoDetalle(); // 👈 agregar esta línea
    cardLayout.show(panelContenedor, "GRID");
}
private void aplicarBorde(boolean resaltado) {
    if (panelDetalleRoot == null) return;
    if (resaltado) {
        panelDetalleRoot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NARANJA, 4),
            new EmptyBorder(8, 8, 8, 8)
        ));
    } else {
        panelDetalleRoot.setBorder(new EmptyBorder(12, 12, 12, 12));
    }
}

private void iniciarParpadeoDetalle() {
    detenerParpadeoDetalle(); // por si ya había uno corriendo
    bordeVisible = true;
    timerParpadeo = new Timer(500, e -> {
        bordeVisible = !bordeVisible;
        aplicarBorde(bordeVisible);
    });
    timerParpadeo.start();
}

private void detenerParpadeoDetalle() {
    if (timerParpadeo != null) {
        timerParpadeo.stop();
        timerParpadeo = null;
    }
    aplicarBorde(false);
}
    /** Reinicia el aviso de "báscula no conectada" (llámalo al abrir una nueva venta) */
    public void resetAvisoBascula() {
        avisoBasculaMostrado = false;
    }

    private void getRootPaneAncestor() {
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) root.setDefaultButton(btnAceptar);
    }

    /**
     * Carga la imagen de un producto.
     *
     * Regla de negocio: SOLO se muestra una imagen real si el producto tiene
     * "imagen_url" (es decir, está enlazado a catalogo_global y fue dado de
     * alta con imagen). Si el producto NO tiene imagen_url (no está dado de
     * alta en catalogo_global), se muestra SIEMPRE default.png.
     *
     * IMPORTANTE: ya NO se busca un archivo local por el nombre del producto
     * (antes: /Img/Verduleria/NOMBRE.png). Esa búsqueda hacía que productos
     * sin imagen_url (ej. "cebolla") mostraran igual una imagen porque
     * casualmente existía un archivo local con ese nombre. Eso quedó
     * eliminado a propósito.
     */
  private ImageIcon cargarImagen(Productos p, int tamano) {
    // Solo si tiene imagen_url se usa la imagen real (desde caché o internet, una sola vez)
    ImageIcon icono = Utilidades.CacheImagenes.icono(p.getImagenUrl(), tamano);
    if (icono != null) return icono;

    // Sin imagen_url o sin poder descargarla → default.png
    URL urlDefault = getClass().getResource("/Img/Verduleria/default.png");
    if (urlDefault != null) {
        Image img = new ImageIcon(urlDefault).getImage();
        return new ImageIcon(img.getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH));
    }
    return new ImageIcon();
}
    // ---------------------------------------------------------
    // BÁSCULA (puerto serial)
    // ---------------------------------------------------------

    /**
     * Conecta el panel a una báscula por puerto serial (real o simulada vía
     * VSPE). Si el puerto no existe o falla la conexión, simplemente no se
     * activa la lectura automática y el usuario puede seguir usando los
     * botones +/- o escribir la cantidad a mano.
     *
     * Solo actualiza txtCantidad cuando el panel está mostrando el detalle
     * de un producto (mostrandoDetalle == true), para no pisar nada mientras
     * se está viendo el grid.
     */
 public void conectarBascula(String nombrePuerto) {
    bascula = new BasculaListener(
        nombrePuerto,
        pesoKg -> {
            SwingUtilities.invokeLater(() -> {
                if (mostrandoDetalle && productoActual != null) {
                    txtCantidad.setText(String.format("%.3f", pesoKg));
                }
            });
        },
        conectada -> {
            // Se ejecuta desde el hilo de reintento, hay que pasar a EDT
            SwingUtilities.invokeLater(() -> {
                basculaConectada = conectada;
                System.out.println("[BASCULA] Estado de conexión actualizado -> " + conectada);
            });
        }
    );

    // 👇 Ya no es un intento único: reintenta cada 2.5s indefinidamente en background.
    // No bloquea la UI, así que la ventana de Nueva Venta se abre normal aunque
    // la báscula/VSPE todavía no esté lista.
    bascula.iniciarConReintento(2500, -1);
}
    /** Libera el puerto serial. Llama esto al cerrar la pantalla de ventas o la app. */
    public void desconectarBascula() {
        if (bascula != null) bascula.desconectar();
    }

    public JButton getBtnAceptar() { return btnAceptar; }
    public JTextField getTxtCantidad() { return txtCantidad; }
    public Productos getProductoActual() { return productoActual; }
    public boolean isBasculaConectada() { return basculaConectada; }
}