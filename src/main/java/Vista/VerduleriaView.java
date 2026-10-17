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
import java.util.function.Consumer;
import Modelo.Productos;

public class VerduleriaView extends JDialog {

    private static final Color VERDE       = new Color(46, 125, 50);
    private static final Color VERDE_OSCURO  = new Color(27, 94, 32);
    private static final Color ROJO_CERRAR   = new Color(211, 47, 47);
    private static final Color FONDO_CARD    = Color.WHITE;
    private static final Color FONDO_HOVER   = new Color(230, 245, 230);

    private final Map<String, Productos> mapaProductos;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    // --- Header compartido ---
    private final JLabel lblTituloHeader = new JLabel("Verdulería");
    private final JLabel lblSubtituloHeader = new JLabel(" ");

    // --- Panel detalle ---
    private JLabel lblImagenDetalle;
    private JLabel lblPrecioKgDetalle;
    private JLabel lblTotalDetalle;
    private JTextField txtCantidad;
    private JButton btnAceptar;
    private JButton btnCerrar;

    private Productos productoActual;
    private static final double PASO_KG = 0.100;

    // callbacks que asigna el Controller
    private Consumer<Productos> onProductoAceptado;
    private Runnable onCerrar;

    public VerduleriaView(JFrame parent, List<String> productos, Map<String, Productos> mapaProductos) {
        super(parent, "Verdulería", true);
        this.mapaProductos = mapaProductos;

        setSize(780, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        add(construirHeader(), BorderLayout.NORTH);

        panelContenedor.add(construirPanelGrid(productos), "GRID");
        panelContenedor.add(construirPanelDetalle(), "DETALLE");
        add(panelContenedor, BorderLayout.CENTER);

        cardLayout.show(panelContenedor, "GRID");
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(VERDE);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        lblTituloHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloHeader.setForeground(Color.WHITE);
        lblSubtituloHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtituloHeader.setForeground(new Color(220, 255, 220));
        textos.add(lblTituloHeader);
        textos.add(lblSubtituloHeader);

        btnCerrar = new JButton("✕ Cerrar");
        btnCerrar.setBackground(ROJO_CERRAR);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnCerrar.addActionListener(e -> {
            if (onCerrar != null) onCerrar.run();
            dispose();
        });

        header.add(textos, BorderLayout.WEST);
        header.add(btnCerrar, BorderLayout.EAST);
        return header;
    }

    // ---------------------------------------------------------
    // GRID DE PRODUCTOS
    // ---------------------------------------------------------
   private JScrollPane construirPanelGrid(List<String> nombresProductos) {
        JPanel grid = new JPanel(new GridLayout(0, 4, 12, 12));
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(15, 15, 15, 15));

        System.out.println("DEBUG-GRID: Total de productos recibidos en la lista: " + (nombresProductos != null ? nombresProductos.size() : 0));

        for (String nombre : nombresProductos) {
            System.out.println("DEBUG-GRID: Buscando en mapa el producto -> [" + nombre + "]");
            Productos p = mapaProductos.get(nombre);
            if (p != null) {
                System.out.println("DEBUG-GRID: Producto encontrado con éxito, creando tarjeta para: " + p.getNombre());
                grid.add(crearTarjeta(p));
            } else {
                System.out.println("DEBUG-GRID: ¡ALERTA! El producto '" + nombre + "' NO se encontró en el mapaProductos.");
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel crearTarjeta(Productos p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(FONDO_CARD);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        card.setPreferredSize(new Dimension(150, 150));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblImagen = new JLabel(cargarImagen(p, 70), SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(150, 90));

        JLabel lblNombre = new JLabel(p.getNombre(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblPrecio = new JLabel(String.format("$%.2f /kg", p.getPrecioKg()), SwingConstants.CENTER);
        lblPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
    // PANEL DETALLE (precio/kg, stepper, aceptar)
    // ---------------------------------------------------------
    private JPanel construirPanelDetalle() {
        JPanel detalle = new JPanel(new BorderLayout(15, 15));
        detalle.setBorder(new EmptyBorder(20, 20, 20, 20));
        detalle.setBackground(Color.WHITE);

        lblImagenDetalle = new JLabel("", SwingConstants.CENTER);
        lblImagenDetalle.setPreferredSize(new Dimension(220, 220));
        lblImagenDetalle.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
        detalle.add(lblImagenDetalle, BorderLayout.WEST);

        JPanel derecha = new JPanel(new BorderLayout(10, 10));
        derecha.setBackground(Color.WHITE);

        JPanel precios = new JPanel(new GridLayout(1, 2));
        precios.setBackground(Color.WHITE);
        lblPrecioKgDetalle = new JLabel("Precio por kilo: $0.00");
        lblPrecioKgDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTotalDetalle = new JLabel("Total: $0.00");
        lblTotalDetalle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalDetalle.setForeground(VERDE_OSCURO);
        precios.add(lblPrecioKgDetalle);
        precios.add(lblTotalDetalle);
        derecha.add(precios, BorderLayout.NORTH);

        // Stepper
        JPanel stepper = new JPanel(new BorderLayout(8, 0));
        stepper.setBackground(Color.WHITE);

        JButton btnMenos = crearBotonStepper("−");
        JButton btnMas = crearBotonStepper("+");

        txtCantidad = new JTextField("1.000", 8);
        txtCantidad.setHorizontalAlignment(JTextField.CENTER);
        txtCantidad.setFont(new Font("Segoe UI", Font.BOLD, 20));

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
        derecha.add(stepper, BorderLayout.CENTER);

        btnAceptar = new JButton("✔ Aceptar (Enter)");
        btnAceptar.setBackground(VERDE);
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(new EmptyBorder(10, 20, 10, 20));
        derecha.add(btnAceptar, BorderLayout.SOUTH);

        detalle.add(derecha, BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnAceptar);

        return detalle;
    }

    private JButton crearBotonStepper(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(new Color(240, 240, 240));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(50, 50));
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

    /** Muestra la tarjeta de detalle para el producto seleccionado */
    public void mostrarDetalle(Productos p) {
        this.productoActual = p;
        lblSubtituloHeader.setText("Producto a detalle · " + p.getNombre());
        lblImagenDetalle.setIcon(cargarImagen(p, 200));
        lblPrecioKgDetalle.setText(String.format("Precio por kilo: $%.2f", p.getPrecioKg()));
        txtCantidad.setText("1.000");
        actualizarTotal();
        cardLayout.show(panelContenedor, "DETALLE");
        SwingUtilities.invokeLater(() -> { txtCantidad.requestFocusInWindow(); txtCantidad.selectAll(); });
    }

    /** Regresa a la cuadrícula de productos (sin cerrar el diálogo) */
    public void volverAGrid() {
        lblSubtituloHeader.setText(" ");
        productoActual = null;
        cardLayout.show(panelContenedor, "GRID");
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
        System.out.println("DEBUG-IMG: Intentando cargar imagen para producto: " + p.getNombre());
        try {
            Image img = null;

            // 1. Solo si el producto tiene imagen_url (está en catalogo_global),
            //    se intenta descargar la imagen real desde Cloudinary
            if (p.getImagenUrl() != null && !p.getImagenUrl().isEmpty()) {
                System.out.println("DEBUG-IMG -> URL Cloudinary encontrada: " + p.getImagenUrl());
                URL urlNet = new URL(p.getImagenUrl());
                img = Toolkit.getDefaultToolkit().createImage(urlNet);

                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(img, 0);
                try {
                    tracker.waitForID(0, 3000); // Esperar hasta 3 segundos la descarga
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (tracker.isErrorAny()) {
                    System.out.println("DEBUG-IMG -> Error de red/descarga para Cloudinary en: " + p.getNombre());
                    img = null;
                }
            } else {
                System.out.println("DEBUG-IMG -> El producto NO tiene imagen_url registrada (no está en catalogo_global). Se usará default.png");
            }

            // 2. Si no hay imagen_url o falló la descarga, se usa SIEMPRE default.png
            //    (ya no se busca imagen local por nombre de producto)
            if (img == null || img.getWidth(null) == -1) {
                System.out.println("DEBUG-IMG -> Usando default.png de respaldo");
                URL urlDefault = getClass().getResource("/Img/Verduleria/default.png");
                if (urlDefault != null) {
                    img = new ImageIcon(urlDefault).getImage();
                } else {
                    img = null;
                }
            }

            // 3. Redimensionar si se obtuvo una imagen válida
            if (img != null) {
                ImageIcon iconoTemporal = new ImageIcon(img);
                if (iconoTemporal.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    System.out.println("DEBUG-IMG -> ¡Imagen cargada y redimensionada con éxito para " + p.getNombre() + "!");
                    Image imgEscalada = iconoTemporal.getImage().getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH);
                    return new ImageIcon(imgEscalada);
                } else {
                    System.out.println("DEBUG-IMG -> La imagen se obtuvo pero el estado de carga no es COMPLETE.");
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG-IMG -> Excepción capturada al cargar imagen de " + p.getNombre() + ": " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("DEBUG-IMG -> Retornando ImageIcon vacío para: " + p.getNombre());
        return new ImageIcon();
    }

    // ---------------------------------------------------------
    // Getters para el Controller
    // ---------------------------------------------------------
    public JButton getBtnAceptar() { return btnAceptar; }
    public JButton getBtnCerrar() { return btnCerrar; }
    public JTextField getTxtCantidad() { return txtCantidad; }
    public Productos getProductoActual() { return productoActual; }
}