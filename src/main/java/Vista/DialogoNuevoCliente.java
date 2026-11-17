
package Vista;

import Modelo.Cliente;
import Modelo.ClienteDao;
import Estilos.EstiloDialogoNuevoCliente;

import java.awt.*;
import javax.swing.*;

public class DialogoNuevoCliente extends JDialog {

    private final ClienteDao clienteDao = new ClienteDao();
    private final int idEmpresa;

    private Cliente clienteRegistrado;

    private final JTextField txtDni = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtDireccion = new JTextField();

    private final JButton btnGuardar = new JButton("Guardar");

    public DialogoNuevoCliente(Window owner, int idEmpresa) {

        super(owner, "Agregar nuevo cliente", ModalityType.APPLICATION_MODAL);

        this.idEmpresa = idEmpresa;

        construirUI();
        configurarCampoDni();
        // Validaciones de campos numéricos
        configurarCampoNumerico(txtDni, 8);
        configurarCampoNumerico(txtTelefono, 15);
        conectarEventos();

        setSize(460, 420);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {

        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // Encabezado
        JPanel encabezado =
                EstiloDialogoNuevoCliente.crearEncabezado();

        // Formulario
        JPanel formulario = new JPanel(new GridBagLayout());

        EstiloDialogoNuevoCliente.estilizarFormulario(formulario);

        agregarCampo(formulario, 0, "CLIENTE_ID:", txtDni);
        agregarCampo(formulario, 1, "Nombre:", txtNombre);
        agregarCampo(formulario, 2, "Teléfono:", txtTelefono);
        agregarCampo(formulario, 3, "Dirección:", txtDireccion);

        // Panel de botones
        JPanel botones =
                EstiloDialogoNuevoCliente.crearPanelBotones();

        EstiloDialogoNuevoCliente.estilizarBotonGuardar(btnGuardar);

        botones.add(btnGuardar);

        // Agregar componentes
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void agregarCampo(
            JPanel panel,
            int fila,
            String etiqueta,
            JTextField campo) {

        GridBagConstraints c = new GridBagConstraints();

        // Etiqueta
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(9, 0, 9, 15);

        JLabel lbl = new JLabel(etiqueta);

        EstiloDialogoNuevoCliente.estilizarEtiqueta(lbl);

        panel.add(lbl, c);

        // Campo de texto
        c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = fila;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(9, 0, 9, 0);

        EstiloDialogoNuevoCliente.estilizarCampo(campo);

        panel.add(campo, c);
    }

    private void conectarEventos() {

        btnGuardar.addActionListener(e -> guardarCliente());

        getRootPane().setDefaultButton(btnGuardar);
    }

    private void guardarCliente() {

        String dni = txtDni.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validar campos obligatorios
        if (dni.isEmpty() || nombre.isEmpty()
                || telefono.isEmpty() || direccion.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Todos los campos son obligatorios.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Validar DNI
        if (dni.length() > 8) {

            JOptionPane.showMessageDialog(
                    this,
                    "El DNI no puede superar los 8 caracteres.",
                    "DNI inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtDni.requestFocusInWindow();
            return;
        }

        // Validar teléfono
        if (telefono.length() > 15) {

            JOptionPane.showMessageDialog(
                    this,
                    "El teléfono no puede superar los 15 caracteres.",
                    "Teléfono inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            txtTelefono.requestFocusInWindow();
            return;
        }

        // Crear cliente
        Cliente nuevo = new Cliente();

        nuevo.setDni(dni);
        nuevo.setNombre(nombre);
        nuevo.setTelefono(telefono);
        nuevo.setDireccion(direccion);
        nuevo.setIdEmpresa(idEmpresa);

        // Registrar cliente
        if (!clienteDao.RegistrarCliente(nuevo)) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo registrar el cliente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Recuperar el ID interno generado por MySQL
        clienteRegistrado = clienteDao.BuscarClientePorDni(
                dni,
                idEmpresa
        );

        if (clienteRegistrado == null
                || clienteRegistrado.getId() <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "El cliente se registró, pero no se pudo recuperar su ID.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Cliente registrado correctamente.",
                "Registro exitoso",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
    }

    public Cliente getClienteRegistrado() {
        return clienteRegistrado;
    }
    private void configurarCampoDni() {

    ((javax.swing.text.AbstractDocument) txtDni.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {

        @Override
        public void insertString(
                FilterBypass fb,
                int offset,
                String texto,
                javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {

            if (texto != null && texto.matches("[0-9]+")
                    && fb.getDocument().getLength() + texto.length() <= 8) {
                super.insertString(fb, offset, texto, attr);
            }
        }

        @Override
        public void replace(
                FilterBypass fb,
                int offset,
                int length,
                String texto,
                javax.swing.text.AttributeSet attrs)
                throws javax.swing.text.BadLocationException {

            if (texto != null
                    && texto.matches("[0-9]*")
                    && fb.getDocument().getLength() - length + texto.length() <= 8) {
                super.replace(fb, offset, length, texto, attrs);
            }
        }
    });
}
    private void configurarCampoNumerico(JTextField campo, int maxDigitos) {

    ((javax.swing.text.AbstractDocument) campo.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {

        @Override
        public void insertString(
                FilterBypass fb,
                int offset,
                String texto,
                javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {

            if (texto != null
                    && texto.matches("[0-9]+")
                    && fb.getDocument().getLength() + texto.length() <= maxDigitos) {

                super.insertString(fb, offset, texto, attr);
            }
        }

        @Override
        public void replace(
                FilterBypass fb,
                int offset,
                int length,
                String texto,
                javax.swing.text.AttributeSet attrs)
                throws javax.swing.text.BadLocationException {

            if (texto != null
                    && texto.matches("[0-9]*")
                    && fb.getDocument().getLength() - length + texto.length() <= maxDigitos) {

                super.replace(fb, offset, length, texto, attrs);
            }
        }
    });
}
}
