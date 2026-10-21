package Vista;

import Controlador.TurnoController;
import Estilos.Estilos;
import Modelo.AbonoDao;
import Modelo.AbonoProductoDao;
import Modelo.AbrirCajaEfectivo;
import Modelo.Detalle;
import Modelo.Eventos;
import Modelo.ImprimirTicket;
import Modelo.LoaderDialog;
import Modelo.TurnoModel;
import Modelo.Venta;
import Modelo.VentaDao;
import Servicios.CobroService;
import static Vista.Sistema.txtCodigoVenta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public final class ventanaCobrar extends JDialog {
    // Creamos la fecha forzada a la zona horaria del Centro de México (GMT-6)
    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT-6"));
    Date fechaLocal = cal.getTime();

    private int idEmpresaActiva;
    private String dniCliente;

    private boolean cobroRealizado = false;
    private JTable TableConsultaCreditCliente;

    private double totalPagar = 0.00;
    private double totalPagado = 0.00;
    private double totalComision = 0.00;
    private double ultimoPagoEfectivo = 0.00;

    private JLabel lblTextoTotal = new JLabel("Total a Pagar:");
    private JLabel lblTotal = new JLabel("0.00");
    private JTextField txtPaga = new JTextField();
    private JLabel lblCambio = new JLabel("0.00");
    private DefaultTableModel modeloPagos = new DefaultTableModel(new String[]{"Método", "Monto (Comisión)"}, 0);
    private JTable tablaPagos = new JTable(modeloPagos);

    // Flag para indicar si esta venta es a crédito
    private boolean esVentaCredito = false;
    private ConsultaCreditoCliente ventanaPadreConsulta;

    // ---------- MODO ABONO A PRODUCTO ----------
    private boolean modoAbono = false;
    private int abonoIdDetalle;
    private String abonoNota = "";
    private Runnable abonoAlTerminar;
    private String abonoProducto = "";
    private double abonoPrecioProducto = 0.0;
    private double abonoAbonadoAntes = 0.0;
    private List<double[]> abonoPartes = null;   // cada elemento: {idDetalle, monto}; null = abono a un solo producto

    /**
     * Activa el modo "cobro de abono". En este modo el cobro registra una venta
     * por el monto abonado y guarda el abono ligado a esa venta y a su producto.
     */
    public void configurarAbono(String producto, String dni, int idEmpresa,
                                int idDetalle, String nota, Runnable alTerminar) {
        this.modoAbono = true;
        this.dniCliente = dni;
        this.idEmpresaActiva = idEmpresa;
        this.abonoIdDetalle = idDetalle;
        this.abonoNota = (nota == null) ? "" : nota;
        this.abonoAlTerminar = alTerminar;
        this.abonoProducto = producto;
        setTitle("Cobro de abono - " + producto);
        lblTextoTotal.setText("Abono a cobrar:");
    }
    public void configurarAbonoCategoria(String categoria, String dni, int idEmpresa,
                                     List<double[]> partes, String nota, Runnable alTerminar) {
    this.modoAbono = true;
    this.dniCliente = dni;
    this.idEmpresaActiva = idEmpresa;
    this.abonoPartes = partes;
    this.abonoIdDetalle = 0;
    this.abonoNota = (nota == null) ? "" : nota;
    this.abonoAlTerminar = alTerminar;
    this.abonoProducto = "Categoría " + categoria;
    setTitle("Cobro de abono - Categoría " + categoria);
    lblTextoTotal.setText("Abono a cobrar:");
}
    public void setDetalleAbonoTicket(double precioProducto, double abonadoAntes) {
        this.abonoPrecioProducto = precioProducto;
        this.abonoAbonadoAntes = abonadoAntes;
    }
    // Constructor para ventas normales (efectivo/tarjeta/mixto)
    public ventanaCobrar(JFrame parent) {
        super(parent, "Pago", true); // modal
        initComponentes();
    }

    public void setIdEmpresaActiva(int idEmpresa) {
        this.idEmpresaActiva = idEmpresa;
    }

    // Constructor para indicar si es venta crédito
    public ventanaCobrar(
            JFrame parent,
            boolean esCredito,
            JTable TableConsultaCreditCliente,
            String dniCliente,
            int idEmpresaActiva,
            ConsultaCreditoCliente ventanaPadre) {

        super(parent, "Pago", true);

        this.esVentaCredito = esCredito;
        this.TableConsultaCreditCliente = TableConsultaCreditCliente;
        this.dniCliente = dniCliente;
        this.idEmpresaActiva = idEmpresaActiva;
        this.ventanaPadreConsulta = ventanaPadre;

        System.out.println("========================================");
        System.out.println("🟣 [LOG] Constructor crédito");
        System.out.println("🟣 DNI cliente = " + this.dniCliente);
        System.out.println("🟣 Empresa activa = " + this.idEmpresaActiva);
        System.out.println("========================================");

        initComponentes();
    }

    private void initComponentes() {
        setTitle("Pago");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));
        Estilos.PanelConEstilo panel = new Estilos.PanelConEstilo();
        panel.setLayout(new GridLayout(5, 2, 10, 10));

        panel.add(lblTextoTotal);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(lblTotal);

        panel.add(new JLabel("Paga Con (Efectivo):"));
        txtPaga.setFont(new Font("Arial", Font.PLAIN, 24));
        panel.add(txtPaga);
        new Eventos().aplicarSoloDecimal(txtPaga);

        panel.add(new JLabel("Su cambio:"));
        lblCambio.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(lblCambio);

        JButton btnCobrar = new JButton("Cobrar Efectivo");
        btnCobrar.addActionListener(e -> cobrarEfectivo());
        panel.add(btnCobrar);

        JButton btnTarjeta = new JButton("Cobro con Tarjeta");
        btnTarjeta.addActionListener(e -> {
            ventanaCobroConTarjeta tarjeta = new ventanaCobroConTarjeta(this);
            tarjeta.setVisible(true);
        });
        panel.add(btnTarjeta);

        add(panel, BorderLayout.CENTER);
        tablaPagos.setFillsViewportHeight(true);
        add(new JScrollPane(tablaPagos), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getParent());

        txtPaga.addActionListener(e -> cobrarEfectivo());
        txtPaga.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                actualizarCambioTemporal();
            }
        });

        // -------------------- LOG al abrir la ventana --------------------
        System.out.println("========================================");
        System.out.println("🟦 [LOG] Se ha abierto ventanaCobrar.");
        System.out.println("🟦 [LOG] Flag esVentaCredito = " + esVentaCredito);
        try {
            String txtId = Sistema.txtIdCV.getText();
            int idTmp = (txtId == null || txtId.trim().isEmpty()) ? -1 : Integer.parseInt(txtId.trim());
            String nombreTmp = (idTmp > 0) ? new VentaDao().obtenerNombreClientePorId(idTmp) : "N/A";
            System.out.println("🟦 [LOG] Campo Sistema.txtIdCV = '" + txtId + "' -> interpreto id = " + idTmp + " - nombre actual en campo = " + nombreTmp);
        } catch (Exception ex) {
            System.out.println("⚠️ [LOG] No se pudo leer Sistema.txtIdCV: " + ex.getMessage());
        }
        System.out.println("========================================");
    }

    public void setTotal(double total) {
        this.totalPagar = total;
        this.totalPagado = 0.0;
        this.totalComision = 0.0;
        lblTotal.setText(String.format("%.2f", totalPagar));
        lblCambio.setText("0.00");
        modeloPagos.setRowCount(0);
        txtPaga.setText("");
        txtPaga.setEditable(true);

        System.out.println("🔢 [LOG] setTotal() -> totalPagar establecido: " + totalPagar);
    }

    public double getSaldoPendiente() {
        return totalPagar - totalPagado;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    private void cobrarEfectivo() {
        try {
            String pagaTexto = txtPaga.getText().replace(",", "");

            if (pagaTexto == null || pagaTexto.isEmpty()) {
                lblCambio.setText("0.00");
                JOptionPane.showMessageDialog(this, "Ingresa un monto");
                txtPaga.requestFocus();
                return;
            }

            BigDecimal pagoOriginal = new BigDecimal(pagaTexto);

            if (pagoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Ingrese un monto válido.");
                return;
            }

            ultimoPagoEfectivo = pagoOriginal.doubleValue();

            BigDecimal saldoPendiente = BigDecimal.valueOf(getSaldoPendiente());
            BigDecimal pagoAplicado = pagoOriginal;

            if (pagoAplicado.compareTo(saldoPendiente) > 0) {
                pagoAplicado = saldoPendiente;
            }

            BigDecimal cambio = pagoOriginal.subtract(saldoPendiente);
            if (cambio.compareTo(BigDecimal.ZERO) < 0) {
                cambio = BigDecimal.ZERO;
            }

            lblCambio.setText(cambio.setScale(2, RoundingMode.HALF_UP).toString());

            agregarPago("Efectivo", pagoAplicado.doubleValue(), 0.00);

            System.out.println("💵 [LOG] cobrarEfectivo -> pagoOriginal: " + pagoOriginal + " | pagoAplicado: " + pagoAplicado + " | cambio calculado: " + cambio);

            if (getSaldoPendiente() <= 0) {
                txtPaga.setEditable(false);
                procesarVentaFinal();
            }

            txtPaga.setText("");
        } catch (NumberFormatException | ArithmeticException e) {
            lblCambio.setText("Error");
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido.");
        }
    }

    public void agregarPago(String metodo, double monto, double comision) {
        if (comision > 0) {
            totalComision += comision;
            totalPagar += comision;
        }

        double saldoPendiente = getSaldoPendiente();
        if (monto > saldoPendiente) {
            monto = saldoPendiente;
        }

        totalPagado += monto;

        modeloPagos.addRow(new Object[]{
                metodo,
                String.format("$%.2f (Comisión $%.2f)", monto, comision)
        });

        lblTotal.setText(String.format("%.2f", getSaldoPendiente()));

        System.out.println("➕ [LOG] agregarPago -> metodo: " + metodo + ", monto aplicado: " + monto + ", totalPagado now: " + totalPagado + ", saldo pendiente now: " + getSaldoPendiente());
    }

    public void finalizarPagoTarjeta() {
        procesarVentaFinal();
        txtCodigoVenta.requestFocus();
    }

    private void procesarVentaFinal() {
        // Cobro de un abono a producto: flujo propio
        if (modoAbono) {
            procesarAbono();
            return;
        }

        System.out.println("👉 procesarVentaFinal llamado");
        JDialog loader = new LoaderDialog().mostrarLoader(this);

        // =========================================================
        // 1. EXTRAER DATOS DE LA UI EN EL HILO PRINCIPAL (SEGURO)
        // =========================================================
        final String vendedorUI = Sistema.LabelVendedor.getText();

        double cambioTemporal = 0.0;
        try {
            cambioTemporal = Double.parseDouble(lblCambio.getText());
        } catch (NumberFormatException ex) {
            cambioTemporal = 0.0;
        }
        final double cambioUI = cambioTemporal;

        // Extraer productos a crédito de la UI una sola vez para no tocar JTable desde doInBackground
        final List<String[]> listaProductosCredito = new ArrayList<>();
        final List<String[]> listaAbonosCredito = new ArrayList<>();
        if (esVentaCredito && TableConsultaCreditCliente != null) {
            DefaultTableModel modelo = (DefaultTableModel) TableConsultaCreditCliente.getModel();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                String concepto = modelo.getValueAt(i, 2).toString();
                String[] fila = new String[4];
                fila[0] = modelo.getValueAt(i, 2).toString(); // Nombre producto o "ABONO REALIZADO"
                fila[1] = modelo.getValueAt(i, 3).toString(); // Cantidad
                fila[2] = modelo.getValueAt(i, 4).toString(); // Precio
                fila[3] = modelo.getValueAt(i, 1).toString(); // Id Producto

                if (concepto.toLowerCase().contains("abono")) {
                    listaAbonosCredito.add(fila);
                } else {
                    listaProductosCredito.add(fila);
                }
            }
        }

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                // =========================================================
                // 2. TAREAS PESADAS Y BASE DE DATOS (HILO SECUNDARIO)
                // =========================================================
                System.out.println("🚀 doInBackground iniciado");

                int idCliente = 1; // Default
                if (esVentaCredito) {
                    idCliente = new VentaDao().obtenerIdClientePorDniEmpresa(Integer.parseInt(dniCliente), idEmpresaActiva);
                    if (idCliente <= 0) {
                        throw new Exception("Cliente no encontrado.");
                    }
                }

                TurnoModel turno = TurnoController.getTurnoGlobal();
                if (turno == null) {
                    throw new Exception("No_Turno");
                }

                String tipoPagoFinal = determinarTipoPagoFinal();
                double subtotal = totalPagar - totalComision;

                CobroService servicio = new CobroService();
                servicio.setIdEmpresaActiva(idEmpresaActiva);

                int idVenta = servicio.procesarVenta(turno.getId(), idCliente, vendedorUI, Sistema.TableVenta, totalPagar, tipoPagoFinal, ultimoPagoEfectivo, cambioUI, totalComision, subtotal);
                if (idVenta == 0) {
                    throw new Exception("Error al registrar la venta (ID 0).");
                }

                boolean esCredito = esVentaCredito || tipoPagoFinal.equalsIgnoreCase("credito");
                String ticketGenerado = "";
                String nombreCliente = new VentaDao().obtenerNombreClientePorId(idCliente);

                if (esCredito) {
                    VentaDao ventaDao = new VentaDao();
                    for (String[] fila : listaProductosCredito) {
                        Detalle detalle = new Detalle();
                        detalle.setId_pro(Integer.parseInt(fila[3]));
                        detalle.setCantidad(Double.parseDouble(fila[1]));
                        detalle.setPrecio(Double.parseDouble(fila[2]));
                        detalle.setId(idVenta);
                        ventaDao.RegistrarDetalle(detalle);
                    }

                    int dni = ventaDao.obtenerDniPorIdCliente(idCliente);
                    if (dni != -1) {
                        ventaDao.eliminarCreditosDelCliente(dni, idEmpresaActiva);
                        new AbonoDao().actualizarAbonosAplicados(dni, idVenta);
                    }

                    if (tipoPagoFinal.equalsIgnoreCase("tarjeta")) {
                        ticketGenerado = ImprimirTicket.generarTicketCreditoConTarjeta(idVenta, nombreCliente, subtotal, totalComision, totalPagar, listaProductosCredito, listaAbonosCredito, fechaLocal);
                    } else if (tipoPagoFinal.equalsIgnoreCase("mixto")) {
                        ticketGenerado = ImprimirTicket.generarTicketCreditoMixto(idVenta, nombreCliente, subtotal, totalComision, totalPagar, listaProductosCredito, listaAbonosCredito, fechaLocal);
                    } else {
                        double cambioCredito = Math.max(0, ultimoPagoEfectivo - totalPagar);
                        ticketGenerado = ImprimirTicket.generarTicketCredito(idVenta, totalPagar, tipoPagoFinal, listaProductosCredito, listaAbonosCredito, ultimoPagoEfectivo, cambioCredito, nombreCliente, fechaLocal);
                    }
                } else {
                    switch (tipoPagoFinal.toLowerCase()) {
                        case "efectivo":
                            double cambioEfectivo = Math.max(0, ultimoPagoEfectivo - totalPagar);
                            ticketGenerado = ImprimirTicket.generarTicketEfectivo(idVenta, ultimoPagoEfectivo, cambioEfectivo, tipoPagoFinal, fechaLocal);
                            break;
                        default:
                            ticketGenerado = ImprimirTicket.generarTicketTarjeta(idVenta, totalComision, tipoPagoFinal, subtotal, fechaLocal);
                            break;
                    }
                }

                return ticketGenerado;
            }

            @Override
            protected void done() {
                // =========================================================
                // 3. ACTUALIZAR UI (HILO PRINCIPAL)
                // =========================================================
                loader.dispose();

                try {
                    String ticket = get();

                    if (ticket != null && !ticket.isEmpty()) {
                        if (!esVentaCredito && !ticket.toLowerCase().contains("tarjeta")) {
                            AbrirCajaEfectivo.main(null);
                        }
                        ImprimirTicket.imprimir(ticket);
                        mostrarTicketDialogSoloInformativo(ticket);
                    }

                    if (ventanaPadreConsulta != null) {
                        ventanaPadreConsulta.limpiarCampos();
                    }

                    DefaultTableModel tmp = (DefaultTableModel) Sistema.TableVenta.getModel();
                    tmp.setRowCount(0);
                    Sistema.lblEnviaTotal.setText("");
                    cobroRealizado = true;
                    dispose();
                    txtCodigoVenta.requestFocus();

                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("No_Turno")) {
                        JOptionPane.showMessageDialog(ventanaCobrar.this, "No hay un turno abierto.", "Atención", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ventanaCobrar.this, "Error al procesar la venta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /**
     * Cobro de un abono a un producto: crea la venta del día por el monto abonado
     * y guarda el abono ligado a esa venta y al producto.
     */
    private void procesarAbono() {
        System.out.println("👉 procesarAbono llamado");
        JDialog loader = new LoaderDialog().mostrarLoader(this);

        // Datos de la UI, leídos en el hilo principal
        final String vendedorUI = Sistema.LabelVendedor.getText();

        double cambioTemporal = 0.0;
        try {
            cambioTemporal = Double.parseDouble(lblCambio.getText());
        } catch (NumberFormatException ex) {
            cambioTemporal = 0.0;
        }
        final double cambioUI = cambioTemporal;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                TurnoModel turno = TurnoController.getTurnoGlobal();
                if (turno == null) {
                    throw new Exception("No_Turno");
                }

                VentaDao ventaDao = new VentaDao();
                int dni = Integer.parseInt(dniCliente);
                int idCliente = ventaDao.obtenerIdClientePorDniEmpresa(dni, idEmpresaActiva);
                if (idCliente <= 0) {
                    throw new Exception("Cliente no encontrado.");
                }

                String tipoPagoFinal = determinarTipoPagoFinal();   // Efectivo / Tarjeta / Mixto
                double subtotal = totalPagar - totalComision;       // lo que baja la deuda

                // 1) Venta del día por el abono
                String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date());
                Venta venta = new Venta();
                venta.setCliente(idCliente);
                venta.setVendedor(vendedorUI);
                venta.setTotal(totalPagar);
                venta.setFecha(fecha);
                venta.setTipopago(tipoPagoFinal);
                venta.setIdTurno(turno.getId());
                venta.setPagaCon(ultimoPagoEfectivo);
                venta.setCambio(cambioUI);
                venta.setComision(totalComision);
                venta.setSubtotal(subtotal);

                int idVenta = ventaDao.RegistrarVenta(venta);
                if (idVenta == 0) {
                    throw new Exception("No se pudo registrar la venta del abono.");
                }

                // 2) Abono(s) ligado(s) a esa venta y a su producto
                AbonoProductoDao abonoDao = new AbonoProductoDao();
                boolean ok;
                if (abonoPartes == null) {
                    ok = abonoDao.registrar(idVenta, subtotal, abonoNota, dni, idEmpresaActiva, abonoIdDetalle);
                } else {
                    ok = abonoDao.registrarVarios(idVenta, abonoPartes, abonoNota, dni, idEmpresaActiva);
                }
                if (!ok) {
                    abonoDao.eliminarVenta(idVenta);
                    throw new Exception("No se pudo guardar el abono. La venta se canceló.");
                }

                // 3) Ticket de abono
                double restante = Math.max(0, abonoPrecioProducto - abonoAbonadoAntes - subtotal);
                return ImprimirTicket.generarTicketAbono(idVenta, tipoPagoFinal, ultimoPagoEfectivo, cambioUI,
                        totalComision, subtotal, abonoProducto, abonoPrecioProducto, restante, fechaLocal);   }

            @Override
            protected void done() {
                loader.dispose();
                try {
                    String ticket = get();
                    if (ticket != null && !ticket.isEmpty()) {
                        if (!ticket.toLowerCase().contains("tarjeta")) {
                            AbrirCajaEfectivo.main(null);
                        }
                        ImprimirTicket.imprimir(ticket);
                        mostrarTicketDialogSoloInformativo(ticket);
                    }
                    cobroRealizado = true;
                    dispose();
                    if (abonoAlTerminar != null) {
                        abonoAlTerminar.run();
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("No_Turno")) {
                        JOptionPane.showMessageDialog(ventanaCobrar.this, "No hay un turno abierto.", "Atención", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ventanaCobrar.this, "Error al procesar el abono: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void enfocarCampoEfectivo() {
        txtPaga.requestFocus();
    }

    private String determinarTipoPagoFinal() {
        boolean hayEfectivo = false;
        boolean hayTarjeta = false;

        for (int i = 0; i < modeloPagos.getRowCount(); i++) {
            String metodo = modeloPagos.getValueAt(i, 0).toString();
            if (metodo.equalsIgnoreCase("Efectivo")) {
                hayEfectivo = true;
            }
            if (metodo.equalsIgnoreCase("Tarjeta")) {
                hayTarjeta = true;
            }
        }

        if (hayEfectivo && hayTarjeta) {
            return "Mixto";
        } else if (hayTarjeta) {
            return "Tarjeta";
        } else if (esVentaCredito) {
            return "Credito";
        } else {
            return "Efectivo";
        }
    }

    private void actualizarCambioTemporal() {
        try {
            double pago = Double.parseDouble(txtPaga.getText());
            double saldoPendiente = getSaldoPendiente();

            double cambio = pago - saldoPendiente;
            lblCambio.setText(String.format("%.2f", cambio > 0 ? cambio : 0.00));
        } catch (NumberFormatException e) {
            lblCambio.setText("0.00");
        }
    }

    private void mostrarTicketDialogSoloInformativo(String ticket) {
        JDialog dialog = new JDialog(this, "Vista previa del ticket", true);
        JTextArea area = new JTextArea(ticket);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 30));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 800));

        dialog.add(scroll);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

    public double getPagaConCredito() {
        try {
            return Double.parseDouble(txtPaga.getText().trim());
        } catch (NumberFormatException e) {
            return 0.00;
        }
    }

    public double getCambioCredito() {
        try {
            return Double.parseDouble(lblCambio.getText().trim());
        } catch (NumberFormatException e) {
            return 0.00;
        }
    }

    public boolean isCobroRealizado() {
        return cobroRealizado;
    }
}