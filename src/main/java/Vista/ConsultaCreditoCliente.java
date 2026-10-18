package Vista;

import Estilos.Estilos;
import Modelo.Conexion;
import Modelo.PdfGeneratorCredito;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import Utilidades.TablaBotonEliminarProducto;


public class ConsultaCreditoCliente extends JFrame {
private static int idEmpresaActiva;
private Runnable onCerrarCallback;   // 👈 AGREGAR ESTA LÍNEA
    private JTextField txtBuscar3;
    private JTable tableProductos;
    private DefaultTableModel modeloProductos;
    private JScrollPane scrollProductos;

    private JLabel lblTotalCredito;
    private JTextField txtTotalCredito;
    private JTextField txtRuc;
    private JTextField txtNombre;
    private JLabel lblCliente;

    private JButton btnCobrarCredito;
    private JButton btnAbonar;
    private JButton btnPDF;
    private int idVenta;
    private int idCliente;
    private String dni;  // <-- guardamos el dni para pasarlo a VentanaAbono

    private JTable tableAbonos;
    //datos para el pdf
    private String nombreCliente = "";
private double totalPagarCreditos = 0.0;
    public ConsultaCreditoCliente() {
        setTitle("Consulta Crédito Cliente");
        setSize(1100, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        initComponentes();
        // ========================================================
    // AL CERRAR ESTA VENTANA, AVISAR AL PANEL PADRE
    // ========================================================
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (onCerrarCallback != null) {
                onCerrarCallback.run();
            }
        }
    });
    }
public static void setIdEmpresaActiva(int idEmpresa) {
    idEmpresaActiva = idEmpresa;
}

public static int getIdEmpresaActiva() {
    return idEmpresaActiva;
}
    // Constructor con datos y asignación de ids
public ConsultaCreditoCliente(int ruc, String nombre, int idVenta, int idCliente) {
    this();
    setDato(ruc, nombre);

    try (Connection con = Conexion.getConnection()) {
        String sql = "SELECT id_venta, dni FROM detalle_creditocliente WHERE (dni = ? OR nombre = ?) AND id_empresa = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ruc);
            ps.setString(2, nombre);
            ps.setInt(3, idEmpresaActiva); // <-- usar variable global local a esta clase
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.idVenta = rs.getInt("id_venta");
                    this.dni = rs.getString("dni");
                    this.idCliente = 0;
                } else {
                    this.idVenta = idVenta;
                    this.idCliente = 0;
                    this.dni = null;
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        this.idVenta = idVenta;
        this.idCliente = 0;
        this.dni = null;
    }

    txtBuscar3.setText(String.valueOf(ruc));
    cargarDatos();
}

  private void initComponentes() {
    // Crear y añadir componentes visibles

    JLabel lblBuscar = new JLabel("Buscar:");
    lblBuscar.setBounds(20, 20, 60, 25);
    add(lblBuscar);

    txtBuscar3 = new JTextField();
    txtBuscar3.setBounds(80, 20, 200, 25);
    add(txtBuscar3);

    txtBuscar3.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            cargarDatos();
        }
    });

    // Ocultar los componentes sólo después de crearlos y agregarlos
    lblBuscar.setVisible(false);
    txtBuscar3.setVisible(false);

    JLabel lblCliente = new JLabel("Cliente:");
lblCliente.setBounds(330, 45, 70, 25);
Estilos.estiloEtiqueta2(lblCliente);
add(lblCliente);

txtRuc = new JTextField();
txtRuc.setBounds(400, 45, 150, 25);          // <-- faltaba esto
Estilos.estiloEtiqueta3(txtRuc);
txtRuc.setEditable(false);
txtRuc.setHorizontalAlignment(JTextField.CENTER);
add(txtRuc);

txtNombre = new JTextField();
txtNombre.setBounds(560, 45, 350, 25);
Estilos.estiloEtiqueta3(txtNombre);
txtNombre.setEditable(false);
txtNombre.setHorizontalAlignment(JTextField.CENTER);
add(txtNombre);

    // --- CREAR botón PDF PRIMERO ---
    btnPDF = new JButton("PDF");
    btnPDF.setBounds(960, 8, 80, 25);
    btnPDF.setBackground(new Color(59, 89, 152));
    btnPDF.setForeground(Color.WHITE);
    btnPDF.setFocusPainted(false);
    btnPDF.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12));


    // Listener btnPDF (después de crear el botón)
 btnPDF.addActionListener(e -> {
    
    PdfGeneratorCredito.generarPdfCredito(nombreCliente, totalPagarCreditos, tableProductos.getModel(), this);

});


    add(btnPDF);

   // --- Crear tabla Productos (UNA sola vez, con tooltip) ---
modeloProductos = new DefaultTableModel();
tableProductos = new JTable(modeloProductos) {
    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        if (row >= 0 && col == 2) {
            return String.valueOf(getValueAt(row, col));
        }
        return null;
    }
};
scrollProductos = new JScrollPane(tableProductos);
scrollProductos.setBounds(20, 85, 990, 250);
add(scrollProductos);

modeloProductos.addColumn("ID");
modeloProductos.addColumn("ID Prod");
modeloProductos.addColumn("Nombre");
modeloProductos.addColumn("Cantidad");
modeloProductos.addColumn("Precio");
modeloProductos.addColumn("Total");
modeloProductos.addColumn("Fecha");
modeloProductos.addColumn("Num Ext");
modeloProductos.addColumn("Eliminar");

Estilos.estiloTablas(tableProductos);

// Botón eliminar (después de los estilos)
int columnaEliminar = modeloProductos.getColumnCount() - 1;
tableProductos.getColumnModel().getColumn(columnaEliminar)
        .setCellRenderer(new TablaBotonEliminarProducto.ButtonRenderer());
tableProductos.getColumnModel().getColumn(columnaEliminar)
        .setCellEditor(new TablaBotonEliminarProducto.ButtonEditor(new JCheckBox(), tableProductos, this));
    
  
    lblTotalCredito = new JLabel("Crédito Pendiente:");
    Estilos.estiloEtiqueta(lblTotalCredito);
    lblTotalCredito.setBounds(20, 350, 150, 25);
    add(lblTotalCredito);

    txtTotalCredito = new JTextField("0.00");
    txtTotalCredito.setBounds(170, 350, 150, 25);
    txtTotalCredito.setEditable(false);
    txtTotalCredito.setHorizontalAlignment(JTextField.RIGHT);
    Estilos.estiloEtiqueta3(txtTotalCredito);
    add(txtTotalCredito);

    btnAbonar = new JButton("Abonar");
    btnAbonar.setBounds(630, 350, 150, 30);
    btnAbonar.setBackground(new Color(59, 89, 152));
    btnAbonar.setForeground(Color.WHITE);
    btnAbonar.setFocusPainted(false);
   
btnAbonar.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12));
    btnAbonar.addActionListener(e -> {
        if (dni != null && !dni.isEmpty()) {
            new VentanaAbono(this, tableProductos, txtTotalCredito, idVenta, idCliente, dni).setVisible(true);
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "Debe ingresar un DNI válido para continuar.");
        }
    });

    add(btnAbonar);

    btnCobrarCredito = new JButton("Cobrar Crédito");
    btnCobrarCredito.setBounds(790, 350, 220, 30);
    btnCobrarCredito.setBackground(new Color(0, 153, 76));
    btnCobrarCredito.setForeground(Color.WHITE);
    btnCobrarCredito.setFocusPainted(false);
   
    btnCobrarCredito.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12));
    btnCobrarCredito.addActionListener(e -> cobrarCredito());
    add(btnCobrarCredito);
}

    private void cargarDatos() {
        listarProductos();
        actualizarCreditoPendiente();
    }
private void listarProductos() {
    // Si hay un editor activo (el botón eliminar), cancelarlo antes de vaciar la tabla
    if (tableProductos.isEditing()) {
        tableProductos.getCellEditor().cancelCellEditing();
    }

    // Limpiar tabla antes de listar
    modeloProductos.setRowCount(0);

    if (dni == null || dni.trim().isEmpty()) {
        return; // No hay dni para buscar
    }

    String dniABuscar = dni.trim();
    int idEmpresa = Sistema.getIdEmpresaActiva(); // empresa activa

    String sql = 
    "SELECT id, id_pro, nombre, cantidad, precio, total, fecha, dni, '' AS nota " +
    "FROM detalle_creditocliente WHERE dni = ? AND id_empresa = ? " + 
    "UNION ALL " +
    "SELECT id, 0 AS id_pro, 'ABONO REALIZADO' AS nombre, 1 AS cantidad, monto AS precio, monto AS total, fecha, dni, nota " +
    "FROM abonos_credito WHERE dni = ? AND aplicado = 0 AND id_empresa = ? " +
    "ORDER BY fecha ASC, id ASC";

    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        // Parámetros para detalle_creditocliente
        ps.setString(1, dniABuscar);
        ps.setInt(2, idEmpresa);

        // Parámetros para abonos_credito
        ps.setString(3, dniABuscar);
        ps.setInt(4, idEmpresa);

        try (ResultSet rs = ps.executeQuery()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (rs.next()) {
                Object[] fila = new Object[9];
                fila[0] = rs.getInt("id");
                fila[1] = rs.getInt("id_pro");

                String nombreFila = rs.getString("nombre");
                String nota = rs.getString("nota");
                if ("ABONO REALIZADO".equals(nombreFila) && nota != null && !nota.trim().isEmpty()) {
                    nombreFila += " - " + nota.trim();   // ABONO REALIZADO - abono domingo
                }
                fila[2] = nombreFila;

                fila[3] = rs.getDouble("cantidad");
                fila[4] = rs.getDouble("precio");
                fila[5] = rs.getDouble("total");

                // Manejo de fecha
                Timestamp fechaBD = rs.getTimestamp("fecha");
                fila[6] = (fechaBD != null) ? fechaBD.toLocalDateTime().format(formatter) : "";

                fila[7] = rs.getString("dni");

                modeloProductos.addRow(fila);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
  private void actualizarCreditoPendiente() {
    double totalProductos = 0;
    double totalAbonos = 0;

    for (int i = 0; i < modeloProductos.getRowCount(); i++) {
        String nombre = modeloProductos.getValueAt(i, 2).toString();
        Object val = modeloProductos.getValueAt(i, 5);

        if (val != null) {
            try {
                double monto = Double.parseDouble(val.toString());

                if (nombre.toUpperCase().startsWith("ABONO REALIZADO")) {
             totalAbonos += monto;
        } else {
        totalProductos += monto;
        }
            } catch (NumberFormatException ignored) {}
        }
    }

    double pendiente = totalProductos - totalAbonos;
    this.totalPagarCreditos = pendiente;  // <--- Aquí actualizas el atributo
    txtTotalCredito.setText(String.format("%.2f", pendiente));
}

 private void cobrarCredito() {
    if (modeloProductos.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No hay créditos para cobrar.");
        return;
    }

    double creditoPendiente;

    try {
        creditoPendiente = Double.parseDouble(txtTotalCredito.getText());
    } catch (NumberFormatException e) {
        creditoPendiente = 0;
    }

    if (creditoPendiente <= 0) {
        JOptionPane.showMessageDialog(this, "No hay saldo pendiente para cobrar.");
        return;
    }

    // ==========================================================
    // DATOS DEL CLIENTE Y EMPRESA QUE SE ESTÁN COBRANDO
    // ==========================================================

    String dniCliente = this.dni;
    int empresa = Sistema.getIdEmpresaActiva();

    if (dniCliente == null || dniCliente.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "No se encontró el DNI del cliente.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    if (empresa <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "No se encontró una empresa activa válida.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    System.out.println("========================================");
    System.out.println("🟣 ABRIENDO COBRO DE CRÉDITO");
    System.out.println("DNI cliente: " + dniCliente);
    System.out.println("Empresa activa: " + empresa);
    System.out.println("Cliente: " + nombreCliente);
    System.out.println("Crédito pendiente: " + creditoPendiente);
    System.out.println("========================================");

    // ==========================================================
    // PASAMOS DNI + EMPRESA A ventanaCobrar
    // ==========================================================

    ventanaCobrar cobroDialog = new ventanaCobrar(
            this,
            true,
            tableProductos,
            dniCliente,
            empresa,
            this
    );

    cobroDialog.setTotal(creditoPendiente);
    cobroDialog.setLocationRelativeTo(this);
    cobroDialog.setVisible(true);

    cargarDatos();
}
   public void setDato(int ruc, String nombre) {
    txtRuc.setText(String.valueOf(ruc));
    txtNombre.setText(nombre);
    this.nombreCliente = nombre;
}

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    // Métodos públicos para que VentanaAbono pueda refrescar datos
    public void listar() {
        cargarDatos();
    }

    public void actualizarTotalDesdeTabla() {
        actualizarCreditoPendiente();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConsultaCreditoCliente ventana = new ConsultaCreditoCliente();
            ventana.setVisible(true);
        });
    }
    // En ConsultaCreditoCliente.java
     public void setOnCerrarCallback(Runnable callback) {
    this.onCerrarCallback = callback;
}
public void limpiarCampos() {
    txtRuc.setText("");
    txtNombre.setText("");
    txtTotalCredito.setText("0.00");
    modeloProductos.setRowCount(0); // Vacía la tabla también
}
}
