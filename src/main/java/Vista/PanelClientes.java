
package Vista;

import Modelo.Cliente;
import Modelo.ClienteDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.IntSupplier;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class PanelClientes extends JPanel {

    private final ClienteDao client = new ClienteDao();
    private final Cliente cl = new Cliente();
    private final IntSupplier idEmpresa;

    private final JTextField txtId = new JTextField();
    private final JTextField txtDni = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtDireccion = new JTextField();

    // BUSCADOR
    private final JTextField txtBuscar = new JTextField();

    private final JButton btnGuardar = new JButton();
    private final JButton btnEditar = new JButton();
    private final JButton btnEliminar = new JButton();
    private final JButton btnNuevo = new JButton();

    private final JTable tabla = new JTable();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "CLIENTE_ID", "NOMBRE", "TELÉFONO", "DIRECCIÓN"}) {

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    public PanelClientes(IntSupplier idEmpresa) {

        this.idEmpresa = idEmpresa;

        construirUI();
        conectarEventos();
        estadoNuevo();
    }

    // ---------------------------------------------------------
    // CONSTRUIR INTERFAZ
    // ---------------------------------------------------------
    private void construirUI() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        setBorder(BorderFactory.createEmptyBorder(50, 10, 10, 10));

        // FORMULARIO IZQUIERDO
        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(new Color(204, 204, 255));
        form.setBorder(BorderFactory.createTitledBorder("Registro Cliente"));
        form.setPreferredSize(new Dimension(270, 330));

        agregarFila(form, 0, "CLIENTE_ID:", txtDni);
        agregarFila(form, 1, "Nombre:", txtNombre);
        agregarFila(form, 2, "Teléfono:", txtTelefono);
        agregarFila(form, 3, "Dirección:", txtDireccion);

        btnGuardar.setIcon(icono("/Img/GuardarTodo.png"));
        btnEditar.setIcon(icono("/Img/Actualizar (2).png"));
        btnEliminar.setIcon(icono("/Img/eliminar.png"));
        btnNuevo.setIcon(icono("/Img/nuevo.png"));

        btnGuardar.setToolTipText("Guardar cliente");
        btnEditar.setToolTipText("Modificar cliente");
        btnEliminar.setToolTipText("Desactivar cliente");
        btnNuevo.setToolTipText("Nuevo cliente");

        JPanel botones = new JPanel(
                new java.awt.GridLayout(2, 2, 30, 15));

        botones.setOpaque(false);

        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnNuevo);

        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 2;
        gc.insets = new Insets(25, 10, 10, 10);

        form.add(botones, gc);

        // TABLA
        tabla.setModel(modelo);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(10);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(80);

        // PANEL IZQUIERDO
        JPanel izquierda = new JPanel(new BorderLayout());

        izquierda.setOpaque(false);
        izquierda.add(form, BorderLayout.NORTH);

        // PANEL DERECHO CON BUSCADOR Y TABLA
        JPanel panelTabla = new JPanel(new BorderLayout(5, 8));

        panelTabla.setOpaque(false);

        JPanel panelBuscar = new JPanel(new BorderLayout(8, 0));

        panelBuscar.setBackground(Color.WHITE);
        panelBuscar.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel lblBuscar = new JLabel("Buscar cliente:");

        lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 12));

        txtBuscar.setFont(new Font("Tahoma", Font.PLAIN, 13));
        txtBuscar.setPreferredSize(new Dimension(200, 30));

        txtBuscar.setToolTipText(
                "Buscar por cliente_id o nombre del cliente");

        panelBuscar.add(lblBuscar, BorderLayout.WEST);
        panelBuscar.add(txtBuscar, BorderLayout.CENTER);

        panelTabla.add(panelBuscar, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // AGREGAR PANELES PRINCIPALES
        add(izquierda, BorderLayout.WEST);
        add(panelTabla, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------
    // AGREGAR FILAS DEL FORMULARIO
    // ---------------------------------------------------------
    private void agregarFila(
            JPanel p,
            int fila,
            String texto,
            JTextField campo) {

        JLabel lbl = new JLabel(texto);

        lbl.setFont(new Font("Tahoma", Font.BOLD, 12));

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 10, 8, 10);

        p.add(lbl, c);

        c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = fila;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.ipady = 6;
        c.insets = new Insets(8, 0, 8, 10);

        p.add(campo, c);
    }

    // ---------------------------------------------------------
    // CARGAR ICONOS
    // ---------------------------------------------------------
    private ImageIcon icono(String ruta) {

        java.net.URL url = getClass().getResource(ruta);

        return url != null ? new ImageIcon(url) : null;
    }

    // ---------------------------------------------------------
    // CONECTAR EVENTOS
    // ---------------------------------------------------------
    private void conectarEventos() {

        btnGuardar.addActionListener(e -> guardar());

        btnEditar.addActionListener(e -> editar());

        btnEliminar.addActionListener(e -> eliminar());

        btnNuevo.addActionListener(e -> estadoNuevo());

        // BUSCADOR EN TIEMPO REAL
        txtBuscar.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscar();
            }
        });

        // SELECCIONAR CLIENTE DE LA TABLA
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {

                seleccionarFila(
                        tabla.rowAtPoint(evt.getPoint()));
            }
        });
    }

    // ---------------------------------------------------------
    // API PUBLICA
    // ---------------------------------------------------------
    public void abrir() {

        txtBuscar.setText("");

        listar();

        estadoNuevo();
    }

    // ---------------------------------------------------------
    // LISTAR CLIENTES
    // RESPETA EL TEXTO DEL BUSCADOR
    // ---------------------------------------------------------
    public void listar() {

        String texto = txtBuscar.getText().trim();

        List<Cliente> lista;

        if (texto.isEmpty()) {

            lista = client.ListarCliente(idEmpresa.getAsInt());

        } else {

            lista = client.BuscarClientes(
                    texto,
                    idEmpresa.getAsInt());
        }

        modelo.setRowCount(0);

        if (lista == null) {
            return;
        }

        for (Cliente c : lista) {

            modelo.addRow(new Object[]{
                c.getId(),
                c.getDni(),
                c.getNombre(),
                c.getTelefono(),
                c.getDireccion()
            });
        }
    }

    // ---------------------------------------------------------
    // BUSCAR CLIENTES
    // ---------------------------------------------------------
    private void buscar() {

        listar();

        // Limpiar selección cuando cambia la búsqueda
        tabla.clearSelection();

        // Regresar a modo nuevo
        estadoNuevo();
    }

    // ---------------------------------------------------------
    // GUARDAR CLIENTE
    // ---------------------------------------------------------
    private void guardar() {

        if (vacio(txtDni)
                || vacio(txtNombre)
                || vacio(txtTelefono)
                || vacio(txtDireccion)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Los campos no pueden estar vacíos");

            return;
        }

        cl.setDni(txtDni.getText().trim());
        cl.setNombre(txtNombre.getText().trim());
        cl.setTelefono(txtTelefono.getText().trim());
        cl.setDireccion(txtDireccion.getText().trim());
        cl.setIdEmpresa(idEmpresa.getAsInt());

        if (client.RegistrarCliente(cl)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cliente registrado correctamente");

            txtBuscar.setText("");

            listar();

            estadoNuevo();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al registrar cliente");
        }
    }

    // ---------------------------------------------------------
    // EDITAR CLIENTE
    // ---------------------------------------------------------
    private void editar() {

        if (txtId.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione una fila");

            return;
        }

        if (vacio(txtDni)
                || vacio(txtNombre)
                || vacio(txtTelefono)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Los campos no pueden estar vacíos");

            return;
        }

        cl.setDni(txtDni.getText().trim());
        cl.setNombre(txtNombre.getText().trim());
        cl.setTelefono(txtTelefono.getText().trim());
        cl.setDireccion(txtDireccion.getText().trim());

        cl.setId(Integer.parseInt(txtId.getText()));

        cl.setIdEmpresa(idEmpresa.getAsInt());

        if (client.ModificarCliente(cl)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cliente modificado correctamente");

            listar();

            estadoNuevo();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al modificar cliente");
        }
    }

    // ---------------------------------------------------------
    // DESACTIVAR CLIENTE
    // ---------------------------------------------------------
    private void eliminar() {

        if (txtId.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un cliente para desactivar");

            return;
        }

        int id = Integer.parseInt(txtId.getText());

        String motivo = client.motivoNoEliminar(id);

        if (motivo != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se puede desactivar a \""
                    + txtNombre.getText() + "\".\n"
                    + motivo,
                    "Cliente protegido",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de desactivar al cliente "
                + txtNombre.getText() + "?\n\n"
                + "Sus ventas e historial se conservarán.",
                "Confirmar desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        if (client.EliminarCliente(id)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cliente desactivado correctamente.\n"
                    + "Su historial de ventas se ha conservado.");

            listar();

            estadoNuevo();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo desactivar el cliente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------
    // SELECCIONAR FILA
    // ---------------------------------------------------------
    private void seleccionarFila(int fila) {

        if (fila < 0) {
            return;
        }

        btnEditar.setEnabled(true);

        btnEliminar.setEnabled(true);

        btnGuardar.setEnabled(false);

        txtId.setText(
                tabla.getValueAt(fila, 0).toString());

        txtDni.setText(
                tabla.getValueAt(fila, 1).toString());

        txtNombre.setText(
                tabla.getValueAt(fila, 2).toString());

        txtTelefono.setText(
                tabla.getValueAt(fila, 3).toString());

        txtDireccion.setText(
                tabla.getValueAt(fila, 4).toString());
    }

    // ---------------------------------------------------------
    // ESTADO NUEVO
    // ---------------------------------------------------------
    private void estadoNuevo() {

        txtId.setText("");

        txtDni.setText("");

        txtNombre.setText("");

        txtTelefono.setText("");

        txtDireccion.setText("");

        btnGuardar.setEnabled(true);

        btnEditar.setEnabled(false);

        btnEliminar.setEnabled(false);

        tabla.clearSelection();
    }

    // ---------------------------------------------------------
    // VALIDAR CAMPOS VACIOS
    // ---------------------------------------------------------
    private boolean vacio(JTextField f) {

        return f.getText().trim().isEmpty();
    }

}
