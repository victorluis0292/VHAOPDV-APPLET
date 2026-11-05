package Vista;

import Estilos.EstiloPromociones;
import Estilos.Estilos;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.Promocion;
import Modelo.PromocionDao;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class FrmPromociones extends JDialog {

    private static final ZoneId ZONA = ZoneId.of("America/Monterrey");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int idEmpresa;
    private final ProductosDao proDao;
    private final PromocionDao promoDao = new PromocionDao();

    private List<Productos> catalogo = new ArrayList<>();
    private List<Promocion> promos = new ArrayList<>();
    private Promocion promoEditando = null;

    // Formulario
    private JLabel lblTituloForm, lblResumen, lblProductos;
    private JTextField txtDescripcion, txtCantidad, txtPrecioPaquete;
    private JDateChooser dcInicio, dcFin;
    private JButton btnGuardar, btnNueva, btnEliminar, btnQuitarProd;

    // Buscador de productos (campo + popup de sugerencias)
    private EstiloPromociones.CampoBusqueda txtBuscarProd;
    private final DefaultListModel<ItemProd> modeloSugerencias = new DefaultListModel<>();
    private JList<ItemProd> listaSugerencias;
    private JScrollPane scrollSugerencias;
    private JPopupMenu popupSugerencias;

    // Productos agregados a la promoción
    private final DefaultListModel<ItemProd> modeloProductos = new DefaultListModel<>();
    private JList<ItemProd> listaProductos;

    // Tabla
    private DefaultTableModel modeloTabla;
    private JTable tabla;

    // ------------------------------------------------------------
    // Producto para las listas
    // ------------------------------------------------------------
    private static class ItemProd implements EstiloPromociones.ItemVisual {
        final int id;
        final String codigo;
        final String nombre;
        final double precio;
        final boolean vacio;

        ItemProd(Productos p) {
            this.id = p.getId();
            this.codigo = p.getCodigo() == null ? "" : p.getCodigo();
            this.nombre = p.getNombre() == null ? "" : p.getNombre();
            this.precio = p.getPrecio();
            this.vacio = false;
        }

        // Fila "Sin resultados"
        ItemProd() {
            this.id = -1;
            this.codigo = "";
            this.nombre = "";
            this.precio = 0;
            this.vacio = true;
        }

        @Override public String titulo()  { return nombre; }
        @Override public String detalle() { return "Código: " + codigo; }
        @Override public String precio()  { return String.format("$%.2f", precio); }
        @Override public boolean vacio()  { return vacio; }

        @Override
        public String toString() {   // lo usa la lista de productos agregados
            return codigo + "  |  " + nombre + "  |  " + String.format("$%.2f", precio);
        }
    }

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------
    public FrmPromociones(Frame parent, int idEmpresa, ProductosDao proDao) {
        super(parent, "Promociones", true);
        this.idEmpresa = idEmpresa;
        this.proDao = proDao;

        setSize(1600, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        EstiloPromociones.ventana(this);

        List<Productos> lista = proDao.ListarProductos(idEmpresa);
        if (lista != null) catalogo = lista;

        add(crearFormulario(), BorderLayout.WEST);
        add(crearTabla(), BorderLayout.CENTER);

        listar();
        limpiar();
    }

    // ------------------------------------------------------------
    // Formulario (izquierda)
    // ------------------------------------------------------------
    private JPanel crearFormulario() {
        Estilos.PanelConEstilo panel = new Estilos.PanelConEstilo();
        panel.setPreferredSize(new Dimension(460, 0));

        JPanel c = new JPanel(new GridBagLayout());
        c.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int fila = 0;

        lblTituloForm = new JLabel("Nueva promoción");
        EstiloPromociones.titulo(lblTituloForm);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        c.add(lblTituloForm, gbc);

        txtDescripcion = new JTextField();
        txtCantidad = new JTextField("2");
        txtPrecioPaquete = new JTextField();
        Estilos.estiloCampo(txtDescripcion);
        Estilos.estiloCampo(txtCantidad);
        Estilos.estiloCampo(txtPrecioPaquete);

        soloNumeros(txtCantidad, false);       // piezas: solo enteros
        soloNumeros(txtPrecioPaquete, true);   // precio: decimales (máx. 2)

        dcInicio = crearFecha();
        dcFin = crearFecha();

        agregarCampo(c, "Descripción:", txtDescripcion, gbc, fila++);
        agregarCampo(c, "Piezas del paquete:", txtCantidad, gbc, fila++);
        agregarCampo(c, "Precio del paquete:", txtPrecioPaquete, gbc, fila++);
        agregarCampo(c, "Fecha inicio:", dcInicio, gbc, fila++);
        agregarCampo(c, "Fecha fin:", dcFin, gbc, fila++);

        // --- Productos de la promo (ocupa las 2 columnas) ---
        lblProductos = new JLabel("Productos de la promoción (0)");
        EstiloPromociones.etiqueta(lblProductos);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        c.add(lblProductos, gbc);

        txtBuscarProd = new EstiloPromociones.CampoBusqueda("Busca por código o nombre y presiona Enter");
        txtBuscarProd.setToolTipText("Escribe código o nombre. ↑ ↓ para moverte, Enter para agregar");
        gbc.gridy = fila++;
        c.add(txtBuscarProd, gbc);
        crearPopupSugerencias();

        listaProductos = new JList<>(modeloProductos);
        EstiloPromociones.listaProductos(listaProductos);
        JScrollPane scrollLista = new JScrollPane(listaProductos);
        EstiloPromociones.scroll(scrollLista);
        gbc.gridy = fila++;
        gbc.ipady = 90;          // alto de la lista
        c.add(scrollLista, gbc);
        gbc.ipady = 0;

        btnQuitarProd = new JButton("Quitar seleccionado");
        Estilos.estiloBotonAzul(btnQuitarProd);
        gbc.gridy = fila++;
        c.add(btnQuitarProd, gbc);

        lblResumen = new JLabel(" ");
        lblResumen.setFont(EstiloPromociones.F_ETIQUETA);
        gbc.gridy = fila++;
        c.add(lblResumen, gbc);

        // --- Botones ---
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 6));
        botones.setOpaque(false);
        btnGuardar = new JButton("Guardar");
        btnNueva = new JButton("Nueva");
        btnEliminar = new JButton("Eliminar");
        Estilos.estiloBotonAzul(btnGuardar);
        Estilos.estiloBotonAzul(btnNueva);
        Estilos.estiloBotonAzul(btnEliminar);
        botones.add(btnGuardar);
        botones.add(btnNueva);
        botones.add(btnEliminar);

        gbc.gridy = fila++;
        gbc.insets = new Insets(12, 5, 5, 5);
        c.add(botones, gbc);

        gbc.gridy = fila;
        gbc.weighty = 1.0;
        c.add(Box.createVerticalGlue(), gbc);

        panel.add(c, BorderLayout.CENTER);

        // --- Eventos ---
        txtBuscarProd.getDocument().addDocumentListener(simple(this::mostrarSugerencias));
        txtBuscarProd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        moverSeleccion(1);
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        moverSeleccion(-1);
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        agregarSeleccionado();
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        popupSugerencias.setVisible(false);
                        break;
                    default:
                        break;
                }
            }
        });
        txtBuscarProd.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                popupSugerencias.setVisible(false);
            }
        });

        txtCantidad.getDocument().addDocumentListener(simple(this::actualizarResumen));
        txtPrecioPaquete.getDocument().addDocumentListener(simple(this::actualizarResumen));

        btnQuitarProd.addActionListener(e -> {
            int i = listaProductos.getSelectedIndex();
            if (i >= 0) {
                modeloProductos.remove(i);
                actualizarResumen();
            }
        });
        btnGuardar.addActionListener(e -> guardar());
        btnNueva.addActionListener(e -> limpiar());
        btnEliminar.addActionListener(e -> eliminar());

        return panel;
    }

    // ------------------------------------------------------------
    // Tabla (derecha)
    // ------------------------------------------------------------
    private JPanel crearTabla() {
        Estilos.PanelConEstilo panel = new Estilos.PanelConEstilo();

        JLabel titulo = new JLabel("Promociones registradas");
        EstiloPromociones.titulo(titulo);

        String[] columnas = {"ID", "DESCRIPCIÓN", "PIEZAS", "PRECIO PAQUETE", "INICIO", "FIN", "PRODUCTOS", "ESTADO"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        Estilos.estiloTabla(tabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(30);

        int[] anchos = {50, 200, 70, 120, 100, 100, 320, 100};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // Color del estado
        tabla.getColumnModel().getColumn(7).setCellRenderer(EstiloPromociones.rendererEstado());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0 && fila < promos.size()) cargarEnFormulario(promos.get(fila));
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        EstiloPromociones.scroll(scroll);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------
    // Utilidades de interfaz
    // ------------------------------------------------------------
    private void agregarCampo(JPanel panel, String texto, JComponent comp, GridBagConstraints gbc, int fila) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;

        JLabel label = new JLabel(texto);
        EstiloPromociones.etiqueta(label);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private JDateChooser crearFecha() {
        JDateChooser dc = new JDateChooser();
        EstiloPromociones.fecha(dc);
        return dc;
    }

    private void soloNumeros(JTextField campo, boolean permitirDecimal) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean valido(String t) {
                // Solo dígitos, y opcionalmente un punto con hasta 2 decimales
                return permitirDecimal ? t.matches("\\d*(\\.\\d{0,2})?") : t.matches("\\d*");
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String str, AttributeSet a)
                    throws BadLocationException {
                replace(fb, offset, 0, str, a);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet a)
                    throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, offset)
                        + (text == null ? "" : text)
                        + actual.substring(offset + length);
                if (valido(nuevo)) {
                    super.replace(fb, offset, length, text, a);
                }
            }
        });
    }

    private DocumentListener simple(Runnable accion) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        };
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Promociones", JOptionPane.WARNING_MESSAGE);
    }

    private LocalDate aLocalDate(Date d) {
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date aDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ------------------------------------------------------------
    // Buscador de productos: popup de sugerencias
    // ------------------------------------------------------------
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void crearPopupSugerencias() {
        listaSugerencias = new JList<>(modeloSugerencias);
        EstiloPromociones.listaSugerencias((JList) listaSugerencias);

        // Clic: agrega el producto
        listaSugerencias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int i = listaSugerencias.locationToIndex(e.getPoint());
                if (i >= 0) {
                    listaSugerencias.setSelectedIndex(i);
                    agregarSeleccionado();
                }
            }
        });
        // Hover: resalta la fila bajo el mouse
        listaSugerencias.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = listaSugerencias.locationToIndex(e.getPoint());
                if (i >= 0) listaSugerencias.setSelectedIndex(i);
            }
        });

        scrollSugerencias = new JScrollPane(listaSugerencias);
        EstiloPromociones.scroll(scrollSugerencias);
        popupSugerencias = EstiloPromociones.crearPopup(scrollSugerencias);
    }

    private boolean yaEstaAgregado(int id) {
        for (int i = 0; i < modeloProductos.size(); i++) {
            if (modeloProductos.get(i).id == id) return true;
        }
        return false;
    }

    private void mostrarSugerencias() {
        String t = txtBuscarProd.getText().trim().toLowerCase();
        modeloSugerencias.clear();
        if (t.isEmpty() || !txtBuscarProd.isShowing()) {
            popupSugerencias.setVisible(false);
            return;
        }

        int n = 0;
        for (Productos p : catalogo) {
            if (yaEstaAgregado(p.getId())) continue;   // no sugerir los ya agregados
            String cod = p.getCodigo() == null ? "" : p.getCodigo().toLowerCase();
            String nom = p.getNombre() == null ? "" : p.getNombre().toLowerCase();
            if (cod.contains(t) || nom.contains(t)) {
                modeloSugerencias.addElement(new ItemProd(p));
                if (++n >= 30) break;
            }
        }
        if (n == 0) modeloSugerencias.addElement(new ItemProd());   // "Sin resultados"
        else listaSugerencias.setSelectedIndex(0);

        int filas = Math.min(modeloSugerencias.size(), 6);
        scrollSugerencias.setPreferredSize(
                new Dimension(txtBuscarProd.getWidth(), filas * EstiloPromociones.ALTO_FILA + 2));

        if (popupSugerencias.isVisible()) popupSugerencias.pack();
        else popupSugerencias.show(txtBuscarProd, 0, txtBuscarProd.getHeight());
    }

    private void moverSeleccion(int delta) {
        if (!popupSugerencias.isVisible()) {
            if (delta > 0) mostrarSugerencias();
            return;
        }
        int n = modeloSugerencias.size();
        if (n == 0) return;
        int i = Math.max(0, Math.min(n - 1, listaSugerencias.getSelectedIndex() + delta));
        listaSugerencias.setSelectedIndex(i);
        listaSugerencias.ensureIndexIsVisible(i);
    }

    private void agregarSeleccionado() {
        ItemProd it = listaSugerencias.getSelectedValue();
        if (!popupSugerencias.isVisible() || it == null || it.vacio) return;

        if (yaEstaAgregado(it.id)) {
            aviso("Ese producto ya está en la promoción.");
            return;
        }

        modeloProductos.addElement(it);
        txtBuscarProd.setText("");              // limpia y oculta el popup
        popupSugerencias.setVisible(false);
        actualizarResumen();
        txtBuscarProd.requestFocusInWindow();   // listo para el siguiente producto
    }

    private void actualizarResumen() {
        lblProductos.setText("Productos de la promoción (" + modeloProductos.size() + ")");

        if (modeloProductos.isEmpty()) {
            lblResumen.setText(" ");
            return;
        }
        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            double paquete = Double.parseDouble(txtPrecioPaquete.getText().trim());

            double min = Double.MAX_VALUE;
            for (int i = 0; i < modeloProductos.size(); i++) {
                min = Math.min(min, modeloProductos.get(i).precio);
            }
            double normal = min * cant;
            double ahorro = normal - paquete;

            if (ahorro > 0) {
                EstiloPromociones.resumenConviene(lblResumen,
                        String.format("Normal: $%.2f  |  Paquete: $%.2f  |  Ahorro: $%.2f", normal, paquete, ahorro));
            } else {
                EstiloPromociones.resumenNoConviene(lblResumen,
                        "El paquete no conviene: cuesta igual o más que el precio normal.");
            }
        } catch (NumberFormatException ex) {
            lblResumen.setText(" ");
        }
    }

    // ------------------------------------------------------------
    // Tabla: listar y cargar en el formulario
    // ------------------------------------------------------------
    private String estado(Promocion p, LocalDate hoy) {
        if (!p.isActiva()) return "Inactiva";
        if (hoy.isBefore(p.getFechaInicio())) return "Programada";
        if (hoy.isAfter(p.getFechaFin())) return "Vencida";
        return "Vigente";
    }

    private void listar() {
        promos = promoDao.listar(idEmpresa);
        modeloTabla.setRowCount(0);
        LocalDate hoy = LocalDate.now(ZONA);
        for (Promocion p : promos) {
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getDescripcion(),
                p.getCantidadRequerida(),
                String.format("$%.2f", p.getPrecioPaquete()),
                p.getFechaInicio().format(FMT),
                p.getFechaFin().format(FMT),
                p.getNombresProductos(),
                estado(p, hoy)
            });
        }
    }

    private void cargarEnFormulario(Promocion p) {
        promoEditando = p;
        lblTituloForm.setText("Editando promoción #" + p.getId());
        txtDescripcion.setText(p.getDescripcion());
        txtCantidad.setText(String.valueOf(p.getCantidadRequerida()));
        txtPrecioPaquete.setText(String.format("%.2f", p.getPrecioPaquete()));
        dcInicio.setDate(aDate(p.getFechaInicio()));
        dcFin.setDate(aDate(p.getFechaFin()));

        modeloProductos.clear();
        for (int idProd : p.getIdProductos()) {
            for (Productos prod : catalogo) {
                if (prod.getId() == idProd) {
                    modeloProductos.addElement(new ItemProd(prod));
                    break;
                }
            }
        }
        actualizarResumen();
    }

    private void limpiar() {
        promoEditando = null;
        lblTituloForm.setText("Nueva promoción");
        txtDescripcion.setText("");
        txtCantidad.setText("2");
        txtPrecioPaquete.setText("");
        txtBuscarProd.setText("");
        LocalDate hoy = LocalDate.now(ZONA);
        dcInicio.setDate(aDate(hoy));
        dcFin.setDate(aDate(hoy));
        modeloProductos.clear();
        actualizarResumen();
        tabla.clearSelection();
        txtDescripcion.requestFocus();
    }

    // ------------------------------------------------------------
    // Guardar / eliminar
    // ------------------------------------------------------------
    private void guardar() {
        String desc = txtDescripcion.getText().trim();
        if (desc.isEmpty()) {
            aviso("Escribe una descripción para la promoción.");
            return;
        }

        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            precio = Double.parseDouble(txtPrecioPaquete.getText().trim());
        } catch (NumberFormatException ex) {
            aviso("Las piezas y el precio deben ser números válidos.");
            return;
        }
        if (cantidad < 1) {
            aviso("Las piezas del paquete deben ser 1 o más (1 = precio especial por pieza).");
            return;
        }
        if (precio <= 0) {
            aviso("El precio del paquete debe ser mayor a cero.");
            return;
        }

        LocalDate ini = aLocalDate(dcInicio.getDate());
        LocalDate fin = aLocalDate(dcFin.getDate());
        if (ini == null || fin == null) {
            aviso("Selecciona la fecha de inicio y la fecha de fin.");
            return;
        }
        if (fin.isBefore(ini)) {
            aviso("La fecha de fin no puede ser anterior a la de inicio.");
            return;
        }
        if (modeloProductos.isEmpty()) {
            aviso("Agrega al menos un producto a la promoción.");
            return;
        }

        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < modeloProductos.size(); i++) ids.add(modeloProductos.get(i).id);

        // Un producto solo puede estar en una promoción activa en las mismas fechas
        int excluir = promoEditando == null ? 0 : promoEditando.getId();
        for (int i = 0; i < modeloProductos.size(); i++) {
            ItemProd it = modeloProductos.get(i);
            String conflicto = promoDao.promoEnConflicto(idEmpresa, it.id, ini, fin, excluir);
            if (conflicto != null) {
                aviso("El producto \"" + it.nombre + "\" ya está en la promoción \"" + conflicto
                        + "\" en esas fechas.\nQuítalo de una de las dos o cambia las fechas.");
                return;
            }
        }

        // Aviso si el paquete no conviene
        double min = Double.MAX_VALUE;
        for (int i = 0; i < modeloProductos.size(); i++) min = Math.min(min, modeloProductos.get(i).precio);
        if (min * cantidad - precio <= 0) {
            int r = JOptionPane.showConfirmDialog(this,
                    "El paquete cuesta igual o más que el precio normal, así que nunca se aplicará.\n¿Guardar de todos modos?",
                    "Promociones", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }

        Promocion p = new Promocion();
        p.setIdEmpresa(idEmpresa);
        p.setDescripcion(desc);
        p.setCantidadRequerida(cantidad);
        p.setPrecioPaquete(precio);
        p.setFechaInicio(ini);
        p.setFechaFin(fin);
        p.setIdProductos(ids);

        boolean ok;
        if (promoEditando == null) {
            ok = promoDao.guardar(p);
        } else {
            p.setId(promoEditando.getId());
            p.setActiva(promoEditando.isActiva());
            ok = promoDao.actualizar(p);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Promoción guardada.");
            listar();
            limpiar();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la promoción. Revisa el log.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (promoEditando == null) {
            aviso("Selecciona una promoción de la tabla para eliminar.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la promoción \"" + promoEditando.getDescripcion() + "\"?",
                "Promociones", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        int resultado = promoDao.eliminar(promoEditando.getId(), idEmpresa);
        if (resultado == 1) {
            JOptionPane.showMessageDialog(this, "Promoción eliminada.");
        } else if (resultado == 2) {
            JOptionPane.showMessageDialog(this,
                    "Esta promoción ya se usó en ventas, así que se desactivó en lugar de borrarse.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la promoción.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        listar();
        limpiar();
    }
}