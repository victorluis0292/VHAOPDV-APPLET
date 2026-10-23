package Controlador;

import Modelo.Cliente;
import Modelo.ClienteDao;
import Modelo.Detalle;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.VentaDao;
import Vista.ConsultaCreditoCliente;
import Vista.CreditoClientePanel;
import Vista.FrmBusqueda2;
import Vista.Sistema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controlador del módulo "Crédito Cliente".
 *
 * Toda la lógica del módulo Crédito Cliente vive aquí.
 *
 * IMPORTANTE:
 * - La tabla utilizada es la del CreditoClientePanel.
 * - Los productos provenientes de FrmBusqueda2/VentanaCantidadBusqueda
 *   se agregan mediante agregarFila().
 * - cantidad se maneja como double para permitir cantidades decimales.
 */
public class CreditoClienteController {

    private final CreditoClientePanel panel;

    private final ClienteDao clienteDao = new ClienteDao();
    private final ProductosDao productoDao = new ProductosDao();
    private final VentaDao ventaDao = new VentaDao();

    private final DecimalFormat dfCantidad =
            new DecimalFormat("0.###");

    private int idClienteActual = 0;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CreditoClienteController(CreditoClientePanel panel) {

        this.panel = panel;

        inicializarListeners();
    }


    // ============================================================
    // LISTENERS
    // ============================================================

    private void inicializarListeners() {

        // --------------------------------------------------------
        // BUSCAR CLIENTE ESCRIBIENDO ID / DNI
        // --------------------------------------------------------

        panel.getTxtRuc().addActionListener(
                e -> buscarCliente()
        );


        // --------------------------------------------------------
        // BUSCAR CLIENTE CON BOTÓN ...
        // --------------------------------------------------------

        panel.getBtnBuscarCliente().addActionListener(
                e -> abrirBusquedaClientes()
        );


        // --------------------------------------------------------
        // BUSCAR / AGREGAR PRODUCTO POR CÓDIGO
        // --------------------------------------------------------

        panel.getTxtCodigo().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent evt) {

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

                    evt.consume();

                    agregarProductoPorCodigo();
                }
            }
        });


        // --------------------------------------------------------
        // BOTÓN BUSCAR PRODUCTO
        // --------------------------------------------------------

        panel.getBtnBuscarProducto().addActionListener(
                e -> abrirBusquedaProductos()
        );


        // --------------------------------------------------------
        // ELIMINAR PRODUCTO
        // --------------------------------------------------------

        panel.getBtnEliminarProducto().addActionListener(
                e -> eliminarFilaSeleccionada()
        );


        // --------------------------------------------------------
        // VER HISTORIAL
        // --------------------------------------------------------

        panel.getBtnVerHistorial().addActionListener(
                e -> verHistorial()
        );


        // --------------------------------------------------------
        // ACEPTAR / GENERAR CRÉDITO
        // --------------------------------------------------------

        panel.getBtnAceptar().addActionListener(
                e -> generarVentaCredito()
        );


        // --------------------------------------------------------
        // ACTUALIZAR TOTAL AUTOMÁTICAMENTE
        // --------------------------------------------------------

        panel.getModeloProductos().addTableModelListener(
                e -> recalcularTotal()
        );
    }


    // ============================================================
    // BUSCAR PRODUCTOS
    // ============================================================

    private void abrirBusquedaProductos() {

        int idEmpresa =
                Sistema.getIdEmpresaActiva();

        System.out.println("======================================");
        System.out.println("ABRIENDO BUSQUEDA DE PRODUCTOS");
        System.out.println("Empresa activa: " + idEmpresa);
        System.out.println("Origen: credito");
        System.out.println("======================================");


        /*
         * MUY IMPORTANTE:
         *
         * Mandamos explícitamente "credito".
         *
         * FrmBusqueda2 posteriormente manda este origen
         * a VentanaCantidadBusqueda.
         */

        FrmBusqueda2 frm =
                new FrmBusqueda2(
                        idEmpresa,
                        "credito"
                );

        frm.setLocationRelativeTo(panel);

        frm.setVisible(true);
    }


    // ============================================================
    // BUSCAR CLIENTE POR ID / DNI
    // ============================================================

    private void buscarCliente() {

        String texto =
                panel.getTxtRuc()
                        .getText()
                        .trim();


        if (texto.isEmpty()) {
            return;
        }


        int dni;

        try {

            dni = Integer.parseInt(texto);

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    panel,
                    "DNI inválido"
            );

            return;
        }


        int idEmpresa =
                Sistema.getIdEmpresaActiva();


        Cliente cl =
                clienteDao.BuscarCliente(
                        dni,
                        idEmpresa
                );


        if (cl != null &&
                cl.getNombre() != null &&
                !cl.getNombre().trim().isEmpty()) {

            panel.getTxtNombreCliente()
                    .setText(cl.getNombre());


            // Guardamos el ID interno del cliente
            idClienteActual = cl.getId();


            panel.getTxtCodigo()
                    .requestFocus();

        } else {

            JOptionPane.showMessageDialog(
                    panel,
                    "El cliente no existe en esta empresa"
            );

            limpiarCliente();
        }
    }


    // ============================================================
    // ABRIR BÚSQUEDA DE CLIENTES
    // ============================================================

 private void abrirBusquedaClientes() {

    int idEmpresa = Sistema.getIdEmpresaActiva();

    JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(panel),
            "Buscar Cliente",
            Dialog.ModalityType.APPLICATION_MODAL
    );

    dialog.setSize(700, 500);
    dialog.setLocationRelativeTo(panel);
    dialog.setLayout(null);

    // ========================================================
    // LABEL BUSCAR
    // ========================================================

    JLabel lblBuscar = new JLabel("Buscar:");
    lblBuscar.setFont(
            new java.awt.Font(
                    "Tahoma",
                    java.awt.Font.BOLD,
                    14
            )
    );
    lblBuscar.setBounds(20, 20, 60, 30);
    dialog.add(lblBuscar);


    // ========================================================
    // CAMPO BUSCAR
    // ========================================================

    JTextField txtBuscar = new JTextField();

    txtBuscar.setFont(
            new java.awt.Font(
                    "Tahoma",
                    java.awt.Font.PLAIN,
                    16
            )
    );

    txtBuscar.setBounds(
            83,
            20,
            400,
            30
    );

    dialog.add(txtBuscar);


    // ========================================================
    // BOTÓN BUSCAR
    // ========================================================

    JButton btnBuscar = new JButton("Buscar");

    btnBuscar.setBounds(
            493,
            20,
            100,
            30
    );

    dialog.add(btnBuscar);


    // ========================================================
    // TABLA
    //
    // NOTA: el modelo SIEMPRE conserva la columna "ID" en el
    // índice 0, aunque no se muestre en pantalla. Esto es
    // necesario porque seleccionarCliente() sigue leyendo el
    // ID desde el modelo (no desde la vista de la tabla).
    // ========================================================

    DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[][]{},
                    new String[]{
                            "ID",
                            "DNI",
                            "Nombre",
                            "Teléfono"
                    }
            ) {

                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }
            };


    JTable tabla = new JTable(modelo);

    tabla.setRowHeight(25);

    tabla.setFont(
            new java.awt.Font(
                    "Tahoma",
                    java.awt.Font.PLAIN,
                    14
            )
    );

    tabla.getTableHeader()
            .setReorderingAllowed(false);

    tabla.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
    );


    // ========================================================
    // OCULTAR COLUMNA ID
    //
    // Se remueve solo de la vista (columnModel), el modelo
    // (DefaultTableModel) sigue teniendo la columna ID en el
    // índice 0 internamente.
    // ========================================================

    tabla.getColumnModel()
            .removeColumn(
                    tabla.getColumnModel().getColumn(0)
            );


    // ========================================================
    // ENTER EN LA FILA -> ACEPTAR
    // ========================================================

    tabla.addKeyListener(
            new java.awt.event.KeyAdapter() {

                @Override
                public void keyPressed(
                        java.awt.event.KeyEvent e
                ) {

                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                            && tabla.getSelectedRow() != -1) {

                        e.consume();

                        seleccionarCliente(
                                tabla,
                                dialog
                        );
                    }
                }
            }
    );


    // ========================================================
    // FLECHA ABAJO DESDE EL BUSCADOR -> BAJA A LA TABLA
    // ========================================================

    txtBuscar.addKeyListener(
            new java.awt.event.KeyAdapter() {

                @Override
                public void keyPressed(
                        java.awt.event.KeyEvent e
                ) {

                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN
                            && tabla.getRowCount() > 0) {

                        e.consume();

                        tabla.requestFocusInWindow();

                        int fila =
                                tabla.getSelectedRow();

                        if (fila == -1) {
                            fila = 0;
                        }

                        tabla.setRowSelectionInterval(
                                fila,
                                fila
                        );

                        tabla.scrollRectToVisible(
                                tabla.getCellRect(
                                        fila,
                                        0,
                                        true
                                )
                        );
                    }
                }
            }
    );


    // ========================================================
    // SCROLL
    // ========================================================

    JScrollPane scroll =
            new JScrollPane(tabla);

    scroll.setBounds(
            20,
            70,
            640,
            320
    );

    dialog.add(scroll);


    // ========================================================
    // BOTÓN ACEPTAR
    // ========================================================

    JButton btnAceptar =
            new JButton("ACEPTAR");

    btnAceptar.setBounds(
            400,
            405,
            120,
            35
    );

    dialog.add(btnAceptar);


    // ========================================================
    // BOTÓN CANCELAR
    // ========================================================

    JButton btnCancelar =
            new JButton("CANCELAR");

    btnCancelar.setBounds(
            530,
            405,
            120,
            35
    );

    dialog.add(btnCancelar);


    // ========================================================
    // MÉTODO PARA CARGAR CLIENTES
    // ========================================================

    Runnable cargarClientes = () -> {

        String texto =
                txtBuscar.getText()
                        .trim();


        modelo.setRowCount(0);


        List<Cliente> clientes =
                clienteDao.BuscarClientes(
                        texto,
                        idEmpresa
                );


        for (Cliente cl : clientes) {

            modelo.addRow(
                    new Object[]{
                            cl.getId(),
                            cl.getDni(),
                            cl.getNombre(),
                            cl.getTelefono()
                    }
            );
        }
    };


    // ========================================================
    // BOTÓN BUSCAR
    // ========================================================

    btnBuscar.addActionListener(
            e -> cargarClientes.run()
    );


    // ========================================================
    // BUSCAR CON ENTER
    // ========================================================

    txtBuscar.addActionListener(
            e -> cargarClientes.run()
    );


    // ========================================================
    // FILTRAR AUTOMÁTICAMENTE AL ESCRIBIR
    // ========================================================

    txtBuscar.getDocument()
            .addDocumentListener(
                    new javax.swing.event.DocumentListener() {

                        @Override
                        public void insertUpdate(
                                javax.swing.event.DocumentEvent e
                        ) {
                            cargarClientes.run();
                        }


                        @Override
                        public void removeUpdate(
                                javax.swing.event.DocumentEvent e
                        ) {
                            cargarClientes.run();
                        }


                        @Override
                        public void changedUpdate(
                                javax.swing.event.DocumentEvent e
                        ) {
                            cargarClientes.run();
                        }
                    }
            );


    // ========================================================
    // DOBLE CLICK PARA SELECCIONAR
    // ========================================================

    tabla.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e
                ) {

                    if (e.getClickCount() == 2 &&
                            tabla.getSelectedRow() != -1) {

                        seleccionarCliente(
                                tabla,
                                dialog
                        );
                    }
                }
            }
    );


    // ========================================================
    // ACEPTAR
    // ========================================================

    btnAceptar.addActionListener(e -> {

        seleccionarCliente(
                tabla,
                dialog
        );
    });


    // ========================================================
    // CANCELAR
    // ========================================================

    btnCancelar.addActionListener(
            e -> dialog.dispose()
    );


    // ========================================================
    // CARGAR TODOS LOS CLIENTES AL ABRIR
    // ========================================================

    cargarClientes.run();


    // ========================================================
    // SELECCIONAR AUTOMÁTICAMENTE EL PRIMER CLIENTE
    // ========================================================

    if (tabla.getRowCount() > 0) {
        tabla.setRowSelectionInterval(0, 0);
    }


    // ========================================================
    // ENFOCAR BUSCADOR
    // ========================================================

    txtBuscar.requestFocus();


    // ========================================================
    // MOSTRAR
    // ========================================================

    dialog.setVisible(true);
}

    // ============================================================
    // SELECCIONAR CLIENTE
    // ============================================================

    private void seleccionarCliente(
            JTable tabla,
            JDialog dialog
    ) {

        int fila =
                tabla.getSelectedRow();


        // --------------------------------------------------------
        // VALIDAR SELECCIÓN
        // --------------------------------------------------------

        if (fila == -1) {

            JOptionPane.showMessageDialog(
                    dialog,
                    "Selecciona un cliente"
            );

            return;
        }


        // --------------------------------------------------------
        // LEER DESDE EL MODELO (no desde la vista)
        //
        // IMPORTANTE:
        // Como la columna "ID" fue removida de la vista
        // (columnModel), tabla.getValueAt() ya NO corresponde
        // a los mismos índices del modelo. Por eso leemos
        // directamente del TableModel, donde ID sigue siendo
        // la columna 0.
        //
        // Como no hay row sorter, el índice de fila de la vista
        // coincide con el índice de fila del modelo.
        // --------------------------------------------------------

        TableModel modelo =
                tabla.getModel();


        // --------------------------------------------------------
        // OBTENER ID INTERNO
        // --------------------------------------------------------

        int id =
                Integer.parseInt(
                        modelo.getValueAt(
                                fila,
                                0
                        ).toString()
                );


        // --------------------------------------------------------
        // OBTENER DNI
        // --------------------------------------------------------

        String dni =
                modelo.getValueAt(
                        fila,
                        1
                ).toString();


        // --------------------------------------------------------
        // OBTENER NOMBRE
        // --------------------------------------------------------

        String nombre =
                modelo.getValueAt(
                        fila,
                        2
                ).toString();


        // --------------------------------------------------------
        // GUARDAR ID INTERNO
        // --------------------------------------------------------

        idClienteActual = id;


        // --------------------------------------------------------
        // ACTUALIZAR CAMPO ID/DNI
        //
        // IMPORTANTE:
        // Tu sistema actual utiliza este campo como DNI.
        // --------------------------------------------------------

        panel.getTxtRuc()
                .setText(dni);


        // --------------------------------------------------------
        // ACTUALIZAR NOMBRE
        // --------------------------------------------------------

        panel.getTxtNombreCliente()
                .setText(nombre);


        // --------------------------------------------------------
        // CERRAR VENTANA
        // --------------------------------------------------------

        dialog.dispose();


        // --------------------------------------------------------
        // IR A PRODUCTO
        // --------------------------------------------------------

        panel.getTxtCodigo()
                .requestFocus();
    }


    // ============================================================
    // AGREGAR PRODUCTO ESCRIBIENDO CÓDIGO
    // ============================================================

    private void agregarProductoPorCodigo() {

        String texto =
                panel.getTxtCodigo()
                        .getText()
                        .trim();


        if (texto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    panel,
                    "Ingrese el código o ID del producto"
            );

            panel.getTxtCodigo()
                    .requestFocus();

            return;
        }


        int idEmpresa =
                Sistema.getIdEmpresaActiva();


        Productos pro =
                productoDao.BuscarPro(
                        texto,
                        idEmpresa
                );


        // --------------------------------------------------------
        // SI NO ENCONTRÓ POR CÓDIGO,
        // INTENTAR POR ID
        // --------------------------------------------------------

        if (pro == null ||
                pro.getNombre() == null) {

            try {

                int id =
                        Integer.parseInt(texto);


                pro =
                        productoDao.BuscarId(
                                id,
                                idEmpresa
                        );

            } catch (NumberFormatException ignored) {

                // No era un ID numérico.
            }
        }


        // --------------------------------------------------------
        // PRODUCTO NO ENCONTRADO
        // --------------------------------------------------------

        if (pro == null ||
                pro.getNombre() == null) {

            JOptionPane.showMessageDialog(
                    panel,
                    "El código o ID del producto no existe"
            );

            limpiarCodigo();

            return;
        }


        // --------------------------------------------------------
        // CANTIDAD POR DEFECTO
        // --------------------------------------------------------

        double cantidad = 1.0;

        double stock =
                pro.getStock();


        if (stock < cantidad) {

            JOptionPane.showMessageDialog(
                    panel,
                    "Stock no disponible"
            );

            limpiarCodigo();

            return;
        }


        // --------------------------------------------------------
        // CALCULAR TOTAL
        // --------------------------------------------------------

        double total =
                cantidad *
                        pro.getPrecio();


        // --------------------------------------------------------
        // AGREGAR A TABLA
        // --------------------------------------------------------

        agregarFila(
                pro.getId(),
                pro.getNombre(),
                cantidad,
                pro.getPrecio(),
                total
        );


        limpiarCodigo();
    }


    // ============================================================
    // AGREGAR FILA A TABLA
    // ============================================================

    /**
     * Este método es PÚBLICO porque también lo utiliza
     * VentanaCantidadBusqueda cuando el producto viene
     * desde la búsqueda.
     */
    public void agregarFila(
            int idProducto,
            String nombre,
            double cantidad,
            double precio,
            double total
    ) {

        System.out.println("======================================");
        System.out.println("AGREGANDO PRODUCTO A CRÉDITO");
        System.out.println("ID: " + idProducto);
        System.out.println("Descripción: " + nombre);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Precio: " + precio);
        System.out.println("Total: " + total);
        System.out.println("======================================");


        Object[] fila =
                new Object[]{
                        idProducto,
                        nombre,
                        cantidad,
                        precio,
                        total
                };


        panel.getModeloProductos()
                .addRow(fila);


        recalcularTotal();


        panel.getTablaProductos()
                .revalidate();

        panel.getTablaProductos()
                .repaint();
    }


    // ============================================================
    // AGREGAR FILAS DESDE NUEVA VENTA
    // ============================================================

    /**
     * Recibe productos provenientes de Nueva Venta
     * y los pasa a Crédito Cliente.
     */
    public void agregarFilasDesdeVenta(
            Object[][] filasVenta
    ) {

        if (filasVenta == null) {
            return;
        }


        for (Object[] fila : filasVenta) {

            if (fila == null ||
                    fila.length < 5) {

                continue;
            }


            try {

                int idProducto =
                        Integer.parseInt(
                                fila[0].toString()
                        );


                String nombre =
                        fila[1].toString();


                double cantidad =
                        Double.parseDouble(
                                fila[2].toString()
                        );


                double precio =
                        Double.parseDouble(
                                fila[3].toString()
                        );


                double total =
                        Double.parseDouble(
                                fila[4].toString()
                        );


                agregarFila(
                        idProducto,
                        nombre,
                        cantidad,
                        precio,
                        total
                );


            } catch (Exception ex) {

                System.out.println(
                        "Error al transferir producto desde venta: "
                                + ex.getMessage()
                );
            }
        }
    }


    // ============================================================
    // ELIMINAR PRODUCTO
    // ============================================================

    private void eliminarFilaSeleccionada() {

        int fila =
                panel.getTablaProductos()
                        .getSelectedRow();


        if (fila == -1) {

            JOptionPane.showMessageDialog(
                    panel,
                    "Seleccione una fila para eliminar"
            );

            return;
        }


        panel.getModeloProductos()
                .removeRow(fila);


        recalcularTotal();


        panel.getTxtCodigo()
                .requestFocus();
    }


    // ============================================================
    // CALCULAR TOTAL
    // ============================================================

    private void recalcularTotal() {

        DefaultTableModel modelo =
                panel.getModeloProductos();


        double total = 0.0;


        for (int i = 0;
             i < modelo.getRowCount();
             i++) {

            Object valor =
                    modelo.getValueAt(i, 4);


            if (valor == null) {
                continue;
            }


            try {

                total +=
                        Double.parseDouble(
                                valor.toString()
                        );

            } catch (NumberFormatException ex) {

                System.out.println(
                        "Valor de total inválido en fila "
                                + i
                                + ": "
                                + valor
                );
            }
        }


        panel.getLblTotalValor()
                .setText(
                        String.format(
                                "%.2f",
                                total
                        )
                );
    }


    // ============================================================
    // VER HISTORIAL
    // ============================================================

    private void verHistorial() {

        String rucTexto =
                panel.getTxtRuc()
                        .getText()
                        .trim();


        String nombre =
                panel.getTxtNombreCliente()
                        .getText()
                        .trim();


        if (rucTexto.isEmpty() ||
                nombre.isEmpty()) {

            JOptionPane.showMessageDialog(
                    panel,
                    "Ingresa Número de Cliente + ENTER"
            );

            panel.getTxtRuc()
                    .requestFocus();

            return;
        }


        int ruc;

        try {

            ruc =
                    Integer.parseInt(
                            rucTexto
                    );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    panel,
                    "DNI inválido"
            );

            return;
        }


        ConsultaCreditoCliente
                .setIdEmpresaActiva(
                        Sistema.getIdEmpresaActiva()
                );


        ConsultaCreditoCliente consulta =
                new ConsultaCreditoCliente(
                        ruc,
                        nombre,
                        0,
                        0
                );

        consulta.setOnCerrarCallback(this::limpiarCliente);   // 👈 AGREGAR ESTA LÍNEA

        consulta.setVisible(true);
    }
    
    // ============================================================
// VER HISTORIAL DE UN CLIENTE (usado por los banners de fiado)
// ============================================================

/**
 * Abre la misma ventana "Consulta Crédito Cliente" que el botón
 * Ver Historial, pero con el cliente que viene del banner.
 *
 * No toca los campos del panel (txtRuc / txtNombreCliente) ni
 * limpia nada al cerrar, para no pisar una captura que el usuario
 * tenga a medias en la pestaña de Crédito Cliente.
 */
public void verHistorialDe(int dni, String nombre) {

    if (nombre == null || nombre.trim().isEmpty()) {
        return;
    }

    ConsultaCreditoCliente
            .setIdEmpresaActiva(
                    Sistema.getIdEmpresaActiva()
            );

    ConsultaCreditoCliente consulta =
            new ConsultaCreditoCliente(
                    dni,
                    nombre.trim(),
                    0,
                    0
            );

    consulta.setVisible(true);
}

    // ============================================================
    // GENERAR VENTA A CRÉDITO
    // ============================================================

    private void generarVentaCredito() {

        DefaultTableModel modelo =
                panel.getModeloProductos();


        // --------------------------------------------------------
        // VALIDAR PRODUCTOS
        // --------------------------------------------------------

        if (modelo.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                    panel,
                    "No hay productos en la venta"
            );

            panel.getTxtCodigo()
                    .requestFocus();

            return;
        }


        // --------------------------------------------------------
        // VALIDAR CLIENTE
        // --------------------------------------------------------

        String nombreCliente =
                panel.getTxtNombreCliente()
                        .getText()
                        .trim();


        String rucTexto =
                panel.getTxtRuc()
                        .getText()
                        .trim();


        if (nombreCliente.isEmpty()) {

            JOptionPane.showMessageDialog(
                    panel,
                    "Debes buscar un cliente"
            );

            panel.getTxtRuc()
                    .requestFocus();

            return;
        }


        int dni;

        try {

            dni =
                    Integer.parseInt(
                            rucTexto
                    );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    panel,
                    "DNI de cliente inválido"
            );

            return;
        }


        // --------------------------------------------------------
        // GENERAR ID DE VENTA
        // --------------------------------------------------------

        int idVenta =
                ventaDao.IdVenta();


        String fechaActual =
                new SimpleDateFormat(
                        "dd/MM/yyyy"
                ).format(new Date());


        // --------------------------------------------------------
        // REGISTRAR PRODUCTOS
        // --------------------------------------------------------

        for (int i = 0;
             i < modelo.getRowCount();
             i++) {

            try {

                int idProducto =
                        Integer.parseInt(
                                modelo.getValueAt(
                                        i,
                                        0
                                ).toString()
                        );


                String nombreProducto =
                        modelo.getValueAt(
                                i,
                                1
                        ).toString();


                double cantidad =
                        Double.parseDouble(
                                modelo.getValueAt(
                                        i,
                                        2
                                ).toString()
                        );


                double precio =
                        Double.parseDouble(
                                modelo.getValueAt(
                                        i,
                                        3
                                ).toString()
                        );


                double total =
                        Double.parseDouble(
                                modelo.getValueAt(
                                        i,
                                        4
                                ).toString()
                        );


                // ------------------------------------------------
                // CREAR DETALLE
                // ------------------------------------------------

                Detalle detalle =
                        new Detalle();


                detalle.setId_pro(
                        idProducto
                );

                detalle.setNombre(
                        nombreProducto
                );

                detalle.setCantidad(
                        cantidad
                );

                detalle.setPrecio(
                        precio
                );

                detalle.setTotal(
                        total
                );

                detalle.setId(
                        idVenta
                );

                detalle.setCliente(
                        nombreCliente
                );

                detalle.setDni(
                        dni
                );

                detalle.setFecha(
                        fechaActual
                );


                // ------------------------------------------------
                // REGISTRAR CRÉDITO
                // ------------------------------------------------

                ventaDao.RegistrarDetalleCreditocliente(
                        detalle
                );


                // ------------------------------------------------
                // ACTUALIZAR STOCK
                // ------------------------------------------------

                actualizarStock(
                        idProducto,
                        cantidad
                );


            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Error al registrar producto: "
                                + ex.getMessage()
                );

                ex.printStackTrace();

                return;
            }
        }


        // --------------------------------------------------------
        // LIMPIAR
        // --------------------------------------------------------

        limpiarTodo();


        JOptionPane.showMessageDialog(
                panel,
                "Registro exitoso"
        );
    }


    // ============================================================
    // ACTUALIZAR STOCK
    // ============================================================

    private void actualizarStock(
            int idProducto,
            double cantidad
    ) {

        int idEmpresa =
                Sistema.getIdEmpresaActiva();


        Productos pro =
                productoDao.BuscarId(
                        idProducto,
                        idEmpresa
                );


        if (pro != null) {

            boolean actualizado =
                    ventaDao.ActualizarStock(
                            cantidad,
                            idProducto,
                            idEmpresa
                    );


            if (!actualizado) {

                System.out.println(
                        "❌ No se pudo actualizar stock. "
                                + "Producto ID="
                                + idProducto
                );
            }

        } else {

            System.out.println(
                    "❌ Producto ID="
                            + idProducto
                            + " no pertenece a la empresa activa."
            );
        }
    }


    // ============================================================
    // LIMPIAR CÓDIGO
    // ============================================================

    private void limpiarCodigo() {

        panel.getTxtCodigo()
                .setText("");

        panel.getTxtCodigo()
                .requestFocus();
    }


    // ============================================================
    // LIMPIAR CLIENTE
    // ============================================================

   public void limpiarCliente() {

    panel.getTxtRuc().setText("");
    panel.getTxtNombreCliente().setText("");

    idClienteActual = 0;

    panel.getTxtRuc().requestFocus();
}


    // ============================================================
    // LIMPIAR TODO
    // ============================================================

    private void limpiarTodo() {

        panel.getModeloProductos()
                .setRowCount(0);


        panel.getTxtNombreCliente()
                .setText("");


        panel.getTxtRuc()
                .setText("");


        panel.getTxtCodigo()
                .setText("");


        panel.getLblTotalValor()
                .setText("-----");


        idClienteActual = 0;


        panel.getTxtRuc()
                .requestFocus();
    }
}