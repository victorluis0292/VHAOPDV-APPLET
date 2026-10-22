package Vista;

import Estilos.Estilos;
import Modelo.CatalogoGlobal;
import Modelo.CatalogoGlobalDao;
import Modelo.Categoria;
import Modelo.CategoriaDao;
import Modelo.Combo;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.Proveedor;
import Modelo.ProveedorDao;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class PanelProductos extends JPanel {

    // ============================================================
    // CAMPOS PUBLICOS
    // ============================================================
    public JTextField txtBuscar;
    public JTextField txtCodigoPro, txtDesPro, txtCantPro, txtPrecioPro, txtPreciocompraPro, txtIdproducto, txtImagenPro;
    public JComboBox<Combo> cbxProveedorPro;
    public JComboBox<Categoria> cbxCategoria;
    public JButton btnGuardarpro, btnEditarpro, btnEliminarPro, btnNuevoPro, btnBuscarImagen, btnExportarExcel, btnImportarExcel;
    public JRadioButton rbPorUnidad, rbAGranel, rbComoPaquete;
    public JTable TableProducto;
    public DefaultTableModel modelo;
    public JButton btnPromociones;
    public JButton btnGenerarCodigoBarras;

    private ButtonGroup grupoTipoVenta;
    private final CategoriaDao categoriaDao = new CategoriaDao();
    private int idEmpresa;
    private ProductosDao proDao;
    private ProveedorDao proveedorDao;
    private final CatalogoGlobalDao catalogoDao = new CatalogoGlobalDao();

    private Integer idCatalogoGlobalSeleccionado = null;
    private Integer idCategoriaActual = null;
    private boolean ignorarEventoProveedor = false;
    private TableRowSorter<DefaultTableModel> sorterProductos;
    private final Runnable onProductosActualizados;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public PanelProductos(int idEmpresa, ProductosDao proDao, ProveedorDao proveedorDao) {
        this(idEmpresa, proDao, proveedorDao, null);
    }

    public PanelProductos(int idEmpresa, ProductosDao proDao, ProveedorDao proveedorDao, Runnable onProductosActualizados) {
        this.idEmpresa = idEmpresa;
        this.proDao = proDao;
        this.proveedorDao = proveedorDao;
        this.onProductosActualizados = onProductosActualizados;

        initComponents();
        llenarProveedor();
        llenarCategorias();
        ListarProductos();
        initEvents();
    }

    private void notificarCambioDeProductos() {
        if (onProductosActualizados != null) {
            onProductosActualizados.run();
        }
    }

    // ============================================================
    // DISEÑO PRINCIPAL
    // ============================================================

    private void initComponents() {

        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel superior ---
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 0));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel lblTitulo = new JLabel("PRODUCTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(0, 102, 204));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        Estilos.estiloEtiqueta(lblBuscar);
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(350, 34));
        Estilos.estiloCampo(txtBuscar);

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);
        
        //-----Boto de pantalla promociones
        btnPromociones = new JButton("Promociones");
       btnPromociones.setPreferredSize(new Dimension(150, 34));
       Estilos.estiloBotonAzul(btnPromociones);
       JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
       panelCentro.setOpaque(false);
       panelCentro.add(btnPromociones);
       panelSuperior.add(panelCentro, BorderLayout.CENTER);

        // --- Botones Excel ---
        btnExportarExcel = new JButton("Exportar");
        btnExportarExcel.setIcon(cargarIcono("/Img/exportar_excel.png", 25, 25));
        btnExportarExcel.setIconTextGap(6);
        btnExportarExcel.setPreferredSize(new Dimension(150, 34));
        Estilos.estiloBotonAzul(btnExportarExcel);
        btnExportarExcel.setToolTipText("Exportar productos a Excel");
        panelBusqueda.add(btnExportarExcel);

        btnImportarExcel = new JButton("Importar");
        btnImportarExcel.setIcon(cargarIcono("/Img/importar_excel.png", 25, 25));
        btnImportarExcel.setIconTextGap(6);
        btnImportarExcel.setPreferredSize(new Dimension(150, 34));
        Estilos.estiloBotonAzul(btnImportarExcel);
        btnImportarExcel.setToolTipText("Importar productos desde Excel");
        panelBusqueda.add(btnImportarExcel);

        // --- Panel formulario ---
        Estilos.PanelConEstilo panelFormulario = new Estilos.PanelConEstilo();
        panelFormulario.setPreferredSize(new Dimension(430, 0));

        JPanel contenidoFormulario = new JPanel(new GridBagLayout());
        contenidoFormulario.setOpaque(false);

        JLabel tituloFormulario = new JLabel("Nuevo Producto");
        tituloFormulario.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloFormulario.setForeground(new Color(0, 102, 204));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Campos
        txtCodigoPro = new JTextField();
        txtDesPro = new JTextField();
        txtCantPro = new JTextField();
        txtPrecioPro = new JTextField();
        txtPreciocompraPro = new JTextField();

        Estilos.estiloCampo(txtCodigoPro);
        Estilos.estiloCampo(txtDesPro);
        Estilos.estiloCampo(txtCantPro);
        Estilos.estiloCampo(txtPrecioPro);
        Estilos.estiloCampo(txtPreciocompraPro);

        // Radio buttons
        rbPorUnidad = new JRadioButton("Por Unidad/Pza", true);
        rbAGranel = new JRadioButton("A Granel");
        rbComoPaquete = new JRadioButton("Paquete / Kit");

        Font fontRadio = new Font("Segoe UI", Font.PLAIN, 12);
        rbPorUnidad.setFont(fontRadio);
        rbAGranel.setFont(fontRadio);
        rbComoPaquete.setFont(fontRadio);
        rbPorUnidad.setOpaque(false);
        rbAGranel.setOpaque(false);
        rbComoPaquete.setOpaque(false);

        grupoTipoVenta = new ButtonGroup();
        grupoTipoVenta.add(rbPorUnidad);
        grupoTipoVenta.add(rbAGranel);
        grupoTipoVenta.add(rbComoPaquete);

        JPanel panelTipoVenta = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        panelTipoVenta.setOpaque(false);
        panelTipoVenta.add(rbPorUnidad);
        panelTipoVenta.add(rbAGranel);
        panelTipoVenta.add(rbComoPaquete);

        // Categoria / Proveedor
        cbxCategoria = new JComboBox<>();
        estilizarCombo(cbxCategoria);

        cbxProveedorPro = new JComboBox<>();
        estilizarCombo(cbxProveedorPro);

        DefaultListCellRenderer cleanRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Combo) {
                    setText(((Combo) value).getNombre());
                } else if (value instanceof Categoria) {
                    setText(((Categoria) value).getNombre());
                }

                if (isSelected) {
                    setBackground(new Color(220, 235, 250));
                    setForeground(Color.BLACK);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }

                return this;
            }
        };

        cbxProveedorPro.setRenderer(cleanRenderer);
        cbxCategoria.setRenderer(cleanRenderer);

        // Imagen / catálogo
        txtImagenPro = new JTextField();
        txtImagenPro.setEditable(false);
        Estilos.estiloCampo(txtImagenPro);

        btnBuscarImagen = new JButton("📁");
        btnBuscarImagen.setPreferredSize(new Dimension(45, 34));
        Estilos.estiloBotonAzul(btnBuscarImagen);

        JPanel panelImagen = new JPanel(new BorderLayout(5, 0));
        panelImagen.setOpaque(false);
        panelImagen.add(txtImagenPro, BorderLayout.CENTER);
        panelImagen.add(btnBuscarImagen, BorderLayout.EAST);

        // ID oculto
        txtIdproducto = new JTextField();
        txtIdproducto.setVisible(false);

        // Agregar campos
        int fila = 0;
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        contenidoFormulario.add(tituloFormulario, gbc);

        agregarCampo(contenidoFormulario, "Código:", txtCodigoPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Descripción:", txtDesPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Se vende:", panelTipoVenta, gbc, fila++);
        agregarCampo(contenidoFormulario, "Categoría:", cbxCategoria, gbc, fila++);
        agregarCampo(contenidoFormulario, "Cantidad:", txtCantPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Precio Venta:", txtPrecioPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Precio Compra:", txtPreciocompraPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Proveedor:", cbxProveedorPro, gbc, fila++);
        agregarCampo(contenidoFormulario, "Imagen / Catálogo:", panelImagen, gbc, fila++);

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBotones.setOpaque(false);

        btnGuardarpro = new JButton("💾 Guardar");
        btnEditarpro = new JButton("📝 Actualizar");
        btnEliminarPro = new JButton("❌ Eliminar");
        btnNuevoPro = new JButton("➕ Nuevo");

        Estilos.estiloBotonAzul(btnGuardarpro);
        Estilos.estiloBotonAzul(btnEditarpro);
        Estilos.estiloBotonAzul(btnEliminarPro);
        Estilos.estiloBotonAzul(btnNuevoPro);

        btnGuardarpro.setToolTipText("Guardar producto");
        btnEditarpro.setToolTipText("Editar producto seleccionado");
        btnEliminarPro.setToolTipText("Eliminar producto seleccionado");
        btnNuevoPro.setToolTipText("Nuevo producto");

        panelBotones.add(btnGuardarpro);
        panelBotones.add(btnEditarpro);
        panelBotones.add(btnEliminarPro);
        panelBotones.add(btnNuevoPro);

        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 5, 5, 5);
        contenidoFormulario.add(panelBotones, gbc);

        gbc.gridy = fila;
        gbc.weighty = 1.0;
        //////////////////////////////////////////////////////////////////////7
        btnGenerarCodigoBarras = new JButton("Generador de código de barras");
        btnGenerarCodigoBarras.setPreferredSize(new Dimension(0, 34));
        Estilos.estiloBotonAzul(btnGenerarCodigoBarras);
        btnGenerarCodigoBarras.setEnabled(false);

        gbc.gridy = fila++;
        gbc.insets = new Insets(0, 5, 5, 5);
        contenidoFormulario.add(btnGenerarCodigoBarras, gbc);

        JScrollPane scrollFormulario = new JScrollPane(contenidoFormulario);
        scrollFormulario.setBorder(null);
        scrollFormulario.setOpaque(false);
        scrollFormulario.getViewport().setOpaque(false);
        scrollFormulario.getVerticalScrollBar().setUnitIncrement(16);
        scrollFormulario.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelFormulario.add(scrollFormulario, BorderLayout.CENTER);

        // --- Tabla ---
        Estilos.PanelConEstilo panelTabla = new Estilos.PanelConEstilo();

        JPanel encabezadoTabla = new JPanel(new BorderLayout());
        encabezadoTabla.setOpaque(false);

        JLabel tituloTabla = new JLabel("Productos registrados");
        tituloTabla.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloTabla.setForeground(new Color(0, 102, 204));
        encabezadoTabla.add(tituloTabla, BorderLayout.WEST);

        String[] nombresColumnas = {"ID", "CODIGO", "DESCRIPCIÓN", "CATEGORÍA", "PROVEEDOR", "STOCK", "PRECIO", "Precio Compra", "ID_Prov", "IMAGEN_URL"};

        modelo = new DefaultTableModel(null, nombresColumnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        TableProducto = new JTable(modelo);
        Estilos.estiloTabla(TableProducto);
        TableProducto.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableProducto.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableProducto.setRowHeight(30);
        TableProducto.setShowVerticalLines(false);
        TableProducto.setShowHorizontalLines(false);
        TableProducto.setIntercellSpacing(new Dimension(0, 0));
        TableProducto.setFillsViewportHeight(true);
        TableProducto.setAutoCreateRowSorter(false);

        configurarAnchosTabla();

        sorterProductos = new TableRowSorter<>(modelo);
        TableProducto.setRowSorter(sorterProductos);

        TableProducto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                TableProductoMouseClicked(evt);
            }
        });

        JScrollPane scrollTabla = new JScrollPane(TableProducto);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        scrollTabla.setBackground(Color.WHITE);
        scrollTabla.getViewport().setBackground(Color.WHITE);

        panelTabla.add(encabezadoTabla, BorderLayout.NORTH);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(12, 0));
        contenido.setOpaque(false);
        contenido.add(panelFormulario, BorderLayout.WEST);
        contenido.add(panelTabla, BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);

        btnGuardarpro.setEnabled(true);
        btnEditarpro.setEnabled(false);
    }

    // ============================================================
    // CONFIGURACION DE TABLA
    // ============================================================

    private void configurarAnchosTabla() {

        if (TableProducto == null) return;

        TableColumn columna;

        columna = TableProducto.getColumnModel().getColumn(0);
        columna.setPreferredWidth(65);
        columna.setMinWidth(55);
        columna.setMaxWidth(90);

        columna = TableProducto.getColumnModel().getColumn(1);
        columna.setPreferredWidth(130);
        columna.setMinWidth(100);

        columna = TableProducto.getColumnModel().getColumn(2);
        columna.setPreferredWidth(300);
        columna.setMinWidth(180);

        columna = TableProducto.getColumnModel().getColumn(3);
        columna.setPreferredWidth(150);
        columna.setMinWidth(110);

        columna = TableProducto.getColumnModel().getColumn(4);
        columna.setPreferredWidth(180);
        columna.setMinWidth(120);

        columna = TableProducto.getColumnModel().getColumn(5);
        columna.setPreferredWidth(90);
        columna.setMinWidth(70);
        columna.setMaxWidth(120);

        columna = TableProducto.getColumnModel().getColumn(6);
        columna.setPreferredWidth(110);
        columna.setMinWidth(90);

        columna = TableProducto.getColumnModel().getColumn(7);
        columna.setPreferredWidth(130);
        columna.setMinWidth(100);

        columna = TableProducto.getColumnModel().getColumn(8);
        columna.setMinWidth(0);
        columna.setMaxWidth(0);
        columna.setPreferredWidth(0);
        columna.setWidth(0);

        columna = TableProducto.getColumnModel().getColumn(9);
        columna.setMinWidth(0);
        columna.setMaxWidth(0);
        columna.setPreferredWidth(0);
        columna.setWidth(0);
    }

    // ============================================================
    // COMBOBOX / ICONOS / CAMPOS
    // ============================================================

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(33, 33, 33));
        combo.setPreferredSize(new Dimension(0, 34));
        combo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        URL url = getClass().getResource(ruta);
        if (url == null) return null;

        ImageIcon iconoOriginal = new ImageIcon(url);
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenEscalada);
    }

    private void agregarCampo(JPanel panel, String textoLabel, JComponent componente, GridBagConstraints gbc, int fila) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;

        JLabel label = new JLabel(textoLabel);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(70, 70, 70));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(componente, gbc);
    }

    // ============================================================
    // CLICK TABLA
    // ============================================================

    private void TableProductoMouseClicked(MouseEvent evt) {

        int filaSeleccionada = TableProducto.getSelectedRow();
        if (filaSeleccionada == -1) return;

        int filaReal = TableProducto.convertRowIndexToModel(filaSeleccionada);

        txtIdproducto.setText(valorTabla(filaReal, 0));
        txtCodigoPro.setText(valorTabla(filaReal, 1));
        txtDesPro.setText(valorTabla(filaReal, 2));

        String categoriaNombre = valorTabla(filaReal, 3);
        for (int i = 0; i < cbxCategoria.getItemCount(); i++) {
            Categoria item = cbxCategoria.getItemAt(i);
            if (item != null && item.getNombre() != null && item.getNombre().equalsIgnoreCase(categoriaNombre)) {
                cbxCategoria.setSelectedIndex(i);
                idCategoriaActual = item.getIdCategoria();
                break;
            }
        }

        txtCantPro.setText(valorTabla(filaReal, 5));
        txtPrecioPro.setText(valorTabla(filaReal, 6));
        txtPreciocompraPro.setText(valorTabla(filaReal, 7));

        Object idProvObj = modelo.getValueAt(filaReal, 8);
        if (idProvObj != null) {
            try {
                int idProv = Integer.parseInt(idProvObj.toString());
                ignorarEventoProveedor = true;

                for (int i = 0; i < cbxProveedorPro.getItemCount(); i++) {
                    Combo item = cbxProveedorPro.getItemAt(i);
                    if (item != null && item.getId() == idProv) {
                        cbxProveedorPro.setSelectedIndex(i);
                        break;
                    }
                }

                ignorarEventoProveedor = false;
            } catch (Exception ex) {
                ignorarEventoProveedor = false;
            }
        }

        Object imagenUrlObj = modelo.getValueAt(filaReal, 9);
        if (imagenUrlObj != null && !imagenUrlObj.toString().trim().isEmpty()) {
            txtImagenPro.setText(imagenUrlObj.toString().trim());
        } else {
            txtImagenPro.setText("");
        }

        btnEditarpro.setEnabled(true);
        btnGuardarpro.setEnabled(false);
        btnGenerarCodigoBarras.setEnabled(true);
    }

    private String valorTabla(int fila, int columna) {
        Object valor = modelo.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString();
    }

    // ============================================================
    // TIPO DE VENTA
    // ============================================================

    public String obtenerTipoVenta() {
        if (rbAGranel.isSelected()) return "GRANEL";
        if (rbComoPaquete.isSelected()) return "KIT";
        return "UNIDAD";
    }

    public void setTipoVenta(String tipoVenta) {
        if (tipoVenta == null) {
            rbPorUnidad.setSelected(true);
            return;
        }

        switch (tipoVenta.toUpperCase()) {
            case "GRANEL":
                rbAGranel.setSelected(true);
                break;
            case "KIT":
                rbComoPaquete.setSelected(true);
                break;
            default:
                rbPorUnidad.setSelected(true);
        }
    }

    // ============================================================
    // PROVEEDORES / CATEGORIAS
    // ============================================================

    public void llenarProveedor() {
        cbxProveedorPro.removeAllItems();
        cbxProveedorPro.addItem(new Combo(0, "-- Seleccione --"));

        List<Proveedor> lista = proveedorDao.ListarProveedorPorEmpresa(idEmpresa);
        if (lista != null) {
            for (Proveedor proveedor : lista) {
                cbxProveedorPro.addItem(new Combo(proveedor.getId(), proveedor.getNombre()));
            }
        }

        cbxProveedorPro.addItem(new Combo(-1, "+ Agregar proveedor"));
    }

    public void llenarCategorias() {
        cbxCategoria.removeAllItems();
        cbxCategoria.addItem(new Categoria(0, "-- Seleccione --", idEmpresa));

        List<Categoria> lista = categoriaDao.listarPorEmpresa(idEmpresa);
        if (lista != null) {
            for (Categoria cat : lista) {
                cbxCategoria.addItem(cat);
            }
        }
    }

    // ============================================================
    // LISTAR PRODUCTOS
    // ============================================================

    public void ListarProductos() {

        List<Productos> listaPro = proDao.ListarProductos(idEmpresa);

        modelo = (DefaultTableModel) TableProducto.getModel();
        TableProducto.setRowSorter(null);
        modelo.setRowCount(0);

        Object[] ob = new Object[10];

        if (listaPro != null) {
            for (Productos producto : listaPro) {
                ob[0] = producto.getId();
                ob[1] = producto.getCodigo();
                ob[2] = producto.getNombre();
                ob[3] = producto.getCategoria();
                ob[4] = producto.getProveedorPro();
                ob[5] = producto.getStock();
                ob[6] = producto.getPrecio();
                ob[7] = producto.getPreciocompra();
                ob[8] = producto.getProveedor();
                ob[9] = producto.getImagenUrl();
                modelo.addRow(ob);
            }
        }

        sorterProductos = new TableRowSorter<>(modelo);
        TableProducto.setRowSorter(sorterProductos);

        configurarAnchosTabla();
        aplicarFiltroBusqueda();
    }

    // ============================================================
    // IMPORTAR: PROCESAR FILAS
    // ============================================================

    private void procesarImportacion(List<Utilidades.ImportadorExcel.FilaImportada> filas) {

        List<Productos> existentes = proDao.ListarProductos(idEmpresa);
        Map<String, Productos> existentesPorCodigo = new HashMap<>();

        if (existentes != null) {
            for (Productos p : existentes) {
                if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
                    Utilidades.LogUtil.info("Producto existente sin código válido, id=" + p.getId() + " — se ignora en el mapeo");
                    continue;
                }
                existentesPorCodigo.put(p.getCodigo().trim().toUpperCase(), p);
            }
        }

        int nuevos = 0, actualizados = 0, sinCambios = 0, omitidos = 0;
        StringBuilder detalleOmitidos = new StringBuilder();

        for (Utilidades.ImportadorExcel.FilaImportada fila : filas) {

            try {
                boolean categoriaVacia = fila.categoria == null || fila.categoria.trim().isEmpty();
                boolean proveedorVacio = fila.proveedor == null || fila.proveedor.trim().isEmpty();

                if (categoriaVacia || proveedorVacio) {
                    omitidos++;
                    detalleOmitidos.append("- ").append(fila.codigo).append(": categoría y proveedor son obligatorios (vienen vacíos)\n");
                    Utilidades.LogUtil.info("Fila omitida (código=" + fila.codigo + "): categoría o proveedor vacíos");
                    continue;
                }

                Integer idCategoria = resolverCategoria(fila.categoria);
                Combo proveedorEncontrado = resolverProveedor(fila.proveedor);

                if (idCategoria == null || proveedorEncontrado == null) {
                    omitidos++;
                    detalleOmitidos.append("- ").append(fila.codigo).append(": no se pudo crear o asignar la categoría/proveedor\n");
                    Utilidades.LogUtil.info("Fila omitida (código=" + fila.codigo + "): fallo al resolver categoría/proveedor");
                    continue;
                }

                Productos existente = existentesPorCodigo.get(fila.codigo.trim().toUpperCase());

                if (existente != null) {

                    boolean hayCambios = hayDiferencias(existente, fila, idCategoria, proveedorEncontrado);
                    if (!hayCambios) {
                        sinCambios++;
                        continue;
                    }

                    Productos pro = new Productos();
                    pro.setId(existente.getId());
                    pro.setCodigo(fila.codigo.trim());
                    pro.setNombre(fila.descripcion.trim());
                    pro.setCategoria(obtenerNombreCategoriaPorId(idCategoria));
                    pro.setIdCategoria(idCategoria);
                    pro.setStock(fila.stock);
                    pro.setPrecio(fila.precioVenta);
                    pro.setPreciocompra(fila.precioCompra);
                    pro.setProveedor(proveedorEncontrado.getId());
                    pro.setProveedorPro(proveedorEncontrado.getNombre());
                    pro.setId_empresa(idEmpresa);
                    pro.setImagenUrl(existente.getImagenUrl());
                    pro.setIdCatalogoGlobal(existente.getIdCatalogoGlobal());

                    if (proDao.ModificarProductos(pro)) {
                        actualizados++;
                    } else {
                        omitidos++;
                        detalleOmitidos.append("- ").append(fila.codigo).append(": error al actualizar\n");
                        Utilidades.LogUtil.info("Fallo ModificarProductos para código=" + fila.codigo);
                    }

                } else {

                    Productos pro = new Productos();
                    pro.setCodigo(fila.codigo.trim());
                    pro.setNombre(fila.descripcion.trim());
                    pro.setCategoria(obtenerNombreCategoriaPorId(idCategoria));
                    pro.setIdCategoria(idCategoria);
                    pro.setStock(fila.stock);
                    pro.setPrecio(fila.precioVenta);
                    pro.setPreciocompra(fila.precioCompra);
                    pro.setProveedor(proveedorEncontrado.getId());
                    pro.setProveedorPro(proveedorEncontrado.getNombre());
                    pro.setId_empresa(idEmpresa);

                    if (proDao.RegistrarProductos(pro)) {
                        nuevos++;
                    } else {
                        omitidos++;
                        detalleOmitidos.append("- ").append(fila.codigo).append(": error al registrar\n");
                        Utilidades.LogUtil.info("Fallo RegistrarProductos para código=" + fila.codigo);
                    }
                }

            } catch (Exception exFila) {
                omitidos++;
                detalleOmitidos.append("- ").append(fila.codigo).append(": error inesperado, ver log\n");
                Utilidades.LogUtil.error("Error procesando fila con código=" + fila.codigo, exFila);
            }
        }

        ListarProductos();
        notificarCambioDeProductos();

        String resumen = "Importación completa.\n\n"
                + "Nuevos: " + nuevos + "\n"
                + "Actualizados: " + actualizados + "\n"
                + "Sin cambios: " + sinCambios + "\n"
                + "Omitidos: " + omitidos;

        if (omitidos > 0) {
            resumen += "\n\nDetalle:\n" + detalleOmitidos + "\nRevisa logs/vhao-pdv.log para más información.";
        }

        JOptionPane.showMessageDialog(this, resumen, "Resultado de la importación", JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================
    // IMPORTAR: COMPARAR SI HAY CAMBIOS REALES
    // ============================================================

    private boolean hayDiferencias(Productos existente, Utilidades.ImportadorExcel.FilaImportada fila, int idCategoriaNueva, Combo proveedorNuevo) {

        String nombreExistente = existente.getNombre() == null ? "" : existente.getNombre().trim();
        String nombreNuevo = fila.descripcion == null ? "" : fila.descripcion.trim();

        if (!nombreExistente.equalsIgnoreCase(nombreNuevo)) return true;

        if (!Objects.equals(existente.getIdCategoria(), Integer.valueOf(idCategoriaNueva))) return true;

        if (!Objects.equals(existente.getProveedor(), Integer.valueOf(proveedorNuevo.getId()))) return true;

        if (existente.getStock() != fila.stock) return true;

        if (Double.compare(existente.getPrecio(), fila.precioVenta) != 0) return true;

        return Double.compare(existente.getPreciocompra(), fila.precioCompra) != 0;
    }

    // ============================================================
    // IMPORTAR: RESOLVER O CREAR CATEGORÍA / PROVEEDOR
    // ============================================================

    private Integer resolverCategoria(String nombreCategoria) {

        if (nombreCategoria == null || nombreCategoria.trim().isEmpty()) return null;

        String nombreLimpio = nombreCategoria.trim();

        for (int i = 0; i < cbxCategoria.getItemCount(); i++) {
            Categoria item = cbxCategoria.getItemAt(i);
            if (item != null && item.getIdCategoria() != 0 && item.getNombre() != null && item.getNombre().equalsIgnoreCase(nombreLimpio)) {
                return item.getIdCategoria();
            }
        }

        int idGenerado = categoriaDao.registrar(nombreLimpio, idEmpresa);

        if (idGenerado <= 0) {
            Utilidades.LogUtil.info("No se pudo crear la categoría nueva: " + nombreLimpio);
            return null;
        }

        llenarCategorias();
        return idGenerado;
    }

    private Combo resolverProveedor(String nombreProveedor) {

        if (nombreProveedor == null || nombreProveedor.trim().isEmpty()) return null;

        String nombreLimpio = nombreProveedor.trim();

        for (int i = 0; i < cbxProveedorPro.getItemCount(); i++) {
            Combo item = cbxProveedorPro.getItemAt(i);
            if (item != null && item.getId() > 0 && item.getNombre() != null && item.getNombre().equalsIgnoreCase(nombreLimpio)) {
                return item;
            }
        }

        Proveedor nuevo = new Proveedor();
        nuevo.setNombre(nombreLimpio);
        nuevo.setIdEmpresa(idEmpresa);
        nuevo.setRuc("");
        nuevo.setTelefono("");
        nuevo.setDireccion("");

        boolean exito = proveedorDao.RegistrarProveedor(nuevo);

        if (!exito) {
            Utilidades.LogUtil.info("No se pudo crear el proveedor nuevo: " + nombreLimpio);
            return null;
        }

        llenarProveedor();

        for (int i = 0; i < cbxProveedorPro.getItemCount(); i++) {
            Combo item = cbxProveedorPro.getItemAt(i);
            if (item != null && item.getId() > 0 && item.getNombre() != null && item.getNombre().equalsIgnoreCase(nombreLimpio)) {
                return item;
            }
        }

        return null;
    }

    private String obtenerNombreCategoriaPorId(int idCategoria) {
        for (int i = 0; i < cbxCategoria.getItemCount(); i++) {
            Categoria item = cbxCategoria.getItemAt(i);
            if (item != null && item.getIdCategoria() == idCategoria) {
                return item.getNombre();
            }
        }
        return "";
    }

    // ============================================================
    // EVENTOS
    // ============================================================

    private void initEvents() {
        //--Promociones---
        btnPromociones.addActionListener(e ->
    new FrmPromociones((Frame) SwingUtilities.getWindowAncestor(this), idEmpresa, proDao).setVisible(true));

        // --- Exportar ---
        btnExportarExcel.addActionListener(e -> {
            List<Productos> productos = proDao.ListarProductos(idEmpresa);
            Utilidades.ExportadorExcel.exportarProductos(this, productos);
        });

        // --- Importar ---
        btnImportarExcel.addActionListener(e -> {

            JFileChooser selector = new JFileChooser();
            selector.setDialogTitle("Seleccionar archivo de productos");
            selector.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));

            File carpetaDescargas = new File(System.getProperty("user.home"), "Downloads");
            if (carpetaDescargas.exists()) {
                selector.setCurrentDirectory(carpetaDescargas);
            }

            if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File archivo = selector.getSelectedFile();

            try {
                List<String> encabezados = Utilidades.ImportadorExcel.leerEncabezados(archivo);

                Map<String, Integer> mapeo = DialogoMapeoColumnasImportarExcel.mostrar(
                        SwingUtilities.getWindowAncestor(this), encabezados);

                if (mapeo == null) return;

                List<Utilidades.ImportadorExcel.FilaImportada> filas = Utilidades.ImportadorExcel.leerFilas(archivo, mapeo);
                procesarImportacion(filas);

            } catch (Exception ex) {
                Utilidades.LogUtil.error("Error al leer o procesar el archivo de importación", ex);
                JOptionPane.showMessageDialog(this,
                        "Error al procesar la importación:\n" + ex.getClass().getSimpleName()
                                + (ex.getMessage() != null ? ": " + ex.getMessage() : " (sin mensaje)"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Validar código duplicado ---
        Runnable validarCodigoExistente = () -> {

            String codigoIngresado = txtCodigoPro.getText().trim();
            String idActual = txtIdproducto.getText().trim();

            if (codigoIngresado.isEmpty()) return;

            for (int i = 0; i < modelo.getRowCount(); i++) {
                String idFila = modelo.getValueAt(i, 0).toString();
                String codigoEnTabla = modelo.getValueAt(i, 1).toString();

                if (codigoEnTabla.equalsIgnoreCase(codigoIngresado) && (idActual.isEmpty() || !idFila.equals(idActual))) {
                    JOptionPane.showMessageDialog(this,
                            "¡Este producto ya fue registrado!\nEl código '" + codigoIngresado + "' ya existe en el sistema.",
                            "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                    txtCodigoPro.requestFocus();
                    txtCodigoPro.selectAll();
                    break;
                }
            }
        };

        txtCodigoPro.addActionListener(e -> validarCodigoExistente.run());

        txtCodigoPro.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                if (!txtCodigoPro.getText().trim().isEmpty()) {
                    validarCodigoExistente.run();
                }
            }
        });

        // --- Catálogo global ---
        btnBuscarImagen.addActionListener(e -> {
            String sugerido = txtDesPro.getText() == null ? "" : txtDesPro.getText().trim();
            CatalogoGlobal seleccionado = mostrarSelectorCatalogoGlobal(sugerido);

            if (seleccionado != null) {
                txtImagenPro.setText(seleccionado.getImagenUrl() == null ? "" : seleccionado.getImagenUrl().trim());
                idCatalogoGlobalSeleccionado = seleccionado.getId();
            }
        });

        // --- Guardar ---
        btnGuardarpro.addActionListener(e -> {

            Categoria categoriaSeleccionada = (Categoria) cbxCategoria.getSelectedItem();

            if (categoriaSeleccionada == null || categoriaSeleccionada.getIdCategoria() == 0) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione una categoría válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                cbxCategoria.requestFocus();
                return;
            }

            Combo itemProveedor = (Combo) cbxProveedorPro.getSelectedItem();

            if (itemProveedor == null || itemProveedor.getId() == 0 || itemProveedor.getId() == -1) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un proveedor válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                cbxProveedorPro.requestFocus();
                return;
            }

            if (!txtCodigoPro.getText().trim().isEmpty() && !txtDesPro.getText().trim().isEmpty() && !txtPrecioPro.getText().trim().isEmpty()) {

                String codigoIngresado = txtCodigoPro.getText().trim();

                for (int i = 0; i < modelo.getRowCount(); i++) {
                    if (modelo.getValueAt(i, 1).toString().equalsIgnoreCase(codigoIngresado)) {
                        JOptionPane.showMessageDialog(this,
                                "El código '" + codigoIngresado + "' ya fue registrado en otro producto.",
                                "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                        txtCodigoPro.requestFocus();
                        txtCodigoPro.selectAll();
                        return;
                    }
                }

                try {
                    Productos pro = new Productos();
                    pro.setCodigo(codigoIngresado);
                    pro.setNombre(txtDesPro.getText().trim());
                    pro.setCategoria(categoriaSeleccionada.getNombre());
                    pro.setStock(Integer.parseInt(txtCantPro.getText().trim()));
                    pro.setPrecio(Double.parseDouble(txtPrecioPro.getText().trim()));
                    pro.setPreciocompra(Double.parseDouble(txtPreciocompraPro.getText().trim()));
                    pro.setProveedor(itemProveedor.getId());
                    pro.setProveedorPro(itemProveedor.getNombre());
                    pro.setImagenUrl(txtImagenPro.getText().trim());
                    pro.setId_empresa(idEmpresa);
                    pro.setIdCatalogoGlobal(idCatalogoGlobalSeleccionado);
                    pro.setIdCategoria(categoriaSeleccionada.getIdCategoria());

                    boolean exito = proDao.RegistrarProductos(pro);

                    if (exito) {
                        JOptionPane.showMessageDialog(this, "¡Producto registrado con éxito!");
                        ListarProductos();
                        limpiarFormulario();
                        notificarCambioDeProductos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al registrar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Cantidad y precios deben contener valores numéricos válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Hay campos obligatorios vacíos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --- Nuevo ---
        btnNuevoPro.addActionListener(e -> {
            limpiarFormulario();
            btnGuardarpro.setEnabled(true);
            btnEditarpro.setEnabled(false);
        });

        // --- Eliminar ---
        btnEliminarPro.addActionListener(e -> {

            if (!txtIdproducto.getText().trim().isEmpty()) {

                int pregunta = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este producto?", "Advertencia", JOptionPane.YES_NO_OPTION);

                if (pregunta == JOptionPane.YES_OPTION) {
                    try {
                        int id = Integer.parseInt(txtIdproducto.getText().trim());
                        proDao.EliminarProductos(id);
                        ListarProductos();
                        limpiarFormulario();
                        notificarCambioDeProductos();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "El ID del producto no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para eliminar.");
            }
        });

        // --- Editar ---
        btnEditarpro.addActionListener(e -> {

            if (txtIdproducto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para editar.");
                return;
            }

            Categoria categoriaSeleccionada = (Categoria) cbxCategoria.getSelectedItem();

            if (categoriaSeleccionada == null || categoriaSeleccionada.getIdCategoria() == 0) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione una categoría válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                cbxCategoria.requestFocus();
                return;
            }

            Combo itemProveedor = (Combo) cbxProveedorPro.getSelectedItem();

            if (itemProveedor == null || itemProveedor.getId() == 0 || itemProveedor.getId() == -1) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un proveedor válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                cbxProveedorPro.requestFocus();
                return;
            }

            if (!txtCodigoPro.getText().trim().isEmpty() && !txtDesPro.getText().trim().isEmpty() && !txtPrecioPro.getText().trim().isEmpty()) {

                try {
                    Productos pro = new Productos();
                    pro.setId(Integer.parseInt(txtIdproducto.getText().trim()));
                    pro.setCodigo(txtCodigoPro.getText().trim());
                    pro.setNombre(txtDesPro.getText().trim());
                    pro.setCategoria(categoriaSeleccionada.getNombre());
                    pro.setStock(Integer.parseInt(txtCantPro.getText().trim()));
                    pro.setPrecio(Double.parseDouble(txtPrecioPro.getText().trim()));
                    pro.setPreciocompra(Double.parseDouble(txtPreciocompraPro.getText().trim()));
                    pro.setProveedor(itemProveedor.getId());
                    pro.setProveedorPro(itemProveedor.getNombre());
                    pro.setImagenUrl(txtImagenPro.getText().trim());
                    pro.setId_empresa(idEmpresa);
                    pro.setIdCatalogoGlobal(idCatalogoGlobalSeleccionado);
                    pro.setIdCategoria(categoriaSeleccionada.getIdCategoria());

                    boolean exito = proDao.ModificarProductos(pro);

                    if (exito) {
                        JOptionPane.showMessageDialog(this, "¡Producto actualizado con éxito!");
                        ListarProductos();
                        limpiarFormulario();
                        notificarCambioDeProductos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Cantidad y precios deben contener valores numéricos válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Hay campos obligatorios vacíos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
         //--boton generador codigo de barras --
         btnGenerarCodigoBarras.addActionListener(e -> {
    if (txtIdproducto.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla primero.");
        return;
    }
    new DialogoCodigoBarras(
            (Frame) SwingUtilities.getWindowAncestor(this),
            txtDesPro.getText().trim(),
            txtCodigoPro.getText().trim(),
            txtPrecioPro.getText().trim()
    ).setVisible(true);
});
        // --- Proveedor ---
        cbxProveedorPro.addActionListener(e -> {
            if (ignorarEventoProveedor) return;

            Combo seleccionado = (Combo) cbxProveedorPro.getSelectedItem();

            if (seleccionado != null && seleccionado.getId() == -1) {
                abrirDialogoAgregarProveedor();
            }
        });

        // --- Búsqueda ---
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltroBusqueda(); }
        });
    }

    // ============================================================
    // LIMPIAR FORMULARIO
    // ============================================================

    private void limpiarFormulario() {

        txtIdproducto.setText("");
        txtCodigoPro.setText("");
        txtDesPro.setText("");
        txtCantPro.setText("");
        txtPrecioPro.setText("");
        txtPreciocompraPro.setText("");
        txtImagenPro.setText("");

        idCatalogoGlobalSeleccionado = null;
        idCategoriaActual = null;

        rbPorUnidad.setSelected(true);

        if (cbxCategoria.getItemCount() > 0) {
            cbxCategoria.setSelectedIndex(0);
        }

        if (cbxProveedorPro.getItemCount() > 0) {
            ignorarEventoProveedor = true;
            cbxProveedorPro.setSelectedIndex(0);
            ignorarEventoProveedor = false;
        }

        TableProducto.clearSelection();
        btnGuardarpro.setEnabled(true);
        btnEditarpro.setEnabled(false);
        btnGenerarCodigoBarras.setEnabled(false);
    }

    // ============================================================
    // FILTRO
    // ============================================================

    private void aplicarFiltroBusqueda() {

        if (sorterProductos == null) return;

        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim();

        if (texto.isEmpty()) {
            sorterProductos.setRowFilter(null);
            return;
        }

        try {
            RowFilter<DefaultTableModel, Integer> filtro = RowFilter.regexFilter("(?i)" + Pattern.quote(texto));
            sorterProductos.setRowFilter(filtro);
        } catch (PatternSyntaxException ex) {
            sorterProductos.setRowFilter(null);
        }
    }

    // ============================================================
    // DIALOGO AGREGAR PROVEEDOR
    // ============================================================

    private void abrirDialogoAgregarProveedor() {

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agregar Proveedor", true);
        dialog.setSize(450, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtRuc = new JTextField(15);
        JTextField txtNombre = new JTextField(15);
        JTextField txtTelefono = new JTextField(15);
        JTextField txtDireccion = new JTextField(15);

        Estilos.estiloCampo(txtRuc);
        Estilos.estiloCampo(txtNombre);
        Estilos.estiloCampo(txtTelefono);
        Estilos.estiloCampo(txtDireccion);

        int fila = 0;

        gbc.gridx = 0; gbc.gridy = fila;
        dialog.add(crearLabelDialogo("RFC:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtRuc, gbc);
        fila++;

        gbc.gridx = 0; gbc.gridy = fila;
        dialog.add(crearLabelDialogo("Nombre:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);
        fila++;

        gbc.gridx = 0; gbc.gridy = fila;
        dialog.add(crearLabelDialogo("Teléfono:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtTelefono, gbc);
        fila++;

        gbc.gridx = 0; gbc.gridy = fila;
        dialog.add(crearLabelDialogo("Dirección:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtDireccion, gbc);
        fila++;

        JButton btnGuardar = new JButton("💾 Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        Estilos.estiloBotonAzul(btnGuardar);
        Estilos.estiloBotonAzul(btnCancelar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 2;
        dialog.add(panelBotones, gbc);

        Runnable restaurarSeleccion = () -> {
            ignorarEventoProveedor = true;
            if (cbxProveedorPro.getItemCount() > 0) {
                cbxProveedorPro.setSelectedIndex(0);
            }
            ignorarEventoProveedor = false;
        };

        btnCancelar.addActionListener(e -> {
            dialog.dispose();
            restaurarSeleccion.run();
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                restaurarSeleccion.run();
            }
        });

        btnGuardar.addActionListener(e -> {

            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El nombre es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Proveedor nuevo = new Proveedor();
            nuevo.setRuc(txtRuc.getText().trim());
            nuevo.setNombre(txtNombre.getText().trim());
            nuevo.setTelefono(txtTelefono.getText().trim());
            nuevo.setDireccion(txtDireccion.getText().trim());
            nuevo.setIdEmpresa(idEmpresa);

            if (proveedorDao.RegistrarProveedor(nuevo)) {
                JOptionPane.showMessageDialog(dialog, "Proveedor agregado con éxito.");
                dialog.dispose();
                llenarProveedor();
                seleccionarProveedorPorNombre(nuevo.getNombre());
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar el proveedor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JLabel crearLabelDialogo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private void seleccionarProveedorPorNombre(String nombre) {
        ignorarEventoProveedor = true;

        for (int i = 0; i < cbxProveedorPro.getItemCount(); i++) {
            Combo item = cbxProveedorPro.getItemAt(i);
            if (item != null && item.getNombre() != null && item.getNombre().equals(nombre)) {
                cbxProveedorPro.setSelectedIndex(i);
                break;
            }
        }

        ignorarEventoProveedor = false;
    }

    // ============================================================
    // CATALOGO GLOBAL
    // ============================================================

    private CatalogoGlobal mostrarSelectorCatalogoGlobal(String filtroInicial) {

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Catálogo Global", true);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelBusqueda = new JPanel(new BorderLayout(6, 0));
        panelBusqueda.setBorder(new EmptyBorder(10, 10, 0, 10));
        panelBusqueda.setOpaque(false);

        JLabel lblBuscarCat = new JLabel("Buscar:");
        lblBuscarCat.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JTextField txtBuscarCat = new JTextField(filtroInicial == null ? "" : filtroInicial);
        Estilos.estiloCampo(txtBuscarCat);

        JButton btnBuscarCat = new JButton("🔎 Buscar");
        JButton btnVerTodo = new JButton("Ver todo");
        Estilos.estiloBotonAzul(btnBuscarCat);
        Estilos.estiloBotonAzul(btnVerTodo);

        JPanel panelBotonesBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelBotonesBusqueda.setOpaque(false);
        panelBotonesBusqueda.add(btnBuscarCat);
        panelBotonesBusqueda.add(btnVerTodo);

        panelBusqueda.add(lblBuscarCat, BorderLayout.WEST);
        panelBusqueda.add(txtBuscarCat, BorderLayout.CENTER);
        panelBusqueda.add(panelBotonesBusqueda, BorderLayout.EAST);

        JPanel panelContenedor = new JPanel(new GridLayout(0, 3, 10, 10));
        panelContenedor.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(panelContenedor);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));

        final CatalogoGlobal[] elegido = {null};

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setOpaque(false);
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        dialog.add(panelPrincipal, BorderLayout.CENTER);

        JButton btnCancelar = new JButton("Cancelar");
        Estilos.estiloBotonAzul(btnCancelar);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setOpaque(false);
        panelSur.add(btnCancelar);

        dialog.add(panelSur, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialog.dispose());

        Runnable[] cargarRef = new Runnable[1];

        cargarRef[0] = () -> {
            String filtro = txtBuscarCat.getText() == null ? "" : txtBuscarCat.getText().trim();
            cargarResultadosCatalogo(filtro, panelContenedor, dialog, elegido);
        };

        btnBuscarCat.addActionListener(e -> cargarRef[0].run());

        btnVerTodo.addActionListener(e -> {
            txtBuscarCat.setText("");
            cargarRef[0].run();
        });

        txtBuscarCat.addActionListener(e -> cargarRef[0].run());

        txtBuscarCat.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { cargarRef[0].run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { cargarRef[0].run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { cargarRef[0].run(); }
        });

        cargarResultadosCatalogo(txtBuscarCat.getText().trim(), panelContenedor, dialog, elegido);

        dialog.setVisible(true);

        return elegido[0];
    }

    // ============================================================
    // CARGAR CATALOGO
    // ============================================================

    private void cargarResultadosCatalogo(String filtro, JPanel panelContenedor, JDialog dialog, CatalogoGlobal[] elegido) {

        panelContenedor.removeAll();
        panelContenedor.setLayout(new BorderLayout());
        panelContenedor.add(new JLabel("Cargando catálogo...", JLabel.CENTER), BorderLayout.CENTER);
        panelContenedor.revalidate();
        panelContenedor.repaint();

        new Thread(() -> {

            List<CatalogoGlobal> lista = (filtro == null || filtro.isEmpty())
        ? catalogoDao.buscarPorNombre("")
        : catalogoDao.buscarPorNombre(filtro);

        // Descarga (o lee del caché) las imágenes aquí, fuera del hilo de la interfaz
        if (lista != null) {
            for (CatalogoGlobal c : lista) Utilidades.CacheImagenes.icono(c.getImagenUrl(), 90);
            }

        SwingUtilities.invokeLater(() -> {

                panelContenedor.removeAll();

                if (lista == null || lista.isEmpty()) {
                    panelContenedor.setLayout(new BorderLayout());
                    panelContenedor.add(new JLabel("No se encontraron productos en el catálogo global.", JLabel.CENTER), BorderLayout.CENTER);
                } else {
                    panelContenedor.setLayout(new GridLayout(0, 3, 10, 10));

                    for (CatalogoGlobal cat : lista) {

                        JButton btnItem = new JButton();
                        btnItem.setToolTipText("Seleccionar " + cat.getNombre());
                        btnItem.setFocusPainted(false);
                        btnItem.setBackground(Color.WHITE);
                        btnItem.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

                        String imageUrl = cat.getImagenUrl();

                        ImageIcon icono = Utilidades.CacheImagenes.icono(cat.getImagenUrl(), 90);
                        if (icono != null) btnItem.setIcon(icono);

                        btnItem.setText("<html><center>" + cat.getNombre() + "</center></html>");
                        btnItem.setVerticalTextPosition(SwingConstants.BOTTOM);
                        btnItem.setHorizontalTextPosition(SwingConstants.CENTER);

                        btnItem.addActionListener(evt -> {
                            elegido[0] = cat;

                            if (txtDesPro.getText().trim().isEmpty()) {
                                txtDesPro.setText(cat.getNombre());
                            }

                            txtImagenPro.setText(cat.getImagenUrl() == null ? "" : cat.getImagenUrl().trim());
                            idCatalogoGlobalSeleccionado = cat.getId();

                            dialog.dispose();
                        });

                        panelContenedor.add(btnItem);
                    }
                }

                panelContenedor.revalidate();
                panelContenedor.repaint();
            });

        }).start();
    }
}