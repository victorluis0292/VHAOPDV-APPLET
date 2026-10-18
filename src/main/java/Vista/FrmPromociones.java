package Vista;

import Estilos.Estilos;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.Promocion;
import Modelo.PromocionDao;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.table.DefaultTableCellRenderer;
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

    private JLabel lblTituloForm, lblResumen;
    private JTextField txtDescripcion, txtCantidad, txtPrecioPaquete, txtBuscarProd;
    private JDateChooser dcInicio, dcFin;
    private JComboBox<ItemProd> cbxProductos;
    private final DefaultListModel<ItemProd> modeloProductos = new DefaultListModel<>();
    private JList<ItemProd> listaProductos;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JButton btnGuardar, btnNueva, btnEliminar, btnAgregarProd, btnQuitarProd;

    // ------------------------------------------------------------
    // Producto para el combo y la lista
    // ------------------------------------------------------------
   private static class ItemProd {
    final int id;
    final String codigo;
    final String nombre;
    final double precio;
    final boolean placeholder;

    ItemProd(Productos p) {
        this.id = p.getId();
        this.codigo = p.getCodigo() == null ? "" : p.getCodigo();
        this.nombre = p.getNombre() == null ? "" : p.getNombre();
        this.precio = p.getPrecio();
        this.placeholder = false;
    }

    // Ítem "Seleccione"
    ItemProd() {
        this.id = -1;
        this.codigo = "";
        this.nombre = "Seleccione";
        this.precio = 0;
        this.placeholder = true;
    }

    @Override
    public String toString() {
        if (placeholder) return "Seleccione";
        return codigo + "  |  " + nombre + "  |  $" + String.format("%.2f", precio);
    }
}
    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------
    public FrmPromociones(Frame parent, int idEmpresa, ProductosDao proDao) {
        super(parent, "Promociones", true);
        this.idEmpresa = idEmpresa;
        this.proDao = proDao;

        setSize(1180, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(12, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        List<Productos> lista = proDao.ListarProductos(idEmpresa);
        if (lista != null) catalogo = lista;

        add(crearFormulario(), BorderLayout.WEST);
        add(crearTabla(), BorderLayout.CENTER);

        filtrarProductos("");
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
        lblTituloForm.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloForm.setForeground(new Color(0, 102, 204));
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

        // --- Selector de productos (ocupa las 2 columnas) ---
        JLabel lblProd = new JLabel("Productos de la promo (cualquiera de ellos cuenta):");
        lblProd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProd.setForeground(new Color(70, 70, 70));
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        c.add(lblProd, gbc);

        txtBuscarProd = new JTextField();
        Estilos.estiloCampo(txtBuscarProd);
        txtBuscarProd.setToolTipText("Escribe código o nombre para filtrar");

        JLabel lupa = new JLabel(iconoLupa());
        lupa.setBorder(new EmptyBorder(0, 8, 0, 0));
        txtBuscarProd.setLayout(new BorderLayout());
        txtBuscarProd.add(lupa, BorderLayout.WEST);
        txtBuscarProd.setMargin(new Insets(2, 32, 2, 6));

        cbxProductos = new JComboBox<>();
        cbxProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbxProductos.setPreferredSize(new Dimension(0, 34));
        
                cbxProductos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean foc) {
                Component comp = super.getListCellRendererComponent(list, value, index, sel, foc);
                if (value instanceof ItemProd && ((ItemProd) value).placeholder && !sel) {
                    comp.setForeground(Color.GRAY);
                }
                return comp;
            }
        });
        
        btnAgregarProd = new JButton("Agregar a la promoción");
        Estilos.estiloBotonAzul(btnAgregarProd);

        JPanel buscador = new JPanel(new GridLayout(3, 1, 0, 6));
        buscador.setOpaque(false);
        buscador.add(txtBuscarProd);
        buscador.add(cbxProductos);
        buscador.add(btnAgregarProd);
        gbc.gridy = fila++;
        c.add(buscador, gbc);

        listaProductos = new JList<>(modeloProductos);
        listaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollLista = new JScrollPane(listaProductos);
    gbc.gridy = fila++;
    gbc.ipady = 90;          // alto de la lista
    c.add(scrollLista, gbc);
    gbc.ipady = 0;

        btnQuitarProd = new JButton("Quitar seleccionado");
        Estilos.estiloBotonAzul(btnQuitarProd);
        gbc.gridy = fila++;
        c.add(btnQuitarProd, gbc);

        lblResumen = new JLabel(" ");
        lblResumen.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        txtBuscarProd.getDocument().addDocumentListener(simple(() -> filtrarProductos(txtBuscarProd.getText())));
        txtCantidad.getDocument().addDocumentListener(simple(this::actualizarResumen));
        txtPrecioPaquete.getDocument().addDocumentListener(simple(this::actualizarResumen));

        btnAgregarProd.addActionListener(e -> agregarProducto());
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
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(0, 102, 204));

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
        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String s = v == null ? "" : v.toString();
                    if (s.equals("Vigente")) comp.setForeground(new Color(0, 128, 0));
                    else if (s.equals("Programada")) comp.setForeground(new Color(200, 120, 0));
                    else comp.setForeground(Color.GRAY);
                }
                return comp;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0 && fila < promos.size()) cargarEnFormulario(promos.get(fila));
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    
    private Icon iconoLupa() {
    java.net.URL url = getClass().getResource("/resources/Img/lupa.png");
    if (url == null) {
        return new ImageIcon(); // evita errores si no encuentra la imagen
    }
    Image img = new ImageIcon(url).getImage()
            .getScaledInstance(16, 16, Image.SCALE_SMOOTH);
    return new ImageIcon(img);
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(70, 70, 70));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private JDateChooser crearFecha() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(0, 34));
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
    // Productos: filtrar, agregar, resumen
    // ------------------------------------------------------------
    private void filtrarProductos(String texto) {
    String t = texto == null ? "" : texto.trim().toLowerCase();
    cbxProductos.removeAllItems();
    cbxProductos.addItem(new ItemProd());   // "Seleccione" siempre primero
    int n = 0;
    for (Productos p : catalogo) {
        String cod = p.getCodigo() == null ? "" : p.getCodigo().toLowerCase();
        String nom = p.getNombre() == null ? "" : p.getNombre().toLowerCase();
        if (t.isEmpty() || cod.contains(t) || nom.contains(t)) {
            cbxProductos.addItem(new ItemProd(p));
            if (++n >= 200) break;
        }
    }
    cbxProductos.setSelectedIndex(0);
}

    private void agregarProducto() {
        ItemProd item = (ItemProd) cbxProductos.getSelectedItem();
    if (item == null || item.placeholder) {
        aviso("Selecciona un producto de la lista.");
        return;
    }
        for (int i = 0; i < modeloProductos.size(); i++) {
            if (modeloProductos.get(i).id == item.id) {
                aviso("Ese producto ya está en la promoción.");
                return;
            }
        }
        modeloProductos.addElement(item);
        actualizarResumen();
    }

    private void actualizarResumen() {
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
                lblResumen.setForeground(new Color(0, 128, 0));
                lblResumen.setText(String.format("Normal: $%.2f  |  Paquete: $%.2f  |  Ahorro: $%.2f", normal, paquete, ahorro));
            } else {
                lblResumen.setForeground(new Color(200, 0, 0));
                lblResumen.setText("El paquete no conviene: cuesta igual o más que el precio normal.");
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
        lblResumen.setText(" ");
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