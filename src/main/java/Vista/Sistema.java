package Vista;
import Controlador.RolController;
import Modelo.AbrirCajaEfectivo;
import Modelo.Cliente;
import Modelo.ClienteDao;
import Modelo.Combo;
import Modelo.Config;
import Modelo.Detalle;
import Modelo.Entrada;
import Modelo.EntradaDao;
import Modelo.Eventos;
import Modelo.loginDAO;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.Proveedor;
import Modelo.ProveedorDao;
import Modelo.Venta;
import Modelo.VentaDao;
import Modelo.login;
import Reportes.Grafico;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableRowSorter;
import javax.print.*; //caja registradora
import javax.print.attribute.*;  //caja registradora
import java.io.*; 
import static Modelo.AbrirCajaEfectivo.main;
import Modelo.Conexion;
import Modelo.ImprimirTicket;
import Utilidades.TablaBotonImprimirVenta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import static java.awt.SystemColor.menu;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import Estilos.Estilos;
import Helper.CobroHelper;
import Modelo.Empresa;
import Modelo.EmpresaDao;
import Utilidades.BotonCerrarSesion;
import Utilidades.ConfigApp;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import Controlador.InfoEmpresa;


 import Vista.menuCorteCaja;
import static Vista.FrmBusqueda2.TableProductoJF;
import java.awt.Component;
import java.awt.FlowLayout;
import static java.awt.SystemColor.menu;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import Utilidades.FormularioAgregarProveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import Controlador.VerduleriaController;
import javax.swing.JScrollPane;
import Controlador.HistorialVentasController;
//promociones
import Modelo.Promocion;
import Modelo.PromocionDao;
import Servicios.CalculadoraPromociones;
import javax.swing.table.TableCellRenderer;


public final class Sistema extends javax.swing.JFrame {
    private boolean sistemaYaIniciado = false;
      private Login loginView;
      private static String usuarioActivo;
      private JButton btnReimprimirTicket;
      private JButton btnCerrarSesion;
      private Vista.VerduleriaPanel panelVerduleria;
      private List<Productos> cacheProductosEmpresa = new ArrayList<>();
      private Vista.HistorialVentasPanel panelHistorialVentas;
       private HistorialVentasController historialController;
      private javax.swing.JPanel jPanelConfig;
     private RolController rolController = new RolController();
     private JButton btnConfiguraciones;
      
   private boolean corteValidado = false;
    private Set<Integer> pestañasConValidacion = new HashSet<>(); // ✅ atributo de clase

  private login usuarioLogueado; // objeto que guarda el usuario logueado
    private static int idEmpresaActiva;  // <-- nuevo campo
    
    
    private Vista.CreditoClientePanel panelCreditoCliente;
    private Controlador.CreditoClienteController creditoClienteController;
    
    //promociones
    // campos de la clase
private boolean aplicandoPromos = false;
private List<Promocion> promosVigentes = new ArrayList<>();
private InfoEmpresa infoEmpresa;
private boolean ajusteActivado = false;


public static void setUsuarioActivo(String usuario) {
    usuarioActivo = usuario;
}

public static String getUsuarioActivo() {
    return usuarioActivo;
}

public void setLoginView(Login login) {
    this.loginView = login;
}

public Login getLoginView() {
    return loginView;
}
// -------------------- EMPRESA --------------------
 public static void setIdEmpresaActiva(int idEmpresa) {
        idEmpresaActiva = idEmpresa;
    }

    public static int getIdEmpresaActiva() {
        return idEmpresaActiva;
    }
    //----------------------------
    public login getLoginActual() {
    return usuarioLogueado;
}
    
  private static Sistema instancia;
public static Sistema getInstancia() {
    if (instancia == null) {
        instancia = new Sistema();
    }
    return instancia;
}

    static JLabel lblTotal;
   public String origenActual = "venta"; // valor inicial por defecto



     public static DefaultTableModel modelo2;
    public static DefaultTableModel modelo3;
     public static DefaultTableModel modelo4;
    TableRowSorter<DefaultTableModel>sorter;
    String minus;
    String mayus;
    Date fechaVenta = new Date();
    
   
    String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(fechaVenta);
    
    Cliente cl = new Cliente();
    ClienteDao client = new ClienteDao();
    Proveedor pr = new Proveedor();
    ProveedorDao PrDao = new ProveedorDao();
    Productos pro = new Productos();
    ProductosDao proDao = new ProductosDao();
    EmpresaDao empDao= new EmpresaDao();
    Venta v = new Venta();
    VentaDao Vdao = new VentaDao();
    Entrada e = new Entrada();
    EntradaDao Edao = new EntradaDao();
    Detalle Dv = new Detalle();
      
    Config conf = new Config();
    Eventos event = new Eventos();
    login lg = new login();
    loginDAO login = new loginDAO();
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel tmp = new DefaultTableModel();
    
    int item;
    double TotalPagar = 0.00;
    double recibe = 0.00;
        double Total = 0.00;
 
    double TotalpagarEntrada = 0.00;
     double TotalpagarCredit= 0.00;

    

   

       // ✅ Reiniciar sistema y mostrar login si existe
    public static void reiniciarInstancia() {
        if (instancia != null) {
            if (instancia.loginView != null) {
                instancia.loginView.setVisible(true);  // volver a mostrar login
            }
            instancia.dispose();  // cerrar sistema
            instancia = null;     // liberar memoria
        }
    }
    

public Sistema() {
        instancia = this;   // 👈 agrega esta línea — asegura que el singleton SIEMPRE apunte a la instancia real en uso, sin importar si se creó con new Sistema() o con getInstancia()

    initComponents(); // inicializa todo

    // -------------------------
    // 🔹 Pestaña: Config Dispositivos
    // -------------------------
    jPanelConfig = new javax.swing.JPanel();
    // Insertar la pestaña justo antes de la última (Cerrar Sesión)
    Menu.insertTab("Config Dispositivos", null, jPanelConfig, null, Math.max(0, Menu.getTabCount() - 1));
    System.out.println("DEBUG: Config Dispositivos insertado en índice " + (Menu.getTabCount() - 2) + " antes de Cerrar Sesión");

    jPanelConfig.setLayout(new BorderLayout());
    jPanelConfig.add(new Vista.OpcionConfig(), BorderLayout.CENTER);

    // -------------------------
    // 🔹 Asegurar que jPanelMenuCorte exista y esté en el JTabbedPane
    // -------------------------
    if (jPanelMenuCorte == null) {
        jPanelMenuCorte = new javax.swing.JPanel();
        jPanelMenuCorte.setLayout(new BorderLayout());
    }
    if (Menu.indexOfComponent(jPanelMenuCorte) == -1) {
        Menu.insertTab("Corte de caja", null, jPanelMenuCorte, null, Math.max(0, Menu.getTabCount() - 1));
        System.out.println("DEBUG: jPanelMenuCorte insertado en índice " + Menu.indexOfComponent(jPanelMenuCorte));
    }

    // -------------------------
    // 🔹 Tabla de venta (si no existe)
    // -------------------------
    if (TableVenta == null) {
        TableVenta = new JTable();
        TableVenta.setFont(new java.awt.Font("Tahoma", 0, 20));
        TableVenta.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID", "DESCRIPCIÓN", "CANTIDAD", "PRECIO U.", "PRECIO TOTAL"
                }
        ));
        JScrollPane scroll = new JScrollPane(TableVenta);
        scroll.setBounds(20, 300, 600, 200);
        jPanel2.add(scroll);
    }
    // llamar a Historialventaspanel
    panelHistorialVentas = new Vista.HistorialVentasPanel();
    jPanel6.setLayout(new BorderLayout());
    jPanel6.removeAll();
    jPanel6.add(panelHistorialVentas, BorderLayout.CENTER);
    jPanel6.revalidate();
    jPanel6.repaint();
//
 // llama a panel creditoCliente
 panelCreditoCliente = new Vista.CreditoClientePanel();
creditoClienteController = new Controlador.CreditoClienteController(panelCreditoCliente);
jPanel17.setLayout(new BorderLayout());
jPanel17.removeAll();
jPanel17.add(panelCreditoCliente, BorderLayout.CENTER);
jPanel17.revalidate();
jPanel17.repaint();
    
        // -------------------------
    // 🔹 Ajustes de layout en jPanel2 (verdulería y tabla)
    // -------------------------
    jPanel2.remove(jScrollPane1);
    jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 650, 310));

    panelVerduleria = new Vista.VerduleriaPanel();
    jPanel2.add(panelVerduleria, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 160, 360, 310));

    Controlador.VerduleriaController verduleriaController =
            new Controlador.VerduleriaController(panelVerduleria, TableVenta);
    verduleriaController.setOnProductoAgregado(() -> {
        txtCodigoVenta.requestFocusInWindow();
        txtCodigoVenta.selectAll();
    });

       // -------------------------
    // 🔹 banners credito cliente
    // -------------------------
    bannersFiados = new BannersCreditoCliente(this::abrirHistorialFiado);
jPanel2.add(bannersFiados, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 4, 590, 92));
    
    
    
    TableVenta.getModel().addTableModelListener(evt -> TotalPagarX());

    jPanel2.revalidate();
    jPanel2.repaint();

    // -------------------------

    // -------------------------
    // 🔹 BOTÓN CERRAR SESIÓN: solo se crea si no existe
    // -------------------------
    if (btnCerrarSesion == null) {
        btnCerrarSesion = BotonCerrarSesion.crearBoton(this, jPanel1);
    }

    // -------------------------
    // 🔹 BOTÓN CONFIGURACIONES (temporal: debajo de Cerrar Sesión)
    // -------------------------
    if (btnConfiguraciones == null) {
        btnConfiguraciones = new javax.swing.JButton("Configuraciones");
        Estilos.estiloBotonMenuLateral(btnConfiguraciones);
    }

    // Tamaño y posición: colocarlo justo debajo de btnCerrarSesion (AbsoluteLayout)
    int ancho = 180;
    int alto  = 40;
    btnConfiguraciones.setSize(ancho, alto);

    if (btnCerrarSesion != null) {
        // obtener coordenadas del botón Cerrar Sesión ya posicionado por NetBeans
        int xCerrar = btnCerrarSesion.getX();
        int yCerrar = btnCerrarSesion.getY();
        int gap = 6; // separación entre botones

        int x = xCerrar;
        int y = yCerrar + btnCerrarSesion.getHeight() + gap;

        // fallback si las coordenadas son 0 (por seguridad)
        if (x <= 0) x = 20;
        if (y <= 0) y = 520;

        btnConfiguraciones.setBounds(x, y, ancho, alto);
    } else {
        // fallback: posición fija
        btnConfiguraciones.setBounds(20, 520, ancho, alto);
    }

    // Asegurar visibilidad y estilo
    btnConfiguraciones.setVisible(true);
    btnConfiguraciones.setOpaque(true);
    btnConfiguraciones.setContentAreaFilled(true);
    btnConfiguraciones.setBorderPainted(true);

    // Añadir al panel lateral y forzar repintado
    jPanel1.add(btnConfiguraciones);
    // Intentar llevar al frente para evitar que quede "detrás"
    try {
        jPanel1.setComponentZOrder(btnConfiguraciones, 0);
    } catch (Exception ex) {
        // si el layout no soporta Z-order, ignorar sin romper la UI
    }
    jPanel1.revalidate();
    jPanel1.repaint();

    // Listener para abrir la pestaña de Config Dispositivos
    btnConfiguraciones.addActionListener(evt -> {
        if (jPanelConfig == null) {
            System.out.println("DEBUG: jPanelConfig es null");
            return;
        }
        int index = Menu.indexOfComponent(jPanelConfig);
        if (index != -1) {
            System.out.println("DEBUG: btnConfiguraciones abre pestaña Config Dispositivos en índice " + index);
            Menu.setSelectedIndex(index);
            Menu.getComponentAt(index).revalidate();
            Menu.getComponentAt(index).repaint();
        } else {
            System.out.println("DEBUG: jPanelConfig no encontrado en Menu");
        }
    });
    
    
    // nuevo llama info empresa
    infoEmpresa = new InfoEmpresa(txtIdConfig, txtRucConfig, txtNombreConfig,
        txtTelefonoConfig, txtDireccionConfig, txtMensaje,
        () -> usuarioLogueado != null ? usuarioLogueado.getIdEmpresa() : 0);
    // -------------------------
    // 🔹 Estilos de botones laterales (incluye el nuevo)
    // -------------------------
    Estilos.estiloBotonMenuLateral(btnNuevaVenta);
    Estilos.estiloBotonMenuLateral(btnClientes);
    Estilos.estiloBotonMenuLateral(btnProveedor);
    Estilos.estiloBotonMenuLateral(btnProductos);
    Estilos.estiloBotonMenuLateral(btnVentas);
    Estilos.estiloBotonMenuLateral(btnConfig);
    Estilos.estiloBotonMenuLateral(jButton1);
    Estilos.estiloBotonMenuLateral(btnEntrada);
    Estilos.estiloBotonMenuLateral(BtnCreditoCliente);
    Estilos.estiloBotonMenuLateral(BtnCorte);
    Estilos.estiloBotonMenuLateral(btnCerrarSesion);
    Estilos.estiloBotonMenuLateral(btnConfiguraciones); // aplicar estilo al nuevo botón

    // -------------------------
    // 🔹 Inicializar teclas rápidas
    // -------------------------
    inicializarTeclas();

    // -------------------------
    // 🔹 Pestañas con validación
    // -------------------------
    pestañasConValidacion.add(1);
    pestañasConValidacion.add(2);
    pestañasConValidacion.add(3);
    pestañasConValidacion.add(4);
    pestañasConValidacion.add(5);
    pestañasConValidacion.add(6);
    pestañasConValidacion.add(7);

    // -------------------------
    // 🔹 Listener de cambio de pestañas (validación de corte)
    // -------------------------
    Menu.addChangeListener(evt -> {
        int selectedIndex = Menu.getSelectedIndex();

        if (selectedIndex == 2) {
            origenActual = "venta";
        } else if (selectedIndex == 7) {
            origenActual = "credito";
        }

        if (pestañasConValidacion.contains(selectedIndex)) {
            if (!corteValidado) {
                boolean acceso = rolController.solicitarClaveAdministrador();
                if (!acceso) {
                    Menu.setSelectedIndex(0);
                    return;
                }
                corteValidado = true;
            }
        } else {
            corteValidado = false;
        }
    });

    // -------------------------
    // 🔹 Panel embebido de Productos Moderno (con Cloudinary y Categorías)
    // -------------------------
    PanelProductos panelProductosNuevo = new PanelProductos(idEmpresaActiva, proDao, PrDao, this::refrescarPanelVerduleria);
    jPanel5.setLayout(new BorderLayout());
    jPanel5.removeAll();
    jPanel5.add(panelProductosNuevo, BorderLayout.CENTER);
    jPanel5.revalidate();
    jPanel5.repaint();
}

// Agrega este método
    public JPanel getPanelMenu() {
        return jPanel18;
    }
    private void abrirHistorialFiado(int dni, String nombre) {
    creditoClienteController.verHistorialDe(dni, nombre);
}
    //public void inicializarSistema(login priv) { //original
public void inicializarSistema(login priv) {

    this.usuarioLogueado = priv;
    sistemaYaIniciado = true; // Activamos la bandera

    System.out.println("DEBUG: Inicializando sistema para usuario -> " + priv.getNombre() + " con rol " + priv.getRol());

    // Configurar acceso rápido (mnemonic) para botón Buscar Producto
    btnBusccarPro.setMnemonic(KeyEvent.VK_X);

    // Llenar lista de proveedores
    llenarProveedor();

    // Aplicar estilos personalizados a tablas
    Estilos.estiloTablas(TableVenta);
    // Agregar listener para detectar cambio de pestaña en el menú (JTabbedPane)
    Menu.addChangeListener(evt -> {
        int selectedIndex = Menu.getSelectedIndex();

        if (selectedIndex == 2) {
            origenActual = "venta";
            System.out.println("DEBUG: origenActual = venta");
        } else if (selectedIndex == 7) {
            origenActual = "credito";
            System.out.println("DEBUG: origenActual = credito");
        }

        // Reseteamos la bandera de validación si cambiamos a una pestaña que no requiere validación
        if (!pestañasConValidacion.contains(selectedIndex)) {
            corteValidado = false;
            System.out.println("DEBUG: corteValidado reseteado porque se cambió de pestaña");
        }
    });

    // -------------------------------
    // 🔹 LIMPIAR PANEL DE CORTE DE CAJA
    // -------------------------------
    jPanelMenuCorte.removeAll();  // eliminamos botones existentes
    jPanelMenuCorte.repaint();    // refrescar panel

    // Configurar estilo y agregar botones nuevamente
    menuCorteCaja.aplicarEstilo(menu);
    menuCorteCaja.agregarBotonesAJPanel(jPanelMenuCorte, priv.getCorreo());
// --- RESTAURAR DISEÑO ORIGINAL DEL PANEL DE CORTE ---
// Llamar a esto justo después de menuCorteCaja.agregarBotonesAJPanel(...)
jPanelMenuCorte.removeAll(); // quitar todo lo que haya (wrappers, botones, etc.)
jPanelMenuCorte.repaint();

// Restaurar un layout neutro igual al original de initComponents
// Si en initComponents usabas otro layout (p.ej. AbsoluteLayout o FlowLayout),
// reemplaza new FlowLayout(...) por el layout original.
jPanelMenuCorte.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));

// Volver a aplicar estilo y repoblar con el método original
menuCorteCaja.aplicarEstilo(menu);
menuCorteCaja.agregarBotonesAJPanel(jPanelMenuCorte, priv.getCorreo());

// Forzar que los botones creados por agregarBotonesAJPanel mantengan su estilo original
for (java.awt.Component comp : jPanelMenuCorte.getComponents()) {
    if (comp instanceof javax.swing.JButton) {
        javax.swing.JButton btn = (javax.swing.JButton) comp;
        // Restaurar propiedades por defecto (evita bordes redondeados o tamaños forzados)
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(true);
        btn.setBorder(javax.swing.UIManager.getBorder("Button.border"));
        btn.setPreferredSize(null);
        btn.setMinimumSize(null);
        btn.setMaximumSize(null);
        btn.setFont(btn.getFont().deriveFont(java.awt.Font.PLAIN, btn.getFont().getSize()));
        // Si se cambió el icono, no lo tocamos; si quieres restaurar iconos, hazlo aquí.
    }
}

// Revalidar y repintar
jPanelMenuCorte.revalidate();
jPanelMenuCorte.repaint();

// Debug opcional (borra estas líneas si no las necesitas)
System.out.println("DEBUG: Restaurado jPanelMenuCorte. Componentes = " + jPanelMenuCorte.getComponentCount());
for (int i = 0; i < jPanelMenuCorte.getComponentCount(); i++) {
    java.awt.Component c = jPanelMenuCorte.getComponent(i);
    System.out.println("  comp[" + i + "] = " + c.getClass().getSimpleName() + " bounds=" + c.getBounds());
}


    // Ajustes adicionales para interfaz: enfocar código de venta
    Menu.setSelectedIndex(0);
    Menu.getComponentAt(0).setBackground(Color.WHITE);

    txtCodigoVenta.requestFocus();
    //
    setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

    // Inicializar fechas en JDateChooser
    Midate.setDate(fechaVenta);
    Midate1.setDate(fechaVenta);

    // Ocultar campos innecesarios al iniciar
    txtIdCliente.setVisible(false);
    txtIdVenta.setVisible(false);
    txtIdPro.setVisible(false);
    txtIdproducto.setVisible(false);
    txtIDProduct.setVisible(false);
    txtIdProveedor.setVisible(false);
    txtIdConfig.setVisible(false);
    txtIdCV.setVisible(false);
    txtCantidadVenta.setVisible(false);
    jLabel5.setVisible(false);
    jLabel4.setVisible(false);
    txtDescripcionVenta.setVisible(false);
    jLabel6.setVisible(false);
    txtPrecioVenta.setVisible(false);
    jLabel7.setVisible(false);
    txtStockDisponible.setVisible(false);
    txtPaga1.setVisible(false);
    jLabel39.setVisible(false);
    jLabel40.setVisible(false);
    btnGenerarVenta.setVisible(false);
    txtCantidadVenta1.setVisible(false);
    txtPrecioVenta1.setVisible(false);
    jLabel56.setVisible(false);
    jLabel55.setVisible(false);
    txtStockDisponible2.setVisible(false);
    jLabel57.setVisible(false);
    txtRucVenta.setVisible(false);
    txtNombreClienteventa.setVisible(false);
    jLabel9.setVisible(false);
    jLabel8.setVisible(false);
    btnGraficar.setVisible(false);
    jLabel11.setVisible(false);
    Midate.setVisible(false);

    // Mostrar botones específicos
    btnTodasFilas.setVisible(true);
    BtnCreditoCliente.setVisible(true);
    TotalEntrada.setVisible(true);
    menu.setVisible(false);

    // Inicializar teclas de acceso rápido
    inicializarTeclas();

    // Hacer visible la ventana
    setVisible(true);

    // Configurar botones y etiquetas según rol del usuario
    if (priv.getRol().equals("Asistente")) {
        btnProductos.setEnabled(true);
        btnProveedor.setEnabled(true);
    }
    LabelVendedor.setText(priv.getNombre());

    // Mostrar pestaña de "Nueva Venta" al iniciar sesión
    Menu.setSelectedIndex(0);
    System.out.println("➡️ Forzamos ir a pestaña VENTAS (índice 0)");

    // Solicitar foco en el campo de código de venta
    txtCodigoVenta.requestFocus();

    // Log final para confirmar que todo se inicializó
    System.out.println("DEBUG: inicializarSistema completado. Panel lateral tiene " + jPanel1.getComponentCount() + " componentes.");
// 🔹 CARGAR DATOS EN EL PANEL DE VERDULERÍA YA EXISTENTE (`panelVerduleria`)
refrescarPanelVerduleria();
cargarPromosVigentes();
activarAjusteAutomatico();  
} 
private void refrescarPanelVerduleria() {
    try {
        List<Productos> listaPro = proDao.ListarProductos(idEmpresaActiva);
        cacheProductosEmpresa = listaPro; // 👈 agregar esta línea
        Map<String, Productos> mapaProductosVerduleria = new HashMap<>();
        List<String> nombresVerduleria = new ArrayList<>();

        if (listaPro != null) {
            for (Productos p : listaPro) {
                if (p.getCategoria() != null && "verduleria".equalsIgnoreCase(p.getCategoria().trim())) {
                    mapaProductosVerduleria.put(p.getNombre(), p);
                    nombresVerduleria.add(p.getNombre());
                }
            }
        }

        if (panelVerduleria != null) {
            panelVerduleria.cargarProductos(nombresVerduleria, mapaProductosVerduleria);
            panelVerduleria.revalidate();
            panelVerduleria.repaint();
        }

        jPanel2.revalidate();
        jPanel2.repaint();

        System.out.println("DEBUG-VERDULERIA: Panel de verdulería actualizado con " + nombresVerduleria.size() + " productos.");
    } catch (Exception e) {
        System.out.println("Error al refrescar productos en el panel de verdulería: " + e.getMessage());
        e.printStackTrace();
    }
}   
private BannersCreditoCliente bannersFiados;
private Productos buscarProductoVerduleria(String texto) {
    if (cacheProductosEmpresa == null || texto == null || texto.trim().isEmpty()) return null;
    String t = texto.trim();

    for (Productos p : cacheProductosEmpresa) {
        if (p.getCategoria() == null || !"verduleria".equalsIgnoreCase(p.getCategoria().trim())) {
            continue; // solo nos interesan los de Verdulería
        }
        boolean coincideCodigo = p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(t);
        boolean coincideId = false;
        try {
            coincideId = p.getId() == Integer.parseInt(t);
        } catch (NumberFormatException ignored) {}

        if (coincideCodigo || coincideId) {
            return p;
        }
    }
    return null;
}
public void abrirDetalleVerduleria(Productos producto) {
    Menu.setSelectedIndex(0); // pestaña Nueva Venta, donde vive panelVerduleria
    if (panelVerduleria != null) {
        panelVerduleria.mostrarDetalle(producto);
    }
}
public Controlador.CreditoClienteController getCreditoClienteController() {
    return creditoClienteController;
}
    public void nuevatabla(){
    
    modelo3=new DefaultTableModel();
    TableVenta.setModel(modelo3);
    }
    
 private void activarAjusteAutomatico() {
    if (ajusteActivado) return;          // evita duplicar listeners si se llama otra vez
    ajusteActivado = true;

    final java.awt.Container cp = getContentPane();
    final java.util.Map<java.awt.Component, java.awt.Rectangle> originales = new java.util.HashMap<>();
    int maxX = 0, maxY = 0;

    for (java.awt.Component c : cp.getComponents()) {
        java.awt.Rectangle r = c.getBounds();
        originales.put(c, r);
        maxX = Math.max(maxX, r.x + r.width);
        maxY = Math.max(maxY, r.y + r.height);
    }
    final int baseW = maxX;
    final int baseH = maxY;

    final Runnable ajustar = () -> {
        int dw = Math.max(0, cp.getWidth() - baseW);
        int dh = Math.max(0, cp.getHeight() - baseH);

        for (java.awt.Component c : cp.getComponents()) {
            java.awt.Rectangle r = originales.get(c);
            if (r == null) continue;

            boolean pegadoIzquierda = r.x == 0 && r.width < 300;   // menú lateral
            boolean franjaSuperior = r.height < 200;                // encabezado

            if (pegadoIzquierda) {
                c.setBounds(r.x, r.y, r.width, r.height + dh);               // crece a lo alto
            } else if (franjaSuperior) {
                c.setBounds(r.x, r.y, r.width + dw, r.height);               // crece a lo ancho
            } else {
                c.setBounds(r.x, r.y, r.width + dw, r.height + dh);          // contenido: ancho y alto
            }
        }
        cp.revalidate();
        cp.repaint();
    };

    cp.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            ajustar.run();
        }
    });
    ajustar.run();
}
  public void ListarCliente(int idEmpresa) {
    List<Cliente> ListarCl = client.ListarCliente(idEmpresa); // ahora recibe idEmpresa
    modelo = (DefaultTableModel) TableCliente.getModel();
    modelo.setRowCount(0); // 🔹 limpiar la tabla antes de llenarla
    Object[] ob = new Object[6];

    for (int i = 0; i < ListarCl.size(); i++) {
        ob[0] = ListarCl.get(i).getId();
        ob[1] = ListarCl.get(i).getDni();
        ob[2] = ListarCl.get(i).getNombre();
        ob[3] = ListarCl.get(i).getTelefono();
        ob[4] = ListarCl.get(i).getDireccion();
        ob[5] = ListarCl.get(i).getIdEmpresa(); // 🔹 mostrar id_empresa si lo quieres ver
        modelo.addRow(ob);
    }
    TableCliente.setModel(modelo);
}


  public void ListarProveedor(int idEmpresa) {
    List<Proveedor> ListarPr = PrDao.ListarProveedorPorEmpresa(idEmpresa); // <-- filtrado por empresa
    modelo = (DefaultTableModel) TableProveedor.getModel();
    
    // Limpiar la tabla antes de llenar
    modelo.setRowCount(0);

    Object[] ob = new Object[5];
    for (int i = 0; i < ListarPr.size(); i++) {
        ob[0] = ListarPr.get(i).getId();
        ob[1] = ListarPr.get(i).getRuc();
        ob[2] = ListarPr.get(i).getNombre();
        ob[3] = ListarPr.get(i).getTelefono();
        ob[4] = ListarPr.get(i).getDireccion();
        modelo.addRow(ob);
    }
    TableProveedor.setModel(modelo);
}

 public void ListarUsuarios() {
    // 🔹 Usar la empresa activa desde Sistema
    int idEmpresa = Sistema.getIdEmpresaActiva(); 
    List<login> Listar = login.ListarUsuarios(idEmpresa); // PASAMOS EL ARGUMENTO
    modelo = (DefaultTableModel) TableUsuarios.getModel();
    Object[] ob = new Object[4];
    for (int i = 0; i < Listar.size(); i++) {
        ob[0] = Listar.get(i).getId();
        ob[1] = Listar.get(i).getNombre();
        ob[2] = Listar.get(i).getCorreo();
        ob[3] = Listar.get(i).getRol();
        modelo.addRow(ob);
    }
    TableUsuarios.setModel(modelo);
}

    
public void ListarConfig() {
    infoEmpresa.cargar();
}


    public void LimpiarTable() {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            modelo.removeRow(i);
            i = i - 1;
        }
    }

    @SuppressWarnings("unchecked")
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnNuevaVenta = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnProveedor = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnConfig = new javax.swing.JButton();
        LabelVendedor = new javax.swing.JLabel();
        tipo = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnEntrada = new javax.swing.JButton();
        BtnCreditoCliente = new javax.swing.JButton();
        BtnCorte = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        Menu = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtCodigoVenta = new javax.swing.JTextField();
        txtDescripcionVenta = new javax.swing.JTextField();
        txtCantidadVenta = new javax.swing.JTextField();
        txtPrecioVenta = new javax.swing.JTextField();
        txtStockDisponible = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableVenta = new javax.swing.JTable();
        btnEliminarventa = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtRucVenta = new javax.swing.JTextField();
        txtNombreClienteventa = new javax.swing.JTextField();
        btnGenerarVenta = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        lblEnviaTotal = new javax.swing.JLabel();
        txtIdCV = new javax.swing.JTextField();
        txtIdPro = new javax.swing.JTextField();
        btnGraficar = new javax.swing.JButton();
        Midate = new com.toedter.calendar.JDateChooser();
        jLabel11 = new javax.swing.JLabel();
        txtPaga1 = new javax.swing.JTextField();
        lblcambio = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        btnTodasFilas = new javax.swing.JButton();
        btnCobrar = new javax.swing.JButton();
        btnBusccarPro = new javax.swing.JButton();
        txtIDProduct = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableCliente = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        txtDniCliente = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtNombreCliente = new javax.swing.JTextField();
        txtTelefonoCliente = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtDirecionCliente = new javax.swing.JTextField();
        txtIdCliente = new javax.swing.JTextField();
        btnGuardarCliente = new javax.swing.JButton();
        btnEditarCliente = new javax.swing.JButton();
        btnEliminarCliente = new javax.swing.JButton();
        btnNuevoCliente = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TableProveedor = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        txtRucProveedor = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtNombreproveedor = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtTelefonoProveedor = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtDireccionProveedor = new javax.swing.JTextField();
        btnguardarProveedor = new javax.swing.JButton();
        btnEditarProveedor = new javax.swing.JButton();
        btnNuevoProveedor = new javax.swing.JButton();
        btnEliminarProveedor = new javax.swing.JButton();
        txtIdProveedor = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        txtIdproducto = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        txtCodigoPro = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        txtDesPro = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtCantPro = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtPrecioPro = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        cbxProveedorPro = new javax.swing.JComboBox<>();
        btnGuardarpro = new javax.swing.JButton();
        btnEditarpro = new javax.swing.JButton();
        btnEliminarPro = new javax.swing.JButton();
        btnNuevoPro = new javax.swing.JButton();
        txtPreciocompraPro = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        TableProducto = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TableVentas = new javax.swing.JTable();
        btnPdfVentas = new javax.swing.JButton();
        txtIdVenta = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        txtRucConfig = new javax.swing.JTextField();
        txtNombreConfig = new javax.swing.JTextField();
        txtTelefonoConfig = new javax.swing.JTextField();
        txtDireccionConfig = new javax.swing.JTextField();
        txtMensaje = new javax.swing.JTextField();
        btnActualizarConfig = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        txtIdConfig = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        txtPass = new javax.swing.JPasswordField();
        btnIniciar = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        cbxRol = new javax.swing.JComboBox<>();
        jScrollPane6 = new javax.swing.JScrollPane();
        TableUsuarios = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        txtCodigoEntrada = new javax.swing.JTextField();
        txtDescripcionEntrada = new javax.swing.JTextField();
        txtCantidadEntrada = new javax.swing.JTextField();
        txtPrecioEntrada = new javax.swing.JTextField();
        txtStockDisponible1 = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        TableEntrada = new javax.swing.JTable();
        btnEliminarentrada = new javax.swing.JButton();
        btnGenerarEntrada = new javax.swing.JButton();
        txtIdPro1 = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        lblcambio1 = new javax.swing.JLabel();
        TotalEntrada = new javax.swing.JLabel();
        Midate1 = new com.toedter.calendar.JDateChooser();
        jPanel17 = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        txtCodigoVentaCreditClient = new javax.swing.JTextField();
        txtCantidadVenta1 = new javax.swing.JTextField();
        txtPrecioVenta1 = new javax.swing.JTextField();
        txtStockDisponible2 = new javax.swing.JTextField();
        btnEliminarventa1 = new javax.swing.JButton();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        txtRucVentaCredit = new javax.swing.JTextField();
        txtNombreClienteventaCredit = new javax.swing.JTextField();
        jLabel60 = new javax.swing.JLabel();
        lblTotalCredit = new javax.swing.JLabel();
        txtIdCV1 = new javax.swing.JTextField();
        txtIdPro2 = new javax.swing.JTextField();
        lblcambioCredit = new javax.swing.JLabel();
        btnBusccarPro1 = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        TableCreditClient = new javax.swing.JTable();
        jLabel46 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        btnGenerarVentaCredit = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jPanelMenuCorte = new javax.swing.JPanel();
        menu = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1070, 660));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(69, 8, 206));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/LOGO-VHAO-SYSTEM.png"))); // NOI18N

        btnNuevaVenta.setBackground(new java.awt.Color(153, 153, 153));
        btnNuevaVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnNuevaVenta.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevaVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Nventa.png"))); // NOI18N
        btnNuevaVenta.setText("Nueva Venta");
        btnNuevaVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnNuevaVenta.setFocusable(false);
        btnNuevaVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaVentaActionPerformed(evt);
            }
        });

        btnClientes.setBackground(new java.awt.Color(153, 153, 153));
        btnClientes.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes.png"))); // NOI18N
        btnClientes.setText("Clientes Activos");
        btnClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnClientes.setFocusable(false);
        btnClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClientesActionPerformed(evt);
            }
        });

        btnProveedor.setBackground(new java.awt.Color(153, 153, 153));
        btnProveedor.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnProveedor.setForeground(new java.awt.Color(255, 255, 255));
        btnProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor.png"))); // NOI18N
        btnProveedor.setText("Proveedor");
        btnProveedor.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnProveedor.setFocusable(false);
        btnProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedorActionPerformed(evt);
            }
        });

        btnProductos.setBackground(new java.awt.Color(153, 153, 153));
        btnProductos.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnProductos.setForeground(new java.awt.Color(255, 255, 255));
        btnProductos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        btnProductos.setText("Catalogo de Productos");
        btnProductos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnProductos.setFocusable(false);
        btnProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnProductosMouseClicked(evt);
            }
        });
        btnProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductosActionPerformed(evt);
            }
        });

        btnVentas.setBackground(new java.awt.Color(153, 153, 153));
        btnVentas.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnVentas.setForeground(new java.awt.Color(255, 255, 255));
        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/compras.png"))); // NOI18N
        btnVentas.setText("Historial de Ventas");
        btnVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnVentas.setFocusable(false);
        btnVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentasActionPerformed(evt);
            }
        });

        btnConfig.setBackground(new java.awt.Color(153, 153, 153));
        btnConfig.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnConfig.setForeground(new java.awt.Color(255, 255, 255));
        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnConfig.setText("Config");
        btnConfig.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnConfig.setFocusable(false);
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });

        LabelVendedor.setForeground(new java.awt.Color(255, 255, 255));
        LabelVendedor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelVendedor.setText("Vida Informatico");

        tipo.setForeground(new java.awt.Color(255, 255, 255));

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Usuarios");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnEntrada.setBackground(new java.awt.Color(153, 153, 153));
        btnEntrada.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        btnEntrada.setForeground(new java.awt.Color(255, 255, 255));
        btnEntrada.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Nventa.png"))); // NOI18N
        btnEntrada.setText("Entrada");
        btnEntrada.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnEntrada.setFocusable(false);
        btnEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntradaActionPerformed(evt);
            }
        });

        BtnCreditoCliente.setBackground(new java.awt.Color(153, 153, 153));
        BtnCreditoCliente.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        BtnCreditoCliente.setForeground(new java.awt.Color(255, 255, 255));
        BtnCreditoCliente.setText("Credito Cliente");
        BtnCreditoCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        BtnCreditoCliente.setFocusable(false);
        BtnCreditoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCreditoClienteActionPerformed(evt);
            }
        });

        BtnCorte.setBackground(new java.awt.Color(153, 153, 153));
        BtnCorte.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        BtnCorte.setForeground(new java.awt.Color(255, 255, 255));
        BtnCorte.setText("Corte de caja");
        BtnCorte.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        BtnCorte.setFocusable(false);
        BtnCorte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCorteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnNuevaVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnProveedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(LabelVendedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnEntrada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(BtnCreditoCliente, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(BtnCorte, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(tipo))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(jLabel1)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(34, 34, 34)
                .addComponent(LabelVendedor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tipo)
                .addGap(8, 8, 8)
                .addComponent(btnNuevaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BtnCreditoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BtnCorte, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 199, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 780));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/encabezado.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 1030, 120));

        Menu.setBackground(new java.awt.Color(255, 255, 255));
        Menu.setPreferredSize(new java.awt.Dimension(865, 660));

        jPanel2.setBackground(new java.awt.Color(102, 255, 204));
        jPanel2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel2KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jPanel2KeyTyped(evt);
            }
        });
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Código");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Descripción");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, -1, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Cant");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 50, -1, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Precio");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 255));
        jLabel7.setText("Stock Disponible");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        txtCodigoVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCodigoVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoVentaActionPerformed(evt);
            }
        });
        txtCodigoVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoVentaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtCodigoVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 210, 40));

        txtDescripcionVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtDescripcionVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDescripcionVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtDescripcionVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, 150, 30));

        txtCantidadVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCantidadVenta.setText("1");
        txtCantidadVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadVentaActionPerformed(evt);
            }
        });
        txtCantidadVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtCantidadVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, 40, 30));

        txtPrecioVenta.setEditable(false);
        txtPrecioVenta.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtPrecioVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecioVentaActionPerformed(evt);
            }
        });
        jPanel2.add(txtPrecioVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 70, 80, 30));

        txtStockDisponible.setEditable(false);
        jPanel2.add(txtStockDisponible, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 79, 30));

        TableVenta.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        TableVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "DESCRIPCIÓN", "CANTIDAD", "PRECIO U.", "PRECIO TOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableVenta.setRowHeight(25);
        TableVenta.getTableHeader().setReorderingAllowed(false);
        TableVenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableVentaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                TableVentaMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                TableVentaMousePressed(evt);
            }
        });
        TableVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TableVentaKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(TableVenta);
        if (TableVenta.getColumnModel().getColumnCount() > 0) {
            TableVenta.getColumnModel().getColumn(0).setMinWidth(80);
            TableVenta.getColumnModel().getColumn(0).setMaxWidth(100);
            TableVenta.getColumnModel().getColumn(1).setResizable(false);
            TableVenta.getColumnModel().getColumn(2).setMinWidth(50);
            TableVenta.getColumnModel().getColumn(2).setMaxWidth(120);
            TableVenta.getColumnModel().getColumn(3).setMinWidth(100);
            TableVenta.getColumnModel().getColumn(3).setMaxWidth(150);
            TableVenta.getColumnModel().getColumn(4).setMinWidth(100);
            TableVenta.getColumnModel().getColumn(4).setMaxWidth(150);
        }

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, 1030, 310));

        btnEliminarventa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarventa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarventaActionPerformed(evt);
            }
        });
        jPanel2.add(btnEliminarventa, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 110, -1, 40));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel8.setText("Dni");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 480, -1, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel9.setText("Cliente:");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 490, -1, -1));

        txtRucVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtRucVenta.setText("1");
        txtRucVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRucVentaActionPerformed(evt);
            }
        });
        txtRucVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRucVentaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRucVentaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRucVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtRucVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 510, 116, 30));

        txtNombreClienteventa.setEditable(false);
        txtNombreClienteventa.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtNombreClienteventa.setText("DEFAULT");
        jPanel2.add(txtNombreClienteventa, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 520, 169, 30));

        btnGenerarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/print.png"))); // NOI18N
        btnGenerarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarVentaActionPerformed(evt);
            }
        });
        jPanel2.add(btnGenerarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 580, -1, 45));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel10.setText("Total a Pagar:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 490, -1, -1));

        lblEnviaTotal.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblEnviaTotal.setText("-----");
        jPanel2.add(lblEnviaTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 490, -1, -1));

        txtIdCV.setText("1");
        jPanel2.add(txtIdCV, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 530, 20, -1));
        jPanel2.add(txtIdPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(678, 126, -1, -1));

        btnGraficar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/torta.png"))); // NOI18N
        btnGraficar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficarActionPerformed(evt);
            }
        });
        jPanel2.add(btnGraficar, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 60, -1, -1));

        Midate.setDateFormatString("d/MM/yyyy HH:mm");
        jPanel2.add(Midate, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 70, 210, 30));

        jLabel11.setText("Seleccionar:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 50, -1, -1));

        txtPaga1.setFont(new java.awt.Font("Tahoma", 0, 22)); // NOI18N
        txtPaga1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPaga1ActionPerformed(evt);
            }
        });
        txtPaga1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPaga1KeyReleased(evt);
            }
        });
        jPanel2.add(txtPaga1, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 570, 70, 30));

        lblcambio.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jPanel2.add(lblcambio, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 530, 60, 20));

        jLabel39.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel39.setText("Paga con:");
        jPanel2.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 570, -1, -1));

        jLabel40.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel40.setText("su cambio:");
        jPanel2.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 530, -1, -1));

        btnTodasFilas.setText("Credito Cliente");
        btnTodasFilas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTodasFilasActionPerformed(evt);
            }
        });
        jPanel2.add(btnTodasFilas, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 570, 120, 40));

        btnCobrar.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnCobrar.setText("cobrar");
        btnCobrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCobrarMouseClicked(evt);
            }
        });
        btnCobrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCobrarActionPerformed(evt);
            }
        });
        btnCobrar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCobrarKeyPressed(evt);
            }
        });
        jPanel2.add(btnCobrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 540, 90, 40));

        btnBusccarPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa.png"))); // NOI18N
        btnBusccarPro.setMnemonic('x');
        btnBusccarPro.setText("buscar");
        btnBusccarPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBusccarProActionPerformed(evt);
            }
        });
        jPanel2.add(btnBusccarPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 70, 110, 40));

        txtIDProduct.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtIDProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtIDProductKeyTyped(evt);
            }
        });
        jPanel2.add(txtIDProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 10, 150, 30));

        Menu.addTab("1", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "DNI", "NOMBRE", "TELÉFONO", "DIRECCIÓN"
            }
        ));
        TableCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableClienteMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(TableCliente);
        if (TableCliente.getColumnModel().getColumnCount() > 0) {
            TableCliente.getColumnModel().getColumn(0).setPreferredWidth(10);
            TableCliente.getColumnModel().getColumn(1).setPreferredWidth(50);
            TableCliente.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableCliente.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableCliente.getColumnModel().getColumn(4).setPreferredWidth(80);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 50, 555, 330));

        jPanel9.setBackground(new java.awt.Color(204, 204, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Registro Cliente"));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setText("Dni:");

        txtDniCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDniClienteKeyTyped(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Nombre:");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel14.setText("Télefono:");

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel15.setText("Dirección:");

        btnGuardarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnGuardarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarClienteActionPerformed(evt);
            }
        });

        btnEditarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnEditarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarClienteActionPerformed(evt);
            }
        });

        btnEliminarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnEliminarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarClienteActionPerformed(evt);
            }
        });

        btnNuevoCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnNuevoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoClienteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(46, 46, 46)
                                .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(40, 40, 40)
                                .addComponent(txtTelefonoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(38, 38, 38)
                                .addComponent(txtDirecionCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(btnGuardarCliente)
                                .addGap(39, 39, 39)
                                .addComponent(btnEditarCliente))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(btnEliminarCliente)
                                .addGap(39, 39, 39)
                                .addComponent(btnNuevoCliente)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtDniCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtDniCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel13))
                    .addComponent(txtNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel14))
                    .addComponent(txtTelefonoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel15))
                    .addComponent(txtDirecionCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGuardarCliente)
                    .addComponent(btnEditarCliente))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEliminarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevoCliente))
                .addGap(29, 29, 29))
        );

        jPanel3.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 270, 330));

        Menu.addTab("2", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableProveedor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "RUC", "NOMBRE", "TELÉFONO", "DIRECCIÓN"
            }
        ));
        TableProveedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableProveedorMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(TableProveedor);
        if (TableProveedor.getColumnModel().getColumnCount() > 0) {
            TableProveedor.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableProveedor.getColumnModel().getColumn(1).setPreferredWidth(40);
            TableProveedor.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableProveedor.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableProveedor.getColumnModel().getColumn(4).setPreferredWidth(80);
        }

        jPanel4.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 57, 558, 310));

        jPanel10.setBackground(new java.awt.Color(255, 204, 204));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevo Proveedor"));

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel17.setText("Rfc:");

        jLabel18.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel18.setText("Nombre:");

        jLabel19.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel19.setText("Teléfono:");

        jLabel20.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel20.setText("Dirección:");

        btnguardarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnguardarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnguardarProveedorActionPerformed(evt);
            }
        });

        btnEditarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarProveedorActionPerformed(evt);
            }
        });

        btnNuevoProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProveedorActionPerformed(evt);
            }
        });

        btnEliminarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProveedorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addComponent(btnEliminarProveedor)
                                        .addGap(73, 73, 73)
                                        .addComponent(btnNuevoProveedor))
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addComponent(btnguardarProveedor)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnEditarProveedor))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel17))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNombreproveedor, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                    .addComponent(txtRucProveedor)))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel20))
                                .addGap(24, 24, 24)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtTelefonoProveedor)
                                    .addComponent(txtDireccionProveedor))))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(txtIdProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(99, 99, 99))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtRucProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtNombreproveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txtTelefonoProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel20))
                    .addComponent(txtDireccionProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(txtIdProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnguardarProveedor)
                    .addComponent(btnEditarProveedor))
                .addGap(17, 17, 17)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEliminarProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevoProveedor))
                .addGap(26, 26, 26))
        );

        jPanel4.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 260, 320));

        Menu.addTab("3", jPanel4);

        jPanel5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPanel5KeyReleased(evt);
            }
        });
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(txtIdproducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(223, 25, -1, -1));

        jPanel11.setBackground(new java.awt.Color(255, 204, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevo Producto"));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel22.setText("Código:");

        txtCodigoPro.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCodigoPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoProActionPerformed(evt);
            }
        });
        txtCodigoPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoProKeyPressed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel23.setText("Descripción:");

        txtDesPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDesProActionPerformed(evt);
            }
        });
        txtDesPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDesProKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDesProKeyTyped(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel24.setText("Cantidad:");

        txtCantPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantProActionPerformed(evt);
            }
        });
        txtCantPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantProKeyTyped(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel25.setText("Precio Venta:");

        txtPrecioPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPrecioProKeyTyped(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel26.setText("Proveedor:");

        cbxProveedorPro.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxProveedorProItemStateChanged(evt);
            }
        });
        cbxProveedorPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxProveedorProActionPerformed(evt);
            }
        });

        btnGuardarpro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarpro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarproActionPerformed(evt);
            }
        });

        btnEditarpro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarpro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarproActionPerformed(evt);
            }
        });

        btnEliminarPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProActionPerformed(evt);
            }
        });

        btnNuevoPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProActionPerformed(evt);
            }
        });

        txtPreciocompraPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPreciocompraProActionPerformed(evt);
            }
        });
        txtPreciocompraPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPreciocompraProKeyTyped(evt);
            }
        });

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel47.setText("Precio Compra:");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel26)
                        .addGap(192, 192, 192))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel24)
                            .addComponent(jLabel22))
                        .addGap(22, 22, 22)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDesPro, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCodigoPro, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel47))
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cbxProveedorPro, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPreciocompraPro, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPrecioPro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCantPro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(24, Short.MAX_VALUE))))))
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(btnGuardarpro)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarpro)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEliminarPro)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNuevoPro)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(txtCodigoPro, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtDesPro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(txtCantPro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(txtPrecioPro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPreciocompraPro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel47))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxProveedorPro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnGuardarpro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEditarpro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEliminarPro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNuevoPro, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        jPanel5.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 270, 360));

        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBuscarKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarKeyTyped(evt);
            }
        });
        jPanel5.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 0, 260, 30));

        jLabel45.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel45.setText("Buscar ");
        jPanel5.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 0, -1, -1));

        TableProducto.setFont(new java.awt.Font("Swis721 Lt BT", 0, 16)); // NOI18N
        TableProducto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CODIGO", "DESCRIPCIÓN", "PROVEEDOR", "STOCK", "PRECIO", "Precio Compra", "IdProveedor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableProductoMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(TableProducto);
        if (TableProducto.getColumnModel().getColumnCount() > 0) {
            TableProducto.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableProducto.getColumnModel().getColumn(1).setPreferredWidth(50);
            TableProducto.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableProducto.getColumnModel().getColumn(3).setPreferredWidth(60);
            TableProducto.getColumnModel().getColumn(4).setPreferredWidth(40);
            TableProducto.getColumnModel().getColumn(5).setPreferredWidth(50);
        }

        jPanel5.add(jScrollPane10, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 50, 790, 330));

        Menu.addTab("4", jPanel5);

        jPanel6.setBackground(new java.awt.Color(255, 255, 102));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CLIENTE", "VENDEDOR", "TOTAL"
            }
        ));
        TableVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableVentasMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(TableVentas);
        if (TableVentas.getColumnModel().getColumnCount() > 0) {
            TableVentas.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableVentas.getColumnModel().getColumn(1).setPreferredWidth(60);
            TableVentas.getColumnModel().getColumn(2).setPreferredWidth(60);
            TableVentas.getColumnModel().getColumn(3).setPreferredWidth(60);
        }

        jPanel6.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 910, 460));

        btnPdfVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        btnPdfVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfVentasActionPerformed(evt);
            }
        });
        jPanel6.add(btnPdfVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));
        jPanel6.add(txtIdVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, 46, -1));

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Historial Ventas");
        jPanel6.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 50, 280, -1));

        Menu.addTab("5", jPanel6);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel27.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel27.setText("RFC");
        jPanel7.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        jLabel28.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel28.setText("NOMBRE");
        jPanel7.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 100, -1, -1));

        jLabel29.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel29.setText("TELÉFONO");
        jPanel7.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, -1, -1));

        jLabel30.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel30.setText("DIRECCIÓN");
        jPanel7.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel31.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel31.setText("MENSAJE");
        jPanel7.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, -1));
        jPanel7.add(txtRucConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 147, 30));
        jPanel7.add(txtNombreConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 220, 30));
        jPanel7.add(txtTelefonoConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 200, 218, 30));
        jPanel7.add(txtDireccionConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 147, 30));
        jPanel7.add(txtMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 400, 30));

        btnActualizarConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnActualizarConfig.setText("ACTUALIZAR");
        btnActualizarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarConfigActionPerformed(evt);
            }
        });
        jPanel7.add(btnActualizarConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 320, -1, 35));

        jLabel32.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel32.setText("DATOS DE LA EMPRESA");
        jPanel7.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, -1, -1));

        jPanel8.setBackground(new java.awt.Color(153, 255, 204));

        txtIdConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(351, Short.MAX_VALUE)
                .addComponent(txtIdConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(263, Short.MAX_VALUE)
                .addComponent(txtIdConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        jPanel7.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 420, 310));

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/empresa.png"))); // NOI18N
        jPanel7.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 100, 410, 290));

        Menu.addTab("6", jPanel7);

        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel13.setBackground(new java.awt.Color(255, 204, 255));

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/iniciar.png"))); // NOI18N

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(0, 0, 255));
        jLabel34.setText("Correo Electrónico");

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(0, 0, 255));
        jLabel35.setText("Password");

        txtCorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCorreoActionPerformed(evt);
            }
        });

        btnIniciar.setBackground(new java.awt.Color(0, 0, 204));
        btnIniciar.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciar.setText("Registrar");
        btnIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(0, 0, 255));
        jLabel36.setText("Nombre:");

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(0, 0, 255));
        jLabel37.setText("Rol:");

        cbxRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Asistente" }));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel36)
                            .addComponent(jLabel35)
                            .addComponent(jLabel34)
                            .addComponent(txtCorreo, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                            .addComponent(txtPass)
                            .addComponent(jLabel37)
                            .addComponent(txtNombre)
                            .addComponent(cbxRol, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addGap(18, 18, 18)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addGap(2, 2, 2)
                .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxRol, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel12.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 280, 380));

        TableUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Nombre", "Correo", "Rol"
            }
        ));
        TableUsuarios.setRowHeight(20);
        jScrollPane6.setViewportView(TableUsuarios);

        jPanel12.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, 540, 380));

        Menu.addTab("7", jPanel12);

        jPanel14.setBackground(new java.awt.Color(204, 204, 204));
        jPanel14.setMinimumSize(new java.awt.Dimension(1000, 1000));
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel38.setText("Código");
        jPanel14.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel41.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel41.setText("Descripción");
        jPanel14.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, -1, -1));

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel42.setText("Cant");
        jPanel14.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, -1, -1));

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel43.setText("Precio");
        jPanel14.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 50, -1, -1));

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(0, 0, 255));
        jLabel44.setText("Stock Disponible");
        jPanel14.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        txtCodigoEntrada.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        txtCodigoEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoEntradaActionPerformed(evt);
            }
        });
        txtCodigoEntrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoEntradaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoEntradaKeyTyped(evt);
            }
        });
        jPanel14.add(txtCodigoEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 102, 30));

        txtDescripcionEntrada.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        txtDescripcionEntrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDescripcionEntradaKeyTyped(evt);
            }
        });
        jPanel14.add(txtDescripcionEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, 191, 30));

        txtCantidadEntrada.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        txtCantidadEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadEntradaActionPerformed(evt);
            }
        });
        txtCantidadEntrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadEntradaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCantidadEntradaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadEntradaKeyTyped(evt);
            }
        });
        jPanel14.add(txtCantidadEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 70, 40, 30));

        txtPrecioEntrada.setEditable(false);
        jPanel14.add(txtPrecioEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 70, 80, 30));

        txtStockDisponible1.setEditable(false);
        txtStockDisponible1.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jPanel14.add(txtStockDisponible1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 79, 30));

        TableEntrada.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        TableEntrada.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "DESCRIPCIÓN", "CANTIDAD", "PRECIO U.", "PRECIO TOTAL"
            }
        ));
        TableEntrada.setRowHeight(25);
        jScrollPane7.setViewportView(TableEntrada);
        if (TableEntrada.getColumnModel().getColumnCount() > 0) {
            TableEntrada.getColumnModel().getColumn(0).setPreferredWidth(60);
            TableEntrada.getColumnModel().getColumn(1).setPreferredWidth(100);
            TableEntrada.getColumnModel().getColumn(2).setPreferredWidth(40);
            TableEntrada.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableEntrada.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jPanel14.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 843, 191));

        btnEliminarentrada.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarentrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarentradaActionPerformed(evt);
            }
        });
        jPanel14.add(btnEliminarentrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 110, -1, 40));

        btnGenerarEntrada.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/print.png"))); // NOI18N
        btnGenerarEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarEntradaActionPerformed(evt);
            }
        });
        jPanel14.add(btnGenerarEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 440, -1, 45));
        jPanel14.add(txtIdPro1, new org.netbeans.lib.awtextra.AbsoluteConstraints(678, 126, -1, -1));

        jLabel48.setText("Seleccionar:");
        jPanel14.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 50, -1, -1));
        jPanel14.add(lblcambio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 350, 60, 20));

        TotalEntrada.setText("-----");
        jPanel14.add(TotalEntrada, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 400, -1, -1));
        jPanel14.add(Midate1, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 70, 210, 30));

        Menu.addTab("1", jPanel14);

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel17KeyPressed(evt);
            }
        });
        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel53.setText("Código");
        jPanel17.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel55.setText("Cant");
        jPanel17.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 100, -1, -1));

        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel56.setText("Precio");
        jPanel17.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 100, -1, -1));

        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(0, 0, 255));
        jLabel57.setText("Stock Disponible");
        jPanel17.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        txtCodigoVentaCreditClient.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCodigoVentaCreditClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoVentaCreditClientActionPerformed(evt);
            }
        });
        txtCodigoVentaCreditClient.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoVentaCreditClientKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoVentaCreditClientKeyTyped(evt);
            }
        });
        jPanel17.add(txtCodigoVentaCreditClient, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 210, 40));

        txtCantidadVenta1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCantidadVenta1.setText("1");
        txtCantidadVenta1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadVenta1ActionPerformed(evt);
            }
        });
        txtCantidadVenta1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadVenta1KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadVenta1KeyTyped(evt);
            }
        });
        jPanel17.add(txtCantidadVenta1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 130, 40, 30));

        txtPrecioVenta1.setEditable(false);
        txtPrecioVenta1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtPrecioVenta1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecioVenta1ActionPerformed(evt);
            }
        });
        jPanel17.add(txtPrecioVenta1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 120, 80, 30));

        txtStockDisponible2.setEditable(false);
        jPanel17.add(txtStockDisponible2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 79, 30));

        btnEliminarventa1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarventa1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarventa1ActionPerformed(evt);
            }
        });
        jPanel17.add(btnEliminarventa1, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 110, -1, 40));

        jLabel58.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel58.setText("ID");
        jPanel17.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, -1, -1));

        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel59.setText("Cliente:");
        jPanel17.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, -1, -1));

        txtRucVentaCredit.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtRucVentaCredit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRucVentaCreditActionPerformed(evt);
            }
        });
        txtRucVentaCredit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRucVentaCreditKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRucVentaCreditKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRucVentaCreditKeyTyped(evt);
            }
        });
        jPanel17.add(txtRucVentaCredit, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 25, 116, -1));

        txtNombreClienteventaCredit.setEditable(false);
        txtNombreClienteventaCredit.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtNombreClienteventaCredit.setForeground(new java.awt.Color(51, 51, 255));
        jPanel17.add(txtNombreClienteventaCredit, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 190, 40));

        jLabel60.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel60.setText("Total :");
        jPanel17.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 570, -1, -1));

        lblTotalCredit.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblTotalCredit.setText("-----");
        jPanel17.add(lblTotalCredit, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 570, -1, -1));

        txtIdCV1.setText("2");
        jPanel17.add(txtIdCV1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 580, 20, -1));
        jPanel17.add(txtIdPro2, new org.netbeans.lib.awtextra.AbsoluteConstraints(678, 126, -1, -1));

        lblcambioCredit.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jPanel17.add(lblcambioCredit, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 530, 60, 20));

        btnBusccarPro1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa.png"))); // NOI18N
        btnBusccarPro1.setText("buscar");
        btnBusccarPro1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBusccarPro1ActionPerformed(evt);
            }
        });
        jPanel17.add(btnBusccarPro1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, 30));

        TableCreditClient.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        TableCreditClient.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "DESCRIPCIÓN", "CANTIDAD", "PRECIO U.", "PRECIO TOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableCreditClient.setRowHeight(25);
        TableCreditClient.getTableHeader().setReorderingAllowed(false);
        TableCreditClient.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableCreditClientMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                TableCreditClientMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                TableCreditClientMousePressed(evt);
            }
        });
        TableCreditClient.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TableCreditClientKeyPressed(evt);
            }
        });
        jScrollPane8.setViewportView(TableCreditClient);
        if (TableCreditClient.getColumnModel().getColumnCount() > 0) {
            TableCreditClient.getColumnModel().getColumn(0).setMinWidth(80);
            TableCreditClient.getColumnModel().getColumn(0).setMaxWidth(100);
            TableCreditClient.getColumnModel().getColumn(2).setMinWidth(50);
            TableCreditClient.getColumnModel().getColumn(2).setMaxWidth(120);
            TableCreditClient.getColumnModel().getColumn(3).setMinWidth(100);
            TableCreditClient.getColumnModel().getColumn(3).setMaxWidth(150);
            TableCreditClient.getColumnModel().getColumn(4).setMinWidth(100);
            TableCreditClient.getColumnModel().getColumn(4).setMaxWidth(150);
        }

        jPanel17.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 1030, 380));

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 102, 102));
        jLabel46.setText("Credito A Clientes");
        jPanel17.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 0, -1, 30));

        jButton2.setText("Ver Historial");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel17.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 40, 120, 30));

        btnGenerarVentaCredit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/print.png"))); // NOI18N
        btnGenerarVentaCredit.setText("ACEPTAR");
        btnGenerarVentaCredit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarVentaCreditActionPerformed(evt);
            }
        });
        jPanel17.add(btnGenerarVentaCredit, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 570, -1, 45));

        Menu.addTab("1", jPanel17);

        jPanel18.setBackground(new java.awt.Color(255, 255, 255));
        jPanel18.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel18KeyPressed(evt);
            }
        });
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.GroupLayout jPanelMenuCorteLayout = new javax.swing.GroupLayout(jPanelMenuCorte);
        jPanelMenuCorte.setLayout(jPanelMenuCorteLayout);
        jPanelMenuCorteLayout.setHorizontalGroup(
            jPanelMenuCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 770, Short.MAX_VALUE)
        );
        jPanelMenuCorteLayout.setVerticalGroup(
            jPanelMenuCorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        jPanel18.add(jPanelMenuCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 160, 770, 340));

        Menu.addTab("1", jPanel18);

        getContentPane().add(Menu, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 95, 1120, 650));

        menu.setText("menu");
        menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuActionPerformed(evt);
            }
        });
        getContentPane().add(menu, new org.netbeans.lib.awtextra.AbsoluteConstraints(1230, 20, 80, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClientesActionPerformed
   // Validación de acceso
    if (usuarioLogueado.getRol().equals("Administrador") 
        || corteValidado 
        || rolController.solicitarClaveAdministrador()) {
        
        corteValidado = true; // marcar como validado
        Menu.setSelectedIndex(1); // cambiar a la pestaña Clientes
        
        LimpiarTable(); // limpiar tabla
        ListarCliente(usuarioLogueado.getIdEmpresa()); // 🔹 ahora sí se lista por empresa
        
        btnEditarCliente.setEnabled(false);
        btnEliminarCliente.setEnabled(false);
        LimpiarCliente(); // limpiar campos del formulario
    }
    }//GEN-LAST:event_btnClientesActionPerformed

    private void btnProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedorActionPerformed
       // Verificar permisos
    if (usuarioLogueado.getRol().equals("Administrador") || corteValidado
        || rolController.solicitarClaveAdministrador()) {

        corteValidado = true; // marcar como validado
        Menu.setSelectedIndex(2); // seleccionar pestaña Proveedor

        LimpiarTable(); // limpiar la tabla antes de listar

        // Listar proveedores solo de la empresa del usuario logueado
        ListarProveedor(usuarioLogueado.getIdEmpresa());

        btnEditarProveedor.setEnabled(true);
        btnEliminarProveedor.setEnabled(true);

        LimpiarProveedor(); // limpiar campos de formulario
    }  
    }//GEN-LAST:event_btnProveedorActionPerformed

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductosActionPerformed
        // TODO add your handling code here:
        
         if (usuarioLogueado.getRol().equals("Administrador") || corteValidado
        || rolController.solicitarClaveAdministrador()) {
        corteValidado = true; // marcar como validado
        Menu.setSelectedIndex(3);// abrir pestaña de proveedor
        LimpiarTable();
       // ListarProductos();
        btnEditarpro.setEnabled(false);
        btnEliminarPro.setEnabled(false);
        btnGuardarpro.setEnabled(true);
        LimpiarProductos();
    }
    }//GEN-LAST:event_btnProductosActionPerformed

    private void btnNuevaVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaVentaActionPerformed
        // TODO add your handling code here:
     

        Menu.setSelectedIndex(0);
         txtCodigoVenta.requestFocus();
    }//GEN-LAST:event_btnNuevaVentaActionPerformed

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        if (usuarioLogueado.getRol().equals("Administrador") || corteValidado
            || rolController.solicitarClaveAdministrador()) {
        corteValidado = true;
        infoEmpresa.cargar();
        Menu.setSelectedIndex(5);
    }  
    }//GEN-LAST:event_btnConfigActionPerformed

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
          if (usuarioLogueado.getRol().equals("Administrador") || corteValidado
        || rolController.solicitarClaveAdministrador()) {
        corteValidado = true; // marcar como validado
        Menu.setSelectedIndex(4);

        if (historialController == null) {
            historialController = new HistorialVentasController(panelHistorialVentas, idEmpresaActiva);
        }
        historialController.cargarVentas();
    } 
        
    }//GEN-LAST:event_btnVentasActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
         if (usuarioLogueado.getRol().equals("Administrador") || corteValidado
        || rolController.solicitarClaveAdministrador()) {
        corteValidado = true; // marcar como validado
       
        Menu.setSelectedIndex(6);// abrir pestaña de usuarios
        LimpiarTable();
        ListarUsuarios();
    }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnProductosMouseClicked
        // TODO add your handling code here:
        cbxProveedorPro.removeAllItems();
        llenarProveedor();
        
    }//GEN-LAST:event_btnProductosMouseClicked

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
          if(txtNombre.getText().equals("") || txtCorreo.getText().equals("") || txtPass.getPassword().equals("")){
        JOptionPane.showMessageDialog(null, "Todos los campos son requeridos");
    } else {
        String correo = txtCorreo.getText();
        String pass = String.valueOf(txtPass.getPassword());
        String nom = txtNombre.getText();
        String rol = cbxRol.getSelectedItem().toString();
        
        lg.setNombre(nom);
        lg.setCorreo(correo);
        lg.setPass(pass);
        lg.setRol(rol);

        // 🔥 Aquí agregamos la empresa activa
       
lg.setIdEmpresa(Sistema.getIdEmpresaActiva());

        login.Registrar(lg);
        JOptionPane.showMessageDialog(null, "Usuario Registrado");
        LimpiarTable();
        ListarUsuarios();
        nuevoUsuario();
    }
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void txtCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCorreoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCorreoActionPerformed

    private void btnActualizarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarConfigActionPerformed
       infoEmpresa.actualizar();

    }//GEN-LAST:event_btnActualizarConfigActionPerformed


    private void btnPdfVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfVentasActionPerformed
  //bloque eliminado 
    }//GEN-LAST:event_btnPdfVentasActionPerformed

    private void TableVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableVentasMouseClicked

    }//GEN-LAST:event_TableVentasMouseClicked

    private void btnNuevoProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProActionPerformed
        // TODO add your handling code here:
        
        LimpiarProductos();
              
    
        btnGuardarpro.setEnabled(true);
    }//GEN-LAST:event_btnNuevoProActionPerformed

    private void btnEliminarProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProActionPerformed
     
    }//GEN-LAST:event_btnEliminarProActionPerformed

    private void btnEditarproActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarproActionPerformed
    
   
    }//GEN-LAST:event_btnEditarproActionPerformed

    private void btnGuardarproActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarproActionPerformed
   
    }//GEN-LAST:event_btnGuardarproActionPerformed

    private void cbxProveedorProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxProveedorProActionPerformed
       Object selected = cbxProveedorPro.getSelectedItem();
    if (selected instanceof Combo) {
        Combo combo = (Combo) selected;

        // Si el usuario seleccionó "Agregar proveedor"
        if (combo.getId() == -1) {
            // 🔹 Abre el formulario modal
            new Utilidades.FormularioAgregarProveedor(this, usuarioLogueado.getIdEmpresa()).setVisible(true);

            // 🔹 Después de cerrarlo, recargar la lista de proveedores
            llenarProveedor();
        }
    }
    }//GEN-LAST:event_cbxProveedorProActionPerformed

    private void cbxProveedorProItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxProveedorProItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxProveedorProItemStateChanged

    private void txtPrecioProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPrecioProKeyTyped
        // TODO add your handling code here:
        event.numberDecimalKeyPress(evt, txtPrecioPro);
    }//GEN-LAST:event_txtPrecioProKeyTyped

    private void btnEliminarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProveedorActionPerformed
    if (!"".equals(txtIdProveedor.getText())) {
        int pregunta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar?");
        if (pregunta == 0) {
            int id = Integer.parseInt(txtIdProveedor.getText());
            PrDao.EliminarProveedor(id);

            LimpiarTable();
            // Listar solo los proveedores de la empresa del usuario logueado
            ListarProveedor(usuarioLogueado.getIdEmpresa());

            LimpiarProveedor();
        }
    } else {
        JOptionPane.showMessageDialog(null, "Seleccione una fila");
    }
    }//GEN-LAST:event_btnEliminarProveedorActionPerformed

    private void btnNuevoProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProveedorActionPerformed
        // TODO add your handling code here:
        LimpiarProveedor();
        btnEditarProveedor.setEnabled(false);
        btnEliminarProveedor.setEnabled(false);
        btnguardarProveedor.setEnabled(true);
    }//GEN-LAST:event_btnNuevoProveedorActionPerformed

    private void btnEditarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarProveedorActionPerformed
    
    // 1️⃣ Validar selección
    if (txtIdProveedor.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione un proveedor de la tabla");
        return;
    }

    // 2️⃣ Validar campos obligatorios
    if (txtRucProveedor.getText().trim().isEmpty()
        || txtNombreproveedor.getText().trim().isEmpty()
        || txtTelefonoProveedor.getText().trim().isEmpty()
        || txtDireccionProveedor.getText().trim().isEmpty()) {

        JOptionPane.showMessageDialog(
            null,
            "Todos los campos son obligatorios",
            "Validación",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // 3️⃣ Asignar valores
    pr.setRuc(txtRucProveedor.getText().trim());
    pr.setNombre(txtNombreproveedor.getText().trim());
    pr.setTelefono(txtTelefonoProveedor.getText().trim());
    pr.setDireccion(txtDireccionProveedor.getText().trim());
    pr.setId(Integer.parseInt(txtIdProveedor.getText().trim()));

    // 4️⃣ Empresa del usuario logueado
    pr.setIdEmpresa(usuarioLogueado.getIdEmpresa());

    // 5️⃣ Actualizar
    if (PrDao.ModificarProveedor(pr)) {
        JOptionPane.showMessageDialog(null, "Proveedor modificado correctamente");

        LimpiarTable();
        ListarProveedor(usuarioLogueado.getIdEmpresa());
        LimpiarProveedor();

        btnEditarProveedor.setEnabled(false);
        btnEliminarProveedor.setEnabled(false);
        btnguardarProveedor.setEnabled(true);
    } else {
        JOptionPane.showMessageDialog(null, "Error al modificar proveedor");
    }
    }//GEN-LAST:event_btnEditarProveedorActionPerformed

    private void btnguardarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnguardarProveedorActionPerformed
         if (!"".equals(txtRucProveedor.getText()) || !"".equals(txtNombreproveedor.getText()) 
        || !"".equals(txtTelefonoProveedor.getText()) || !"".equals(txtDireccionProveedor.getText())) {
        
        pr.setRuc(txtRucProveedor.getText());
        pr.setNombre(txtNombreproveedor.getText());
        pr.setTelefono(txtTelefonoProveedor.getText());
        pr.setDireccion(txtDireccionProveedor.getText());
        
        // Asignar la empresa del usuario logueado
        pr.setIdEmpresa(usuarioLogueado.getIdEmpresa());

        PrDao.RegistrarProveedor(pr);
        JOptionPane.showMessageDialog(null, "Proveedor Registrado");

        LimpiarTable();
        // Listar solo los proveedores de la empresa del usuario logueado
        ListarProveedor(usuarioLogueado.getIdEmpresa());

        LimpiarProveedor();
        btnEditarProveedor.setEnabled(false);
        btnEliminarProveedor.setEnabled(false);
        btnguardarProveedor.setEnabled(true);
    } else {
        JOptionPane.showMessageDialog(null, "Los campos están vacíos");
    }
    }//GEN-LAST:event_btnguardarProveedorActionPerformed

    private void TableProveedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableProveedorMouseClicked
        // TODO add your handling code here:
        btnEditarProveedor.setEnabled(true);
        btnEliminarProveedor.setEnabled(true);
        btnguardarProveedor.setEnabled(false);
        int fila = TableProveedor.rowAtPoint(evt.getPoint());
        txtIdProveedor.setText(TableProveedor.getValueAt(fila, 0).toString());
        txtRucProveedor.setText(TableProveedor.getValueAt(fila, 1).toString());
        txtNombreproveedor.setText(TableProveedor.getValueAt(fila, 2).toString());
        txtTelefonoProveedor.setText(TableProveedor.getValueAt(fila, 3).toString());
        txtDireccionProveedor.setText(TableProveedor.getValueAt(fila, 4).toString());
    }//GEN-LAST:event_TableProveedorMouseClicked

    private void btnNuevoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoClienteActionPerformed
        // TODO add your handling code here:
        LimpiarCliente();
        btnEditarCliente.setEnabled(false);
        btnEliminarCliente.setEnabled(false);
        btnGuardarCliente.setEnabled(true);
    }//GEN-LAST:event_btnNuevoClienteActionPerformed

    private void btnEliminarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarClienteActionPerformed
    if (!"".equals(txtIdCliente.getText())) {
        int pregunta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar?");
        if (pregunta == 0) {
            int id = Integer.parseInt(txtIdCliente.getText());
            client.EliminarCliente(id); // elimina el cliente
            LimpiarTable();             // limpia la tabla
            LimpiarCliente();           // limpia los campos
            ListarCliente(usuarioLogueado.getIdEmpresa()); // 🔹 vuelve a listar por empresa
        }
    } else {
        JOptionPane.showMessageDialog(null, "Seleccione un cliente para eliminar");
    }
    }//GEN-LAST:event_btnEliminarClienteActionPerformed

    private void btnEditarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarClienteActionPerformed
      if ("".equals(txtIdCliente.getText())) {
        JOptionPane.showMessageDialog(null, "Seleccione una fila");
    } else {
        if (!"".equals(txtDniCliente.getText()) 
            && !"".equals(txtNombreCliente.getText()) 
            && !"".equals(txtTelefonoCliente.getText())) {
            
            // Cargar datos del formulario al objeto Cliente
            cl.setDni(txtDniCliente.getText());
            cl.setNombre(txtNombreCliente.getText());
            cl.setTelefono(txtTelefonoCliente.getText());
            cl.setDireccion(txtDirecionCliente.getText());
            cl.setId(Integer.parseInt(txtIdCliente.getText()));
            cl.setIdEmpresa(usuarioLogueado.getIdEmpresa()); // 🔹 asegurar empresa

            // Guardar en la base de datos
            if (client.ModificarCliente(cl)) {
                JOptionPane.showMessageDialog(null, "Cliente modificado correctamente");
                LimpiarTable();
                LimpiarCliente();
                ListarCliente(usuarioLogueado.getIdEmpresa()); // 🔹 recargar lista por empresa
            } else {
                JOptionPane.showMessageDialog(null, "Error al modificar cliente");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Los campos no pueden estar vacíos");
        }
    }
    }//GEN-LAST:event_btnEditarClienteActionPerformed

    private void btnGuardarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarClienteActionPerformed
       if (!"".equals(txtDniCliente.getText()) 
        && !"".equals(txtNombreCliente.getText()) 
        && !"".equals(txtTelefonoCliente.getText()) 
        && !"".equals(txtDirecionCliente.getText())) {
        
        // Cargar datos al objeto cliente
        cl.setDni(txtDniCliente.getText());
        cl.setNombre(txtNombreCliente.getText());
        cl.setTelefono(txtTelefonoCliente.getText());
        cl.setDireccion(txtDirecionCliente.getText());
        cl.setIdEmpresa(usuarioLogueado.getIdEmpresa()); // 🔹 asegurar empresa

        // Registrar cliente
        if (client.RegistrarCliente(cl)) {
            JOptionPane.showMessageDialog(null, "Cliente registrado correctamente");
            LimpiarTable();
            LimpiarCliente();
            ListarCliente(usuarioLogueado.getIdEmpresa()); // 🔹 recargar por empresa
            btnEditarCliente.setEnabled(false);
            btnEliminarCliente.setEnabled(false);
            btnGuardarCliente.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(null, "Error al registrar cliente");
        }
    } else {
        JOptionPane.showMessageDialog(null, "Los campos no pueden estar vacíos");
    }
    }//GEN-LAST:event_btnGuardarClienteActionPerformed

    private void txtDniClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDniClienteKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDniClienteKeyTyped

    private void TableClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableClienteMouseClicked
        // TODO add your handling code here:
        btnEditarCliente.setEnabled(true);
        btnEliminarCliente.setEnabled(true);
        btnGuardarCliente.setEnabled(false);
        int fila = TableCliente.rowAtPoint(evt.getPoint());
        txtIdCliente.setText(TableCliente.getValueAt(fila, 0).toString());
        txtDniCliente.setText(TableCliente.getValueAt(fila, 1).toString());
        txtNombreCliente.setText(TableCliente.getValueAt(fila, 2).toString());
        txtTelefonoCliente.setText(TableCliente.getValueAt(fila, 3).toString());
        txtDirecionCliente.setText(TableCliente.getValueAt(fila, 4).toString());
    }//GEN-LAST:event_TableClienteMouseClicked

    private void btnGraficarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficarActionPerformed
   
    }//GEN-LAST:event_btnGraficarActionPerformed

    private void txtRucVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRucVentaKeyTyped

    private void txtRucVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaKeyPressed
        //original
   EnterClienteVenta();
       
    }//GEN-LAST:event_txtRucVentaKeyPressed

 public void EnterClienteVenta() {
    if (!"".equals(txtRucVenta.getText())) {
        int dni = Integer.parseInt(txtRucVenta.getText());
        cl = client.BuscarCliente(dni, usuarioLogueado.getIdEmpresa());
        if (cl.getNombre() != null) {
            txtNombreClienteventa.setText(cl.getNombre());
            txtIdCV.setText(String.valueOf(cl.getId()));
            txtPaga1.requestFocus();
        } else {
            txtRucVenta.setText("");
           // JOptionPane.showMessageDialog(null, "El cliente no existe en esta empresa");
        }
    }
}                                     

    private void btnEliminarventaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarventaActionPerformed
       int fila = TableVenta.getSelectedRow();
if (fila < 0) {
    JOptionPane.showMessageDialog(null, "Seleccione un producto");
    return;
}
if (esFilaPromo(TableVenta.getValueAt(fila, 0))) {
    JOptionPane.showMessageDialog(null, "La promoción se quita sola al quitar el producto.");
    return;
}
modelo = (DefaultTableModel) TableVenta.getModel();
modelo.removeRow(fila);
TotalPagarX();
txtCodigoVenta.requestFocus();
    }//GEN-LAST:event_btnEliminarventaActionPerformed

    private void txtCantidadVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyTyped
        // TODO add your handling code here:
        event.numberKeyPress(evt);
    }//GEN-LAST:event_txtCantidadVentaKeyTyped

    private void txtCantidadVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyPressed
      if (!"".equals(txtCantidadVenta.getText().trim())) {
    int id = Integer.parseInt(txtIdPro.getText());
    String descripcion = txtDescripcionVenta.getText();
    int cant = Integer.parseInt(txtCantidadVenta.getText().trim());
    double precio = Double.parseDouble(txtPrecioVenta.getText());
    double total = cant * precio;
    int stock = Integer.parseInt(txtStockDisponible.getText());

    if (stock >= cant) {
        item = item + 1;
        tmp = (DefaultTableModel) TableVenta.getModel();

        for (int i = 0; i < TableVenta.getRowCount(); i++) {
            if (TableVenta.getValueAt(i, 0).equals(txtCodigoVenta.getText())) {
                JOptionPane.showMessageDialog(null, "El producto ya está registrado");
                return;
            }
        }

        Object[] O = new Object[5];
        O[0] = id;
        O[1] = descripcion;
        O[2] = cant;
        O[3] = precio;
        O[4] = total;
        tmp.addRow(O);
        TableVenta.setModel(tmp);

        TotalPagarX();
        LimparVenta();
        txtCodigoVenta.requestFocus();
    } else {
        JOptionPane.showMessageDialog(null, "Stock no disponible");
    }
} else {
    JOptionPane.showMessageDialog(null, "Ingrese Cantidad");
}
    }//GEN-LAST:event_txtCantidadVentaKeyPressed

    private void txtDescripcionVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescripcionVentaKeyTyped
        // TODO add your handling code here:
        event.textKeyPress(evt);
    }//GEN-LAST:event_txtDescripcionVentaKeyTyped

    private void txtCodigoVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaKeyTyped
   
      
    }//GEN-LAST:event_txtCodigoVentaKeyTyped
// Constructor o método de inicialización de la ventana
private void inicializarTeclas() {
     InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = getRootPane().getActionMap();

    // F12 para abrir ventana cobro
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "openCobrarWindow");
    actionMap.put("openCobrarWindow", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            abrirVentanaCobrar();
        }
    });

    // F1 para abrir ventana búsqueda SOLO en pestaña VENTA
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "openBuscarWindow");
    actionMap.put("openBuscarWindow", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!"venta".equals(origenActual)) {
                System.out.println("⛔ Acción cancelada: F1 solo disponible en pestaña VENTA. origenActual = " + origenActual);
                return;
            }
            System.out.println("[TECLA] F1 presionado: abrir ventana de búsqueda desde " + origenActual);
            abrirVentanaBuscar();
        }
    });
}

// Llamar este método en el constructor de la ventana o en un método de inicialización
public void abrirVentanaCobrar() {
    if (TableVenta.getRowCount() > 0) {
        ventanaCobrar cobrar = new ventanaCobrar(this);

        // 🔹 pasar empresa activa detectada dinámicamente
        int idEmpresaActual = Sistema.idEmpresaActiva; // o la variable donde tengas la empresa activa
        cobrar.setIdEmpresaActiva(idEmpresaActual);

        cobrar.setTotal(TotalPagar);
        cobrar.setVisible(true);
    } else {
        JOptionPane.showMessageDialog(null, "No hay productos en la venta");
        txtCodigoVenta.requestFocus();
    }
}

   //// hasta aqui

    private void txtCodigoVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaKeyPressed
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        String texto = txtCodigoVenta.getText().trim();

        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el código o ID del producto");
            return;
        }

        // 🥬 Interceptar: si coincide con un producto de Verdulería (nombre, código o ID),
        // abrir el panel de detalle con báscula/peso en vez de agregar directo a la tabla.
        Productos productoVerduleria = buscarProductoVerduleria(texto);
        if (productoVerduleria != null) {
            if (panelVerduleria != null) {
                panelVerduleria.mostrarDetalle(productoVerduleria);
            }
            txtCodigoVenta.setText("");
            txtCodigoVenta.requestFocus();
            return; // 👈 no seguir con la búsqueda normal
        }

        // 🔍 Búsqueda normal (productos que NO son de Verdulería)
        pro = proDao.BuscarPro(texto, idEmpresaActiva);

        if (pro.getNombre() == null) {
            try {
                int id = Integer.parseInt(texto);
                pro = proDao.BuscarId(id, idEmpresaActiva);
            } catch (NumberFormatException e) {
                // No es numérico, se ignora
            }
        }

        if (pro.getNombre() != null) {
            txtIdPro.setText(String.valueOf(pro.getId()));
            txtCodigoVenta.setText(pro.getCodigo());
            txtDescripcionVenta.setText(pro.getNombre());
            txtPrecioVenta.setText(String.valueOf(pro.getPrecio()));
            txtStockDisponible.setText(String.valueOf(pro.getStock()));

            int cant = Integer.parseInt(txtCantidadVenta.getText());
            int id = Integer.parseInt(txtIdPro.getText());
            String descripcion = txtDescripcionVenta.getText();
            double precio = Double.parseDouble(txtPrecioVenta.getText());
            double total = cant * precio;
            int stock = Integer.parseInt(txtStockDisponible.getText());

            if (stock >= cant) {
                DefaultTableModel tmp = (DefaultTableModel) TableVenta.getModel();
                Object[] fila = new Object[5];
                fila[0] = id;
                fila[1] = descripcion;
                fila[2] = cant;
                fila[3] = precio;
                fila[4] = total;

                tmp.addRow(fila);
                TableVenta.setModel(tmp);

                TotalPagarX();
                LimparVenta();
            } else {
                JOptionPane.showMessageDialog(null, "Stock no disponible");
                LimparVenta();
            }
        } else {
            JOptionPane.showMessageDialog(null, "El código o ID del producto no existe");
            LimparVenta();
        }

        txtCodigoVenta.requestFocus();
        txtCodigoVenta.selectAll();
    }    
    }//GEN-LAST:event_txtCodigoVentaKeyPressed

    private void btnEntradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntradaActionPerformed
   Menu.setSelectedIndex(7);        // TODO add your handling code here:
    }//GEN-LAST:event_btnEntradaActionPerformed

    private void txtIdConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdConfigActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdConfigActionPerformed

    private void txtCodigoEntradaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoEntradaKeyPressed
// Código que busca el producto y llena los campos
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        if (!"".equals(txtCodigoEntrada.getText().trim())) {
            String cod = txtCodigoEntrada.getText().trim();

            // BUSCAR producto filtrando por la empresa del usuario logueado
            pro = proDao.BuscarPro(cod, idEmpresaActiva);

            if (pro.getNombre() != null) {
                txtIdPro.setText("" + pro.getId());
                txtDescripcionEntrada.setText(pro.getNombre());
                txtPrecioEntrada.setText("" + pro.getPrecio());
                txtStockDisponible1.setText("" + pro.getStock());
                txtCantidadEntrada.requestFocus();
            } else {
                LimpiarEntrada();
                txtCodigoEntrada.requestFocus();
                JOptionPane.showMessageDialog(null, "EL CODIGO DE PRODUCTO NO EXISTE");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Ingrese el codigo del producto");
            txtCodigoEntrada.requestFocus();
            LimpiarEntrada();
        }
    }

        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoEntradaKeyPressed

    private void txtCodigoEntradaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoEntradaKeyTyped

event.numberKeyPress(evt);
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoEntradaKeyTyped

    private void txtDescripcionEntradaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescripcionEntradaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescripcionEntradaKeyTyped

    private void txtCantidadEntradaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadEntradaKeyPressed
        // TODO add your handling code here:
        
      
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             
             //si el campo tiene un nuemero
            //if (!"".equals(txtCantidadEntrada.getText())) {
                if (!"".equals(txtCantidadEntrada.getText())) {
                int id = Integer.parseInt(txtIdPro.getText());
                String descripcion = txtDescripcionEntrada.getText();
                int cant = Integer.parseInt(txtCantidadEntrada.getText());
                double precio = Double.parseDouble(txtPrecioEntrada.getText());
                double total = cant * precio;
                int stock = Integer.parseInt(txtStockDisponible1.getText());
                // aqui le digo que no importa si el producto ingresado es mayor o menor al stock
             stock = 0;
                if (stock != cant) {
                  
                    // !=  >=   38 != 50 esto estaba antes no dejaba meter una entrada mayor al stock
                    item = item + 1;
                    tmp = (DefaultTableModel) TableEntrada.getModel();
                    for (int i = 0; i < TableEntrada.getRowCount(); i++) {
                        if (TableEntrada.getValueAt(i, 1).equals(txtDescripcionEntrada.getText())) {
                            JOptionPane.showMessageDialog(null, "El producto ya esta registrado");
                            LimpiarEntrada();
                                 txtCodigoEntrada.requestFocus();
                            return;
                            
                        }
                    }
                    ArrayList lista = new ArrayList();
                    lista.add(item);
                    lista.add(id);
                    lista.add(descripcion);
                    lista.add(cant);
                    lista.add(precio);
                    lista.add(total);
                    Object[] O = new Object[5];
                    O[0] = lista.get(1);
                    O[1] = lista.get(2);
                    O[2] = lista.get(3);
                    O[3] = lista.get(4);
                    O[4] = lista.get(5);
                    tmp.addRow(O);
                    TableEntrada.setModel(tmp);
                   LimpiarEntrada(); 
                    TotalPagarEntrada();
                    LimparVenta();
                    
                    txtCodigoEntrada.requestFocus();
                   
               
                    
                } else {
                    JOptionPane.showMessageDialog(null, "Stock no disponible");
                    
                }
            } else {
                JOptionPane.showMessageDialog(null, "Ingrese cantidad");
            }
        }
    }//GEN-LAST:event_txtCantidadEntradaKeyPressed

    private void txtCantidadEntradaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadEntradaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadEntradaKeyTyped

    private void btnEliminarentradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarentradaActionPerformed
         int filaSeleccionada = TableEntrada.getSelectedRow();
    if (filaSeleccionada != -1) { // valida que haya una fila seleccionada
        modelo = (DefaultTableModel) TableEntrada.getModel();
        modelo.removeRow(filaSeleccionada);
        TotalPagarEntrada(); // recalcula siempre después de borrar
    } else {
        JOptionPane.showMessageDialog(null, "Seleccione una fila para eliminar");
    }
    txtCodigoEntrada.requestFocus();
    }//GEN-LAST:event_btnEliminarentradaActionPerformed

    private void btnGenerarEntradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarEntradaActionPerformed
        // TODO add your handling code here:
        
         if (TableEntrada.getRowCount() > 0) {
         

                //RegistrarEntrada();
                //RegistrarDetalle();
               // RegistrarDetalleEntrada();
                ActualizarStockEntrada();
              
              LimpiarTableEntrada();
                //LimpiarClienteventa();
                JOptionPane.showMessageDialog(null, "Stock Actualizado Exitosamente");
                LimpiarEntrada();
            
        } else {
            JOptionPane.showMessageDialog(null, "No hay productos en la venta");
              txtCodigoEntrada.requestFocus();
        }
    }//GEN-LAST:event_btnGenerarEntradaActionPerformed

    private void txtCantidadVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVentaActionPerformed

    private void txtCodigoProKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProKeyPressed
   if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
    if (!"".equals(txtCodigoPro.getText().trim())) {
        String cod = txtCodigoPro.getText().trim();

        // Buscar producto filtrando por la empresa del usuario logueado
        pro = proDao.BuscarPro(cod, idEmpresaActiva);

        if (pro.getNombre() == null) {
            // Producto no existe, podemos usar los campos para registrar
            txtIdPro.setText("" + pro.getId()); // normalmente será 0
            txtDescripcionVenta.setText("");    // limpiar descripción
            txtPrecioVenta.setText("");         // limpiar precio
            txtStockDisponible.setText("");     // limpiar stock
            txtCantidadVenta.requestFocus();
        } else {
            // Producto ya existe
            LimparVenta();
            txtCodigoPro.requestFocus();
            JOptionPane.showMessageDialog(null, "EL CODIGO DE PRODUCTO YA ESTA REGISTRADO");
        }
    } else {
        JOptionPane.showMessageDialog(null, "Ingrese el codigo del producto");
        txtCodigoPro.requestFocus();
    }
}


    }//GEN-LAST:event_txtCodigoProKeyPressed

    private void txtBuscarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyReleased
        filtrar();
    }//GEN-LAST:event_txtBuscarKeyReleased

    private void txtDesProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDesProActionPerformed

       
        
        mayus=txtDesPro.getText();
txtDesPro.setText(""+mayus.toUpperCase());        // TODO add your handling code here:
    }//GEN-LAST:event_txtDesProActionPerformed

    private void txtBuscarKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyTyped
char caracter=evt.getKeyChar();
 


//String x="       remueve los espacios    ";

if(Character.isLowerCase(caracter)){
evt.setKeyChar(Character.toUpperCase(caracter));

}
// TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarKeyTyped

    private void txtDesProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDesProKeyTyped
  
   // TODO add your handling code here:
    }//GEN-LAST:event_txtDesProKeyTyped

    private void txtDesProKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDesProKeyReleased
         mayus=txtDesPro.getText();
txtDesPro.setText(""+mayus.toUpperCase());  // TODO add your handling code here:
    }//GEN-LAST:event_txtDesProKeyReleased

    private void jPanel5KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel5KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel5KeyReleased

    private void txtRucVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRucVentaActionPerformed
        // TODO add your handling code here:
      
    }//GEN-LAST:event_txtRucVentaActionPerformed

    private void txtRucVentaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaKeyReleased
   // TODO add your handling code here:
    }//GEN-LAST:event_txtRucVentaKeyReleased

    private void txtPrecioVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioVentaActionPerformed

    private void txtCantidadEntradaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadEntradaKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadEntradaKeyReleased

    private void txtBuscarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarKeyPressed

    private void txtCodigoVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoVentaActionPerformed

    private void btnTodasFilasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTodasFilasActionPerformed
List<Object[]> filasCredito = new ArrayList<>();
for (int i = 0; i < TableVenta.getRowCount(); i++) {
    if (esFilaPromo(TableVenta.getValueAt(i, 0))) continue;   // sin promo en crédito
    Object[] fila = new Object[5];
    for (int j = 0; j < 5; j++) fila[j] = TableVenta.getValueAt(i, j);
    filasCredito.add(fila);
}
creditoClienteController.agregarFilasDesdeVenta(filasCredito.toArray(new Object[0][]));
LimpiarTableVenta();
Menu.setSelectedIndex(8);
    }//GEN-LAST:event_btnTodasFilasActionPerformed
 
    private void btnCobrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCobrarActionPerformed
 abrirVentanaCobrar();       
      //AbrirCajaEfectivo.main(null); // Llama la función para abrir la caja  
    }//GEN-LAST:event_btnCobrarActionPerformed

    private void btnCobrarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCobrarMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCobrarMouseClicked

    private void txtPaga1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPaga1KeyReleased
        String  valor = txtPaga1.getText();
        lblcambio.setText(valor);
       Operacion();   // TODO add your handling code here:
    }//GEN-LAST:event_txtPaga1KeyReleased

    private void txtPaga1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPaga1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPaga1ActionPerformed

    private void txtCodigoEntradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoEntradaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoEntradaActionPerformed

    private void txtCantidadEntradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadEntradaActionPerformed
//LimpiarEntrada();        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadEntradaActionPerformed

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarActionPerformed
 
    private void TableProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableProductoMouseClicked
  // Obtener la fila en la que se hizo clic según la posición del mouse
    int fila = TableProducto.rowAtPoint(evt.getPoint());

    // Validar que la fila sea válida (no clic fuera de filas)
    if (fila < 0) {
        return; // Si no es válida, salir del método para evitar errores
    }

    // Habilitar botones para editar y eliminar, deshabilitar botón guardar
    btnEditarpro.setEnabled(true);
    btnEliminarPro.setEnabled(true);
    btnGuardarpro.setEnabled(false);

    // Llenar los campos de texto con los valores de la fila seleccionada
    txtIdproducto.setText(TableProducto.getValueAt(fila, 0).toString());
    txtCodigoPro.setText(TableProducto.getValueAt(fila, 1).toString());
    txtDesPro.setText(TableProducto.getValueAt(fila, 2).toString());
    txtCantPro.setText(TableProducto.getValueAt(fila, 4).toString());
    txtPrecioPro.setText(TableProducto.getValueAt(fila, 5).toString());
     txtPreciocompraPro.setText(TableProducto.getValueAt(fila, 6).toString());
     

    // Declarar variable para guardar el id del proveedor
    int idProveedor = -1;

    try {
        // Intentar convertir el valor de la columna 6 a entero (id proveedor)
        idProveedor = Integer.parseInt(TableProducto.getValueAt(fila, 7).toString());
    } catch (NumberFormatException e) {
        // Si la conversión falla, mostrar error en consola y salir
        System.out.println("Error: El proveedor no es un número válido.");
        return;
    }

    // Buscar en el combo box el item cuyo id coincida con el idProveedor
    for (int i = 0; i < cbxProveedorPro.getItemCount(); i++) {
        Combo item = (Combo) cbxProveedorPro.getItemAt(i);
        if (item.getId() == idProveedor) {
            // Seleccionar ese índice en el combo box
            cbxProveedorPro.setSelectedIndex(i);
            break; // Salir del ciclo una vez encontrado
        }
    }
    
    }//GEN-LAST:event_TableProductoMouseClicked

    private void TableVentaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableVentaMouseEntered
        // TODO add your handling code here:
      
    }//GEN-LAST:event_TableVentaMouseEntered

    private void TableVentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableVentaMouseClicked
   
                // TODO add your handling code here:
    }//GEN-LAST:event_TableVentaMouseClicked

    private void TableVentaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableVentaMousePressed
//int fila = TableVenta.rowAtPoint(evt.getPoint());
                 // TODO add your handling code here:
    }//GEN-LAST:event_TableVentaMousePressed

    private void TableVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TableVentaKeyPressed
         // TODO add your handling code here:
    }//GEN-LAST:event_TableVentaKeyPressed

    private void btnBusccarProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusccarProActionPerformed
   // Definir el origen antes de crear la ventana
    origenActual = "venta";

    // Crear FrmBusqueda pasando idEmpresaActiva y origenActual
    FrmBusqueda2 BuscarProd = new FrmBusqueda2(this.idEmpresaActiva, origenActual);
    BuscarProd.setVisible(true);
    
    }//GEN-LAST:event_btnBusccarProActionPerformed
private void abrirVentanaBuscar() {
    System.out.println("Abriendo FrmBusqueda con empresa activa: " + this.idEmpresaActiva
                       + " y origen: " + this.origenActual);

    FrmBusqueda2 busqueda = new FrmBusqueda2(this.idEmpresaActiva, this.origenActual);
    busqueda.setVisible(true);
}



    
 
    private void txtCodigoVentaCreditClientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoVentaCreditClientActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoVentaCreditClientActionPerformed

    private void txtCodigoVentaCreditClientKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaCreditClientKeyPressed
   
    }//GEN-LAST:event_txtCodigoVentaCreditClientKeyPressed

    private void txtCodigoVentaCreditClientKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaCreditClientKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoVentaCreditClientKeyTyped

    private void txtCantidadVenta1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadVenta1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVenta1ActionPerformed

    private void txtCantidadVenta1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVenta1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVenta1KeyPressed

    private void txtCantidadVenta1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVenta1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVenta1KeyTyped

    private void txtPrecioVenta1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioVenta1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioVenta1ActionPerformed

    private void btnEliminarventa1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarventa1ActionPerformed
  modelo = (DefaultTableModel) TableCreditClient.getModel();
    int filaSeleccionada = TableCreditClient.getSelectedRow();

    if (filaSeleccionada != -1) {
        modelo.removeRow(filaSeleccionada);
        
    }

    txtCodigoVentaCreditClient.requestFocus();   
    }//GEN-LAST:event_btnEliminarventa1ActionPerformed

    private void txtRucVentaCreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRucVentaCreditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRucVentaCreditActionPerformed

    private void txtRucVentaCreditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaCreditKeyPressed
     
    }//GEN-LAST:event_txtRucVentaCreditKeyPressed

    public void EnterClienteCredit(){
       } 
    private void txtRucVentaCreditKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaCreditKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRucVentaCreditKeyReleased

    private void txtRucVentaCreditKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaCreditKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRucVentaCreditKeyTyped

    private void btnGenerarVentaCreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarVentaCreditActionPerformed
  
    }//GEN-LAST:event_btnGenerarVentaCreditActionPerformed

    private void btnBusccarPro1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusccarPro1ActionPerformed
 
    }//GEN-LAST:event_btnBusccarPro1ActionPerformed

    private void BtnCreditoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCreditoClienteActionPerformed
  Menu.setSelectedIndex(8); 
 
    origenActual = "credito";
   txtRucVentaCredit.requestFocus();// TODO add your handling code here:
    }//GEN-LAST:event_BtnCreditoClienteActionPerformed

    private void TableCreditClientMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableCreditClientMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_TableCreditClientMouseClicked

    private void TableCreditClientMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableCreditClientMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_TableCreditClientMouseEntered

    private void TableCreditClientMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableCreditClientMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TableCreditClientMousePressed

    private void TableCreditClientKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TableCreditClientKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TableCreditClientKeyPressed

    private void btnCobrarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCobrarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCobrarKeyPressed

    private void txtIDProductKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIDProductKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDProductKeyTyped

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
//TotalPagarCreditoCliente();
    if (sistemaYaIniciado) cargarPromosVigentes();        
    TotalPagarX();
        EnterClienteVenta();
        EnterClienteCredit();
        

// TODO add your handling code here:

    }//GEN-LAST:event_formWindowActivated

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentShown

    private void txtCantidadVentaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyReleased
        
    }//GEN-LAST:event_txtCantidadVentaKeyReleased

    private void txtCantProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantProKeyTyped
       event.numberDecimalKeyPress(evt, txtCantPro);        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantProKeyTyped

    private void jPanel17KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel17KeyPressed
        // TODO aing code here:
    }//GEN-LAST:event_jPanel17KeyPressed
private double totalPagar = 0.0;
    private void jPanel2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel2KeyPressed
        // Detectar si la tecla presionada es F12
    if (evt.getKeyCode() == KeyEvent.VK_F12) {
        if (TableVenta.getRowCount() > 0) {
                ventanaCobrar cobrar = new ventanaCobrar(this);  // Sin parámetros
        cobrar.setTotal(TotalPagar);                // Luego asignas el total
        cobrar.setVisible(
                true);
        } else {
            JOptionPane.showMessageDialog(null, "No hay productos en la venta.");
            txtCodigoVenta.requestFocus();
        }
    }  
    
    }//GEN-LAST:event_jPanel2KeyPressed
private double calcularTotalPagar() {
    double total = 0;
    for (int i = 0; i < TableVenta.getRowCount(); i++) {
        double precio = Double.parseDouble(TableVenta.getValueAt(i, 3).toString());
        int cantidad = Integer.parseInt(TableVenta.getValueAt(i, 2).toString());
        total += precio * cantidad;
    }
    return total;
}

    private void jPanel2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel2KeyTyped
  
    }//GEN-LAST:event_jPanel2KeyTyped

    private void jPanel18KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel18KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel18KeyPressed

    private void BtnCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCorteActionPerformed
     // Protección contra usuario nulo
    if (usuarioLogueado == null) {
        System.out.println("DEBUG: BtnCorte pulsado pero usuarioLogueado es null");
        return;
    }

    // Comprobar permisos / validación
    boolean acceso = usuarioLogueado.getRol().equals("Administrador") || corteValidado;
    if (!acceso) {
        acceso = rolController.solicitarClaveAdministrador(); // solo se ejecuta si no hay acceso previo
    }

    if (acceso) {
        corteValidado = true;

        // Buscar dinámicamente el panel de Corte en el JTabbedPane
        int idx = Menu.indexOfComponent(jPanelMenuCorte);
        if (idx == -1) {
            // fallback: buscar por título "Corte de caja"
            for (int i = 0; i < Menu.getTabCount(); i++) {
                if ("Corte de caja".equalsIgnoreCase(Menu.getTitleAt(i))) {
                    idx = i;
                    break;
                }
            }
        }

        if (idx != -1) {
            System.out.println("DEBUG: Abriendo pestaña Corte en índice " + idx);
            Menu.setSelectedIndex(idx);
            Menu.getComponentAt(idx).revalidate();
            Menu.getComponentAt(idx).repaint();
        } else {
            System.out.println("DEBUG: No se encontró la pestaña Corte de caja en Menu");
        }
    }
    }//GEN-LAST:event_BtnCorteActionPerformed

        private void BtnCerrarSActionPerformed(java.awt.event.ActionEvent evt) {                                         
     // Protección contra usuario nulo
    if (usuarioLogueado == null) {
        System.out.println("DEBUG: BtnCerrarS pulsado pero usuarioLogueado es null");
        // Aquí podrías abrir el diálogo de confirmación de cierre sin usuario o forzar volver al login
        return;
    }

    // Si tu flujo es pedir clave igual que en corte, reutiliza la lógica; si no, muestra confirmación de logout
    boolean acceso = usuarioLogueado.getRol().equals("Administrador") || corteValidado;
    if (!acceso) {
        acceso = rolController.solicitarClaveAdministrador();
    }

    if (acceso) {
        // Si quieres abrir la pestaña "Cerrar Sesión" en el JTabbedPane:
        int idx = -1;
        for (int i = 0; i < Menu.getTabCount(); i++) {
            if ("Cerrar Sesión".equalsIgnoreCase(Menu.getTitleAt(i)) || "CerrarSesion".equalsIgnoreCase(Menu.getTitleAt(i))) {
                idx = i;
                break;
            }
        }

        if (idx != -1) {
            System.out.println("DEBUG: Abriendo pestaña Cerrar Sesión en índice " + idx);
            Menu.setSelectedIndex(idx);
        } else {
            // fallback: ejecutar el flujo de cierre (diálogo)
            System.out.println("DEBUG: pestaña Cerrar Sesión no encontrada, mostrando diálogo de cierre");
            // ejemplo de diálogo:
            int opt = javax.swing.JOptionPane.showConfirmDialog(this, "¿Deseas cerrar sesión?", "Cerrar sesión", javax.swing.JOptionPane.YES_NO_OPTION);
            if (opt == javax.swing.JOptionPane.YES_OPTION) {
                // tu lógica de logout
                System.out.println("DEBUG: Usuario confirmó cierre de sesión");
                // reiniciar o mostrar login
            } else {
                System.out.println("DEBUG: Usuario canceló cierre de sesión");
            }
        }
    }
    }
private void btnConfiguracionesActionPerformed(java.awt.event.ActionEvent evt) {
    if (jPanelConfig == null) {
        System.out.println("DEBUG: jPanelConfig es null");
        return;
    }
    int index = Menu.indexOfComponent(jPanelConfig);
    if (index != -1) {
        System.out.println("DEBUG: btnConfig abre pestaña Config Dispositivos en índice " + index);
        Menu.setSelectedIndex(index);
        Menu.getComponentAt(index).revalidate();
        Menu.getComponentAt(index).repaint();
    } else {
        System.out.println("DEBUG: jPanelConfig no encontrado en Menu. Intentando reinsertar...");
        // reinsertar en penúltima posición antes de Cerrar Sesión
        Menu.insertTab("Config Dispositivos", null, jPanelConfig, null, Math.max(0, Menu.getTabCount() - 1));
        int newIdx = Menu.indexOfComponent(jPanelConfig);
        System.out.println("DEBUG: jPanelConfig reinsertado en índice " + newIdx);
        if (newIdx != -1) Menu.setSelectedIndex(newIdx);
    }
}



    private void menuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuActionPerformed
  // Crear y mostrar el diálogo modal de opciones
    MenuOpcionesForm ventana = new MenuOpcionesForm(this, true);
    ventana.setVisible(true);
    }//GEN-LAST:event_menuActionPerformed

    private void btnGenerarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnGenerarVentaActionPerformed

    private void txtPreciocompraProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPreciocompraProKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPreciocompraProKeyTyped

    private void txtCantProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantProActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantProActionPerformed

    private void txtCodigoProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoProActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoProActionPerformed

    private void txtPreciocompraProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPreciocompraProActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPreciocompraProActionPerformed
   private void filtrar(){

   try{
       

   sorter.setRowFilter(RowFilter.regexFilter(txtBuscar.getText().trim(),1,2,3,5,6));
   
   }catch (Exception e){
   }
   }
    
    
    /**
     * @param args the command line arguments
     */
  public static void main(String args[]) {
    try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Windows".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (Exception ex) {
        java.util.logging.Logger.getLogger(Sistema.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }

    // ✅ Solo se muestra el Login, el resto del flujo lo maneja Login
    java.awt.EventQueue.invokeLater(() -> {
        new Vista.Login().setVisible(true);
    });
}


void Operacion(){
     
    double num1=Double.parseDouble(lblEnviaTotal.getText()); 
    double num2=Double.parseDouble(txtPaga1.getText());   
   
   

   double resta=0.0;
      resta = num2 - num1; 
    lblcambio.setText(String.valueOf(resta));
}

 


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCorte;
    private javax.swing.JButton BtnCreditoCliente;
    public static javax.swing.JLabel LabelVendedor;
    public static javax.swing.JTabbedPane Menu;
    public com.toedter.calendar.JDateChooser Midate;
    private com.toedter.calendar.JDateChooser Midate1;
    private javax.swing.JTable TableCliente;
    public static javax.swing.JTable TableCreditClient;
    public static javax.swing.JTable TableEntrada;
    public static javax.swing.JTable TableProducto;
    private javax.swing.JTable TableProveedor;
    private javax.swing.JTable TableUsuarios;
    public static javax.swing.JTable TableVenta;
    private javax.swing.JTable TableVentas;
    public static javax.swing.JLabel TotalEntrada;
    private javax.swing.JButton btnActualizarConfig;
    private javax.swing.JButton btnBusccarPro;
    private javax.swing.JButton btnBusccarPro1;
    private javax.swing.JButton btnClientes;
    public static javax.swing.JButton btnCobrar;
    private javax.swing.JButton btnConfig;
    private javax.swing.JButton btnEditarCliente;
    private javax.swing.JButton btnEditarProveedor;
    private javax.swing.JButton btnEditarpro;
    private javax.swing.JButton btnEliminarCliente;
    private javax.swing.JButton btnEliminarPro;
    private javax.swing.JButton btnEliminarProveedor;
    private javax.swing.JButton btnEliminarentrada;
    private javax.swing.JButton btnEliminarventa;
    private javax.swing.JButton btnEliminarventa1;
    private javax.swing.JButton btnEntrada;
    private javax.swing.JButton btnGenerarEntrada;
    private javax.swing.JButton btnGenerarVenta;
    private javax.swing.JButton btnGenerarVentaCredit;
    private javax.swing.JButton btnGraficar;
    private javax.swing.JButton btnGuardarCliente;
    private javax.swing.JButton btnGuardarpro;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnNuevaVenta;
    private javax.swing.JButton btnNuevoCliente;
    private javax.swing.JButton btnNuevoPro;
    private javax.swing.JButton btnNuevoProveedor;
    private javax.swing.JButton btnPdfVentas;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnProveedor;
    private javax.swing.JButton btnTodasFilas;
    private javax.swing.JButton btnVentas;
    private javax.swing.JButton btnguardarProveedor;
    private javax.swing.JComboBox<Object> cbxProveedorPro;
    private javax.swing.JComboBox<String> cbxRol;
    private javax.swing.JButton jButton1;
    public static javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    public static javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    public static javax.swing.JPanel jPanel17;
    public static javax.swing.JPanel jPanel18;
    public static javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelMenuCorte;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    public static javax.swing.JLabel lblEnviaTotal;
    public static javax.swing.JLabel lblTotalCredit;
    private javax.swing.JLabel lblcambio;
    private javax.swing.JLabel lblcambio1;
    private javax.swing.JLabel lblcambioCredit;
    private javax.swing.JButton menu;
    private javax.swing.JLabel tipo;
    public static javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCantPro;
    private javax.swing.JTextField txtCantidadEntrada;
    private javax.swing.JTextField txtCantidadVenta;
    private javax.swing.JTextField txtCantidadVenta1;
    private javax.swing.JTextField txtCodigoEntrada;
    public static javax.swing.JTextField txtCodigoPro;
    public static javax.swing.JTextField txtCodigoVenta;
    public static javax.swing.JTextField txtCodigoVentaCreditClient;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDesPro;
    private javax.swing.JTextField txtDescripcionEntrada;
    private javax.swing.JTextField txtDescripcionVenta;
    private javax.swing.JTextField txtDireccionConfig;
    private javax.swing.JTextField txtDireccionProveedor;
    private javax.swing.JTextField txtDirecionCliente;
    private javax.swing.JTextField txtDniCliente;
    private javax.swing.JTextField txtIDProduct;
    public static javax.swing.JTextField txtIdCV;
    public static javax.swing.JTextField txtIdCV1;
    private javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtIdConfig;
    public static javax.swing.JTextField txtIdPro;
    private javax.swing.JTextField txtIdPro1;
    private javax.swing.JTextField txtIdPro2;
    private javax.swing.JTextField txtIdProveedor;
    private javax.swing.JTextField txtIdVenta;
    private javax.swing.JTextField txtIdproducto;
    private javax.swing.JTextField txtMensaje;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreCliente;
    public static javax.swing.JTextField txtNombreClienteventa;
    public static javax.swing.JTextField txtNombreClienteventaCredit;
    private javax.swing.JTextField txtNombreConfig;
    private javax.swing.JTextField txtNombreproveedor;
    private javax.swing.JTextField txtPaga1;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtPrecioEntrada;
    private javax.swing.JTextField txtPrecioPro;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtPrecioVenta1;
    private javax.swing.JTextField txtPreciocompraPro;
    private javax.swing.JTextField txtRucConfig;
    private javax.swing.JTextField txtRucProveedor;
    private javax.swing.JTextField txtRucVenta;
    public static javax.swing.JTextField txtRucVentaCredit;
    private javax.swing.JTextField txtStockDisponible;
    private javax.swing.JTextField txtStockDisponible1;
    private javax.swing.JTextField txtStockDisponible2;
    private javax.swing.JTextField txtTelefonoCliente;
    private javax.swing.JTextField txtTelefonoConfig;
    private javax.swing.JTextField txtTelefonoProveedor;
    // End of variables declaration//GEN-END:variables
    private void LimpiarCliente() {
        txtIdCliente.setText("");
        txtDniCliente.setText("");
        txtNombreCliente.setText("");
        txtTelefonoCliente.setText("");
        txtDirecionCliente.setText("");
    }

    private void LimpiarProveedor() {
        txtIdProveedor.setText("");
        txtRucProveedor.setText("");
        txtNombreproveedor.setText("");
        txtTelefonoProveedor.setText("");
        txtDireccionProveedor.setText("");
    }

    private void LimpiarProductos() {
        txtIdPro.setText("");
        txtCodigoPro.setText("");
        //cbxProveedorPro.setSelectedItem(null);
        txtDesPro.setText("");
        txtCantPro.setText("");
        txtPrecioPro.setText("");
            txtPreciocompraPro.setText("");
         txtBuscar.setText("");
    }
//suma las filas de la tabla venta
   public void TotalPagarX() {
        aplicarPromociones(); 
        TotalPagar = 0.00;
        int numFila = TableVenta.getRowCount();
        for (int i = 0; i < numFila; i++) {
            double cal = Double.parseDouble(String.valueOf(TableVenta.getModel().getValueAt(i, 4)));
            
            TotalPagar = TotalPagar + cal;
        }
        lblEnviaTotal.setText(String.format("%.2f", TotalPagar));
    }
    
    private void TotalPagarEntrada() {
    TotalpagarEntrada = 0.00;
    int numFila = TableEntrada.getRowCount();

    if (numFila == 0) {
        TotalEntrada.setText("0.00"); // 🔥 si no hay filas, forzamos a 0
        return;
    }

    for (int i = 0; i < numFila; i++) {
        double cal = Double.parseDouble(
            String.valueOf(TableEntrada.getModel().getValueAt(i, 4))
        );
        TotalpagarEntrada += cal;
    }
    TotalEntrada.setText(String.format("%.2f", TotalpagarEntrada));
     }
  
    private void LimparVenta() {
        txtCodigoVentaCreditClient.setText("");
        txtCodigoVenta.setText("");
        txtDescripcionVenta.setText("");
       // txtCantidadVenta.seftText("");
        txtStockDisponible.setText("");
        txtPrecioVenta.setText("");
        txtIdVenta.setText("");
        
        
    }
   
     public void LimpiarCobro() {
        lblEnviaTotal.setText("");
          txtPaga1.setText("");
           lblcambio.setText("");
    }

        private void LimpiarEntrada() {
  
        txtCodigoEntrada.setText("");
      
        txtDescripcionEntrada.setText("");
  
        txtCantidadEntrada.setText("");
         txtPrecioEntrada.setText("");
        txtStockDisponible1.setText("");
       
     
       
                 }
private void RegistrarVenta() {
    try {
        // 1️⃣ Crear objeto Venta
        Venta v = new Venta();
        
        // 2️⃣ Obtener cliente, vendedor y monto
        int cliente = Integer.parseInt(txtIdCV.getText());
        String vendedor = LabelVendedor.getText();
        double monto = TotalPagar;

        // 3️⃣ Asignar datos al objeto Venta
        v.setCliente(cliente);
        v.setVendedor(vendedor);
        v.setTotal(monto);
        v.setFecha(fechaActual);

        // 4️⃣ 🔹 Asignar id_empresa activo
        int idEmpresa = Sistema.getIdEmpresaActiva();
        if (idEmpresa <= 0) {
            throw new Exception("❌ No se puede registrar la venta: id_empresa no está definido o es inválido.");
        }
        v.setIdEmpresa(idEmpresa);

        // 5️⃣ Registrar la venta en la base de datos
        int idVentaGenerada = Vdao.RegistrarVenta(v);
        System.out.println("✅ Venta registrada con ID: " + idVentaGenerada + " para empresa: " + idEmpresa);

    } catch (NumberFormatException e) {
        System.out.println("Error al convertir el ID del cliente: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("Error al registrar la venta: " + e.getMessage());
    }
}

  private void RegistrarEntrada() {
    try {
        // Asegurarse de que el objeto v está inicializado
        Venta v = new Venta();

        // Obtener el vendedor
        String vendedor = LabelVendedor.getText();
        
        // Obtener fecha actual (si es necesario formatearla)
        Date fecha = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fechaActual = sdf.format(fecha);
        
       
        v.setFecha(fechaActual);
        v.setVendedor(vendedor);
      
        Vdao.RegistrarVenta(v);
    } catch (Exception e) {
        System.out.println("Error al registrar la entrada: " + e.getMessage());
    }
}

    
      private void RegistrarDetalleEntrada() {
       int id = Vdao.IdVenta();
        for (int i = 0; i < TableEntrada.getRowCount(); i++) {
            int id_pro = Integer.parseInt(TableEntrada.getValueAt(i, 0).toString());
            int cant = Integer.parseInt(TableEntrada.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(TableEntrada.getValueAt(i, 3).toString());
            Dv.setId_pro(id_pro);
            Dv.setCantidad(cant);
            Dv.setPrecio(precio);
            Dv.setId(id);
           Vdao.RegistrarDetalle(Dv);

        }
  
    }


 private void ActualizarStockEntrada() {
    for (int i = 0; i < TableEntrada.getRowCount(); i++) {
        int id = Integer.parseInt(TableEntrada.getValueAt(i, 0).toString());
        int cant = Integer.parseInt(TableEntrada.getValueAt(i, 2).toString());

        // Buscar producto filtrando por la empresa del usuario logueado
        pro = proDao.BuscarId(id, idEmpresaActiva);

        if (pro != null) {
            int StockActual = pro.getStock() + cant;
            // Pasar idEmpresaActiva al método
            boolean actualizado = Vdao.ActualizarStockEntrada(StockActual, id, idEmpresaActiva);
            if(!actualizado){
                System.out.println("No se pudo actualizar stock del producto ID=" + id);
            }
        } else {
            System.out.println("Producto con ID " + id + " no pertenece a la empresa activa.");
        }
    }
}

   //------credito cliente-------------
 public void LimpiarTableVenta() {
    DefaultTableModel tmp = (DefaultTableModel) TableVenta.getModel();
    tmp.setRowCount(0);  // Limpia todas las filas
    lblEnviaTotal.setText("");
}
     private void LimpiarTableEntrada() {
        tmp = (DefaultTableModel) TableEntrada.getModel();
        int fila = TableEntrada.getRowCount();
        for (int i = 0; i < fila; i++) {
            tmp.removeRow(0);
        }
    }
   
    private void LimpiarClienteventa() {
        txtRucVenta.setText("");
        txtNombreClienteventa.setText("");
        txtIdCV.setText("");
    }
    
        private void nuevoUsuario(){
        txtNombre.setText("");
        txtCorreo.setText("");
        txtPass.setText("");
    }
 private void llenarProveedor() {
    cbxProveedorPro.removeAllItems(); // Limpia los ítems anteriores
    cbxProveedorPro.addItem(new Combo(0, "Seleccione")); // Ítem por defecto

    // Listar solo proveedores de la empresa del usuario logueado
    List<Proveedor> lista = PrDao.ListarProveedorPorEmpresa(usuarioLogueado.getIdEmpresa());

    for (Proveedor p : lista) {
        cbxProveedorPro.addItem(new Combo(p.getId(), p.getNombre()));
    }
    // 🔹 Al final agregamos la opción especial
    cbxProveedorPro.addItem(new Combo(-1, "➕ Agregar proveedor..."));
}

public void cerrarSesion() {
    // Guardamos el correo del usuario actual para recordar en Login
    if (usuarioLogueado != null) {
        Utilidades.ConfigApp.guardarDatos(idEmpresaActiva, usuarioLogueado.getCorreo());
    }
    // 🔒 Cerrar pool de conexiones antes de reiniciar
    Conexion.closeDataSource();

    // Reiniciar la instancia del sistema
    reiniciarInstancia();
}

//promociones
private static boolean esFilaPromo(Object idCelda) {
    return idCelda != null && idCelda.toString().startsWith("P-");
}

public void cargarPromosVigentes() {
    try {
        promosVigentes = new PromocionDao().listarVigentes(idEmpresaActiva,
                java.time.LocalDate.now(java.time.ZoneId.of("America/Monterrey")));
    } catch (Exception ex) {
        System.err.println("No se pudieron cargar las promociones: " + ex.getMessage());
    }
    System.out.println("[PROMO] vigentes cargadas: " + promosVigentes.size() + " (empresa " + idEmpresaActiva + ")");

}

private void aplicarPromociones() {
    if (aplicandoPromos) return;
    aplicandoPromos = true;
    try {
        DefaultTableModel m = (DefaultTableModel) TableVenta.getModel();

        // 1. quitar las filas de promo anteriores
       for (int i = m.getRowCount() - 1; i >= 0; i--) {
    if (esFilaPromo(m.getValueAt(i, 0))) m.removeRow(i);
}
System.out.println("[PROMO] aplicar: promos="
        + (promosVigentes == null ? -1 : promosVigentes.size()) + ", filas=" + m.getRowCount());
if (promosVigentes == null || promosVigentes.isEmpty() || m.getRowCount() == 0) return;

        // 2. líneas de productos
        List<CalculadoraPromociones.Linea> lineas = new ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            lineas.add(new CalculadoraPromociones.Linea(
                    Integer.parseInt(m.getValueAt(i, 0).toString()),
                    Double.parseDouble(m.getValueAt(i, 2).toString().replace(",", ".")),
                    Double.parseDouble(m.getValueAt(i, 3).toString())));
        }

        // 3. calcular y preparar la fila de cada promo (debajo del último producto de su grupo)
        List<Object[]> pendientes = new ArrayList<>(); // {índice, fila}
        for (CalculadoraPromociones.Aplicada a : CalculadoraPromociones.calcular(lineas, promosVigentes)) {
            Promocion promo = null;
            for (Promocion p : promosVigentes) {
                if (p.getId() == a.idPromocion) { promo = p; break; }
            }
            if (promo == null) continue;

            int ultimo = -1;
            for (int i = 0; i < m.getRowCount(); i++) {
                if (promo.getIdProductos().contains(Integer.parseInt(m.getValueAt(i, 0).toString()))) ultimo = i;
            }
            Object[] fila = {"P-" + a.idPromocion, "Promo: " + a.descripcion, a.paquetes, "", -a.descuento};
            pendientes.add(new Object[]{ultimo + 1, fila});
        }

        // 4. insertar de abajo hacia arriba para no mover los índices
        pendientes.sort((x, y) -> (Integer) y[0] - (Integer) x[0]);
        for (Object[] p : pendientes) {
            m.insertRow((Integer) p[0], (Object[]) p[1]);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        aplicandoPromos = false;
    }
    
}

// Pinta las filas de promo en verde y cursiva
private void instalarRendererPromo() {
    for (int c = 0; c < TableVenta.getColumnCount(); c++) {
        final TableCellRenderer base = TableVenta.getColumnModel().getColumn(c).getCellRenderer();
        TableVenta.getColumnModel().getColumn(c).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            TableCellRenderer r = (base != null) ? base
                    : tbl.getDefaultRenderer(val == null ? Object.class : val.getClass());
            Component comp = r.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
            if (!sel && esFilaPromo(tbl.getValueAt(row, 0))) {
                comp.setForeground(new Color(0, 128, 0));
                comp.setBackground(new Color(232, 245, 233));
                comp.setFont(comp.getFont().deriveFont(Font.ITALIC));
            }
            return comp;
        });
    }
}
}
