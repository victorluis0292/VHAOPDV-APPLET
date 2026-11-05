
package Vista;

import Controlador.EmpresaController;
import Controlador.TurnoController;
import Estilos.Estilos;
import LicenciasLocales.LicenciaValidadorLocalSerie;
import Modelo.*;
import Utilidades.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

    private loginDAO loginDao = new loginDAO();
    private JTextField txtCorreo, txtIdEmpresa;
    private JPasswordField txtPass;
    private JButton btnIniciar;
    private JLabel lblPaypal, lblLinkPaypal, lblLogo;
    private JCheckBox chkRecordar;

    public Login() {

        setTitle("Inicio de Sesión - VHAO Punto de venta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 678);
        setLocationRelativeTo(null);
        setLayout(null);

        // 🔹 Verificar actualizaciones en segundo plano
        VerificadorActualizacion.verificar();

        // 🔹 Label con imagen de fondo
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/Img/login.jpg"));
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setBounds(160, 0, 700, 500);
        backgroundLabel.setLayout(null);
        add(backgroundLabel);

        // 🔹 Panel login amarillo
        JPanel panelLogin = new JPanel(null);
        panelLogin.setBackground(new Color(255, 255, 153, 210));
        panelLogin.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLUE),
                "Iniciar Sesión",
                0, 0,
                new Font("Times New Roman", Font.BOLD, 22),
                Color.BLUE
        ));
        panelLogin.setBounds(190, 40, 320, 440);
        backgroundLabel.add(panelLogin);

        // Campo ID Empresa
        JLabel lblIdEmpresa = new JLabel("ID Empresa");
        lblIdEmpresa.setBounds(30, 60, 150, 20);
        panelLogin.add(lblIdEmpresa);

        txtIdEmpresa = new JTextField();
        Estilos.estiloCampo(txtIdEmpresa);
        txtIdEmpresa.setBounds(30, 80, 240, 30);
        panelLogin.add(txtIdEmpresa);

        // Link "Cambio de empresa"
        JLabel lblCambioEmpresa = new JLabel("<html><u>Cambio de empresa</u></html>");
        lblCambioEmpresa.setForeground(Color.BLUE);
        lblCambioEmpresa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblCambioEmpresa.setBounds(180, 60, 150, 20);
        panelLogin.add(lblCambioEmpresa);

        lblCambioEmpresa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ConfigApp.cambiarDeEmpresa(Login.this, txtCorreo, txtIdEmpresa, txtPass);
            }
        });

        // Campo Correo
        JLabel lblCorreo = new JLabel("Correo Electrónico");
        lblCorreo.setBounds(30, 120, 150, 20);
        panelLogin.add(lblCorreo);

        txtCorreo = new JTextField();
        Estilos.estiloCampo(txtCorreo);
        txtCorreo.setBounds(30, 140, 240, 30);
        panelLogin.add(txtCorreo);

        // Campo Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(30, 180, 150, 20);
        panelLogin.add(lblPass);

        txtPass = new JPasswordField();
        Estilos.estiloCampo(txtPass);
        txtPass.setBounds(30, 200, 240, 30);
        panelLogin.add(txtPass);

        txtPass.addActionListener(e -> btnIniciar.doClick());

        // Checkbox "Recordarme"
        chkRecordar = new JCheckBox("Recordarme");
        chkRecordar.setBounds(30, 240, 150, 25);
        panelLogin.add(chkRecordar);

        // 🔹 Precargar datos guardados
        String correoGuardado = ConfigApp.getCorreo();
        int idEmpresaGuardada = ConfigApp.getIdEmpresa();

        if (correoGuardado != null && !correoGuardado.isEmpty()) {
            txtCorreo.setText(correoGuardado);
            chkRecordar.setSelected(true);
        }

        if (idEmpresaGuardada > 0) {
            txtIdEmpresa.setText(String.valueOf(idEmpresaGuardada));
        }

        // Botón Iniciar Sesión
        btnIniciar = new JButton("Login");
        btnIniciar.setBackground(new Color(0, 0, 204));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIniciar.setBounds(90, 270, 120, 35);
        btnIniciar.addActionListener(e -> validar());
        panelLogin.add(btnIniciar);

        // Botón Registrar
        JButton btnRegistrar = BotonRegistrar.crearBoton();
        btnRegistrar.setBounds(90, 315, 120, 35);
        btnRegistrar.addActionListener(e -> new EmpresaController().abrirFormularioRegistro());
        panelLogin.add(btnRegistrar);

        // Link PayPal
        lblPaypal = new JLabel(
                "Invítame un Café :",
                new ImageIcon(getClass().getResource("/Img/vasocafe.png")),
                JLabel.LEFT
        );
        lblPaypal.setBounds(30, 360, 240, 65);
        lblPaypal.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblPaypal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelLogin.add(lblPaypal);

        lblPaypal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirPaypal();
            }
        });

        lblLinkPaypal = new JLabel("http://paypal.me/victorluishernandez");
        lblLinkPaypal.setForeground(new Color(0, 0, 204));
        lblLinkPaypal.setBounds(30, 385, 250, 60);
        lblLinkPaypal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblLinkPaypal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirPaypal();
            }
        });

        panelLogin.add(lblLinkPaypal);

        // Panel lateral izquierdo (gris)
        JPanel panelIzquierdo = new JPanel(null);
        panelIzquierdo.setBackground(new Color(230, 230, 230));
        panelIzquierdo.setBounds(0, 0, 350, 550);
        add(panelIzquierdo);

        lblLogo = new JLabel(
                new ImageIcon(getClass().getResource("/Img/LOGO-VHAO-SYSTEM.png"))
        );
        lblLogo.setBounds(0, 0, 180, 120);
        panelIzquierdo.add(lblLogo);

        // Link "Activar licencia"
        JLabel lblActivar = new JLabel("<html><u>Activar licencia</u></html>");
        lblActivar.setForeground(Color.BLUE);
        lblActivar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblActivar.setBounds(20, 500, 150, 20);
        panelIzquierdo.add(lblActivar);

        // Ocultar en producción
        System.out.println("Login detectó ambiente: " + Conexion.getEnvironment());

        if (!Conexion.esLocal()) {
            lblActivar.setVisible(false);
        }

        lblActivar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idLicenciaActiva = 5;

                VentanaValidarTecnico ventanaTecnico =
                        new VentanaValidarTecnico(Login.this, idLicenciaActiva);

                ventanaTecnico.setVisible(true);
            }
        });

        // Label versión
        JLabel lblVersionApp = new JLabel("Versión: " + ConfigApp.getVersion());
        lblVersionApp.setBounds(20, 525, 200, 20);
        lblVersionApp.setFont(new Font("BOLD", Font.PLAIN, 12));
        lblVersionApp.setForeground(Color.DARK_GRAY);
        panelIzquierdo.add(lblVersionApp);

        // Menú inferior
        MenuInferiorLogin menuInferior = new MenuInferiorLogin();
        menuInferior.setBounds(0, 550, 700, 60);
        add(menuInferior);

        setVisible(true);
    }

    public void validar() {

        if (!Conexion.hayInternetRapido()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No tienes conexión a internet.",
                    "Sin conexión",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String correo = txtCorreo.getText().trim();
        String pass = new String(txtPass.getPassword());
        String idEmpStr = txtIdEmpresa.getText().trim();

        if (correo.isEmpty() || pass.isEmpty() || idEmpStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor ingresa correo, contraseña y empresa"
            );
            return;
        }

        int idEmpresa;

        try {
            idEmpresa = Integer.parseInt(idEmpStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de empresa inválido");
            return;
        }

        login lg = loginUsuario(correo, pass, idEmpresa);

        if (lg == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Correo, contraseña o empresa incorrectos"
            );
            return;
        }

        // Guardar o limpiar datos según "Recordarme"
        if (chkRecordar.isSelected()) {
            ConfigApp.guardarDatos(lg.getIdEmpresa(), lg.getCorreo());
        } else {
            ConfigApp.limpiarDatos();
        }

        Sistema.setUsuarioActivo(lg.getCorreo().toLowerCase());
        Sistema.setIdEmpresaActiva(lg.getIdEmpresa());

        EmpresaDao empresaDao = new EmpresaDao();
        Empresa empresa = empresaDao.BuscarDatos(lg.getIdEmpresa());

        if (empresa == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se encontró información de la empresa."
            );
            return;
        }

        if (empresa.getEstado() == 0) {

            boolean internet = licenciadeprograma.hayInternet();
            boolean licenciaValida = licenciadeprograma.licenciaValida();
            long diasRestantes = licenciadeprograma.diasRestantes();
            long diasUsados = 30 - diasRestantes;

            if (!licenciaValida || diasRestantes <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "⛔ Su periodo de prueba ha expirado. Active la licencia.",
                        "Licencia requerida",
                        JOptionPane.ERROR_MESSAGE
                );
                return;

            } else {
                String fuente = internet ? "Google" : "reloj del sistema";

                JOptionPane.showMessageDialog(
                        this,
                        "⚠️ Estás usando la versión de prueba ("
                                + diasUsados + " días transcurridos, "
                                + diasRestantes + " restantes)\n"
                                + "La fecha se verificó con: " + fuente + ".",
                        "Licencia temporal",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }

        try {
            new VentaDao().asegurarClienteMostrador();
        } catch (Exception e) {
            System.err.println(
                    "⚠️ Error al asegurar cliente mostrador: " + e.getMessage()
            );
        }

        TurnoDAO turnoDAO = new TurnoDAO();
        TurnoModel turnoAbierto =
                turnoDAO.obtenerTurnoAbierto(correo.toLowerCase(), lg.getIdEmpresa());

        if (turnoAbierto != null) {
            TurnoController.setTurnoGlobal(turnoAbierto);

            Sistema sis = Sistema.getInstancia();
            sis.setLoginView(this);
            sis.inicializarSistema(lg);

            setVisible(false);

        } else {
            mostrarFormularioSaldoInicial(
                    lg,
                    correo.toLowerCase(),
                    lg.getIdEmpresa(),
                    turnoDAO
            );
        }
    }

    private login loginUsuario(String correo, String pass, int idEmpresa) {

        login usuario = loginDao.log(correo, pass, idEmpresa);

        if (usuario == null) {
            Conexion.logError(
                    "Intento de login fallido: " + correo + " | Empresa: " + idEmpresa,
                    null
            );
        } else {
            Conexion.logInfo(
                    "Usuario válido: " + usuario.getNombre()
                            + " | Empresa: " + usuario.getIdEmpresa()
            );
        }

        return usuario;
    }

    private void mostrarFormularioSaldoInicial(
            login lg,
            String usuario,
            int idEmpresa,
            TurnoDAO turnoDAO
    ) {

        CashInBoxForm cashInBoxForm = new CashInBoxForm(lg);

        cashInBoxForm.btnRegistrar.addActionListener(e -> {

            double valor = cashInBoxForm.getAmount();

            if (valor <= 0) {
                JOptionPane.showMessageDialog(
                        cashInBoxForm,
                        "Ingrese un valor mayor a 0."
                );
                return;
            }

            BigDecimal monto = BigDecimal.valueOf(valor);

            CashInBoxDAO cashDAO = new CashInBoxDAO();
            CashInBox cash = new CashInBox(monto, usuario, idEmpresa);

            if (!cashDAO.registrarSaldoInicial(cash, idEmpresa)) {
                JOptionPane.showMessageDialog(
                        cashInBoxForm,
                        "Error al registrar saldo inicial."
                );
                return;
            }

            TurnoModel nuevoTurno = new TurnoModel();
            nuevoTurno.setUsuario(usuario);
            nuevoTurno.setIdEmpresa(idEmpresa);
            nuevoTurno.setFechaInicio(
                    new java.sql.Timestamp(System.currentTimeMillis())
            );
            nuevoTurno.setSaldoInicial(monto);
            nuevoTurno.setEstado("ABIERTO");

            if (!turnoDAO.abrirTurno(nuevoTurno)) {
                JOptionPane.showMessageDialog(
                        cashInBoxForm,
                        "Error al abrir turno."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    cashInBoxForm,
                    "Saldo inicial registrado correctamente."
            );

            cashInBoxForm.dispose();
            TurnoController.setTurnoGlobal(nuevoTurno);

            Sistema sis = Sistema.getInstancia();
            sis.setLoginView(this);
            sis.inicializarSistema(lg);

            dispose();
        });

        cashInBoxForm.setVisible(true);
    }

    private void abrirPaypal() {

        try {
            if (licenciadeprograma.hayInternet()) {
                Desktop.getDesktop().browse(
                        new java.net.URI("http://paypal.me/victorluishernandez")
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No estás conectado a internet",
                        "Sin conexión",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        // 🔹 Configura el entorno: por defecto producción
        Conexion.setEnvironment(System.getProperty("app.env", "prod"));

        java.awt.EventQueue.invokeLater(() -> {

            Login login = new Login();
            login.setVisible(true);

            if (!Conexion.hayConexion()) {
                JOptionPane.showMessageDialog(
                        login,
                        "No se pudo establecer conexión con la base de datos.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
