package Controlador;

import Modelo.ImprimirTicket;
import Modelo.Venta;
import Modelo.VentaDao;
import Modelo.AbonoProductoDao;
import Modelo.TurnoHistorialDao;
import Modelo.TurnoModel;
import Vista.HistorialVentasPanel;
import Vista.DetalleVentaDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.event.KeyEvent;

/**
 * Controlador de Historial de Ventas (filtrado por TURNO).
 */
public class HistorialVentasController {

    private static final int SIN_TURNO = 0;      // idTurno 0 = ventas con id_turno NULL
    private static final int MAX_TURNOS = 30;    // cuántos turnos recientes lista el combo

    private final HistorialVentasPanel panel;
    private final VentaDao ventaDao = new VentaDao();
    private final TurnoHistorialDao turnoDao = new TurnoHistorialDao();
    private final int idEmpresa;

    private final Map<Integer, TurnoHistorialDao.TurnoInfo> turnosCache = new HashMap<>();
    private final SimpleDateFormat fmtInicio = new SimpleDateFormat("dd/MM HH:mm");
    private final SimpleDateFormat fmtHora = new SimpleDateFormat("HH:mm");

    public HistorialVentasController(HistorialVentasPanel panel, int idEmpresa) {
        this.panel = panel;
        this.idEmpresa = idEmpresa;

        this.panel.getBtnPdf().addActionListener(this::onGenerarPdf);
        instalarBotonReimprimir();
        instalarDobleClicDetalle();
        instalarEnterDetalle();

        // Cambiar de turno en el combo (el flag evita disparar esto al llenar el combo por código)
        this.panel.getCmbTurnos().addActionListener(e -> {
            if (panel.isCargandoTurnos()) return;
            panel.getTxtBuscarFolio().setText("");
            cargarVentasDelTurno();
        });

        // Buscar folio en todos los turnos: botón o Enter en el campo
        this.panel.getBtnBuscarFolio().addActionListener(e -> buscarFolioGlobal());
        this.panel.getTxtBuscarFolio().addActionListener(e -> buscarFolioGlobal());
    }

    // =====================================================================
    //  TURNOS Y VENTAS
    // =====================================================================

    /**
     * Carga (o recarga) el historial. Refresca la lista de turnos (por si se abrió uno nuevo)
     * y conserva el turno que estaba seleccionado; la primera vez selecciona el más reciente.
     */
    public void cargarVentas() {
        HistorialVentasPanel.TurnoItem sel = panel.getTurnoSeleccionado();
        cargarTurnos(sel == null ? -1 : sel.idTurno);
        cargarVentasDelTurno();
    }

    /**
     * Llamar al entrar a la pestaña Historial: siempre arranca en el turno actual
     * (si no hay turno abierto, en el más reciente).
     */
    public void abrir() {
        TurnoModel actual = TurnoController.getTurnoGlobal();
        cargarTurnos(actual == null ? -1 : actual.getId());
        panel.getTxtBuscarFolio().setText("");
        cargarVentasDelTurno();
    }

    private void cargarTurnos(int idTurnoASeleccionar) {
        turnosCache.clear();
        List<HistorialVentasPanel.TurnoItem> items = new ArrayList<>();

        for (TurnoHistorialDao.TurnoInfo t : turnoDao.ultimosTurnos(idEmpresa, MAX_TURNOS)) {
            turnosCache.put(t.id, t);
            String cierre = t.fechaCierre == null ? "ABIERTO" : fmtHora.format(t.fechaCierre);
            items.add(new HistorialVentasPanel.TurnoItem(t.id,
                    "#" + t.id + " · " + t.usuario + " · "
                    + fmtInicio.format(t.fechaInicio) + " – " + cierre));
        }
        items.add(new HistorialVentasPanel.TurnoItem(SIN_TURNO, "Sin turno (ventas anteriores)"));

        panel.setTurnos(items, idTurnoASeleccionar);
    }

    private void cargarVentasDelTurno() {
        panel.limpiar();

        HistorialVentasPanel.TurnoItem turno = panel.getTurnoSeleccionado();
        if (turno == null) {
            panel.setTotalVendido(0);
            panel.setResumenTurno("");
            return;
        }

        List<Venta> lista = ventaDao.ListarVentasPorTurno(idEmpresa, turno.idTurno);

        double totalVendido = 0;
        for (Venta v : lista) {
            String estatus = v.isCancelada() ? "Cancelada" : "Exitosa";
            panel.agregarFila(v.getFolio(), v.getNombre_cli(), v.getVendedor(),
                              v.getTotal(), estatus, v.getId());
            if (!v.isCancelada()) {
                totalVendido += v.getTotal();
            }
        }
        panel.setTotalVendido(totalVendido);
        panel.setResumenTurno(armarResumen(turno.idTurno, lista.size()));
    }

    private String armarResumen(int idTurno, int numVentas) {
        if (idTurno == SIN_TURNO) {
            return "Ventas registradas antes de usar turnos · " + numVentas + " ventas";
        }
        TurnoHistorialDao.TurnoInfo t = turnosCache.get(idTurno);
        if (t == null) {
            return numVentas + " ventas";
        }
        return String.format("Cajero: %s · Fondo inicial: $%,.2f · %s · %d ventas",
                t.usuario, t.saldoInicial, t.estado, numVentas);
    }

    /** Busca el folio en TODOS los turnos y, si existe, cambia al turno donde está. */
    private void buscarFolioGlobal() {
        String txt = panel.getTxtBuscarFolio().getText().trim();
        if (txt.isEmpty()) return;

        int folio;
        try {
            folio = Integer.parseInt(txt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel, "El folio debe ser un número.");
            return;
        }

        // Si ya está en el turno que se ve, solo lo selecciona
        if (panel.seleccionarFolio(txt)) return;

        int idTurno = turnoDao.turnoDeFolio(idEmpresa, folio); // -1 = no existe, 0 = sin turno
        if (idTurno < 0) {
            JOptionPane.showMessageDialog(panel, "No se encontró el folio " + folio + ".");
            return;
        }

        cargarTurnos(idTurno);       // si el turno es más viejo que los últimos 30, el combo
                                     // selecciona el primero; se avisa abajo
        HistorialVentasPanel.TurnoItem sel = panel.getTurnoSeleccionado();
        if (sel == null || sel.idTurno != idTurno) {
            JOptionPane.showMessageDialog(panel,
                    "El folio " + folio + " pertenece a un turno anterior a los últimos "
                    + MAX_TURNOS + " turnos.");
        }
        cargarVentasDelTurno();
        panel.seleccionarFolio(txt);
    }

    /** Enter sobre la fila seleccionada abre el detalle (en vez de bajar a la siguiente fila). */
    private void instalarEnterDetalle() {
        JTable tabla = panel.getTableVentas();
        tabla.getInputMap(JComponent.WHEN_FOCUSED)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrirDetalle");
        tabla.getActionMap().put("abrirDetalle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirDetalleSeleccionado();
            }
        });
    }

    private void abrirDetalleSeleccionado() {
        int filaVista = panel.getTableVentas().getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(panel, "Selecciona una venta primero.");
            return;
        }
        int filaModelo = panel.getTableVentas().convertRowIndexToModel(filaVista);
        try {
            int idVenta = Integer.parseInt(
                    String.valueOf(panel.getModelo().getValueAt(filaModelo, HistorialVentasPanel.COL_ID_VENTA)));
            abrirDetalle(idVenta);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel, "No se pudo identificar la venta.");
        }
    }

    private void onGenerarPdf(ActionEvent e) {
        String folioTxt = panel.getTxtFolioSeleccionado().getText().trim();
        if (folioTxt.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Selecciona una fila primero");
            return;
        }
        try {
            int folio = Integer.parseInt(folioTxt);
            Venta venta = ventaDao.BuscarVentaPorFolio(folio, idEmpresa);
            if (venta == null) {
                JOptionPane.showMessageDialog(panel, "No se encontró la venta con folio " + folio);
                return;
            }
            ventaDao.pdfV(venta.getId(), venta.getVendedor());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel, "Folio inválido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Error al generar PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // =====================================================================
    //  DETALLE + CANCELACIÓN
    // =====================================================================

    /** Doble clic en una fila (excepto la columna del botón Reimprimir) abre el detalle. */
    private void instalarDobleClicDetalle() {
        JTable tabla = panel.getTableVentas();
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;

                int filaVista = tabla.rowAtPoint(e.getPoint());
                int colVista = tabla.columnAtPoint(e.getPoint());
                if (filaVista < 0 || colVista == HistorialVentasPanel.COL_TICKET) return;

                int filaModelo = tabla.convertRowIndexToModel(filaVista);
                try {
                    int idVenta = Integer.parseInt(
                            String.valueOf(panel.getModelo().getValueAt(filaModelo, HistorialVentasPanel.COL_ID_VENTA)));
                    abrirDetalle(idVenta);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "No se pudo identificar la venta.");
                }
            }
        });
    }

    private void abrirDetalle(int idVenta) {
        Venta v = ventaDao.BuscarVenta(idVenta);
        if (v == null) {
            JOptionPane.showMessageDialog(panel, "No se pudo cargar la venta.");
            return;
        }

        DetalleVentaDialog dlg = new DetalleVentaDialog(
                SwingUtilities.getWindowAncestor(panel),
                v.getFolio(),
                v.getNombre_cli(),
                v.getVendedor(),
                v.getTotal(),
                ventaDao.listarDetalleCreditoPorVenta(idVenta),
                ventaDao.listarPromocionesPorVenta(idVenta));

        if (v.isCancelada()) {
            dlg.getBtnCancelar().setEnabled(false);
            dlg.getBtnCancelar().setText("Ya cancelada");
        }

        // Solo se cancelan ventas del turno actual (misma regla que VentaDao.cancelarVenta)
        TurnoModel turnoActual = TurnoController.getTurnoGlobal();
        if (!v.isCancelada() && (turnoActual == null || turnoActual.getId() != v.getIdTurno())) {
            dlg.getBtnCancelar().setEnabled(false);
            dlg.getBtnCancelar().setText("Turno cerrado");
        }

        dlg.getBtnCancelar().addActionListener(ev -> cancelarVenta(dlg, v));
        dlg.setVisible(true);
    }

    private void cancelarVenta(DetalleVentaDialog dlg, Venta v) {
        // Los abonos no llevan productos en 'detalle': no se cancelan desde aquí
        if (new AbonoProductoDao().obtenerInfoAbono(v.getId()) != null) {
            JOptionPane.showMessageDialog(dlg,
                    "Este folio es un abono. Los abonos no se cancelan desde aquí.");
            return;
        }

        String motivo = JOptionPane.showInputDialog(dlg,
                "¿Cancelar la venta #" + v.getFolio() + "?\n"
                + "Los productos regresarán al stock.\n\nMotivo:");
        if (motivo == null) return; // presionó Cancelar
        if (motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dlg, "Escribe un motivo.");
            return;
        }

        try {
            ventaDao.cancelarVenta(v.getId(), idEmpresa, obtenerUsuarioActual(), motivo.trim());
            JOptionPane.showMessageDialog(dlg, "Venta cancelada y stock restaurado.");
            dlg.dispose();
            cargarVentas(); // conserva el turno seleccionado
            Vista.Sistema.getInstancia().actualizarVentasHoy(); // card "Tus ventas"
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, ex.getMessage());
        }
    }

    /** Usuario que cancela. Reemplázalo por el usuario en sesión (ej. Sistema.getUsuarioActivo()). */
    private String obtenerUsuarioActual() {
        return "Efren Rodriguez"; // TODO
    }

    // =====================================================================
    //  REIMPRIMIR
    // =====================================================================

    /**
     * Botón "Reimprimir" embebido en la columna Ticket de la tabla.
     */
    private void instalarBotonReimprimir() {
        JTable tabla = panel.getTableVentas();

        // Renderer para mostrar el botón (columna Ticket)
        tabla.getColumnModel().getColumn(HistorialVentasPanel.COL_TICKET).setCellRenderer(new DefaultTableCellRenderer() {
            private final JButton btn = new JButton("Reimprimir");

            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                return btn;
            }
        });

        tabla.getColumnModel().getColumn(HistorialVentasPanel.COL_TICKET).setCellEditor(new ReimprimirButtonEditor());
    }

    private class ReimprimirButtonEditor extends javax.swing.AbstractCellEditor
            implements javax.swing.table.TableCellEditor {

        private final JButton boton = new JButton("Reimprimir");
        private int filaActual;
        private Object valorCelda;

        ReimprimirButtonEditor() {
            boton.addActionListener(e -> {
                fireEditingStopped();
                reimprimir(filaActual, valorCelda);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return valorCelda; // Devuelve el valor original para no sobrescribir el modelo
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.filaActual = row;
            this.valorCelda = value;
            return boton;
        }
    }

    private void reimprimir(int filaVista, Object valorIdCelda) {
        try {
            int idVenta = -1;

            // 1. Intentamos obtener el ID enviado
            if (valorIdCelda instanceof Integer) {
                idVenta = (Integer) valorIdCelda;
            } else if (valorIdCelda != null && !valorIdCelda.toString().equals("Reimprimir")) {
                idVenta = Integer.parseInt(valorIdCelda.toString().trim());
            }

            // 2. Leemos la fila del modelo (convertRowIndexToModel: ya soporta el filtro por folio)
            int filaModelo = panel.getTableVentas().convertRowIndexToModel(filaVista);
            DefaultTableModel modelo = panel.getModelo();

            // Intentamos buscar la venta primero por ID
            Venta venta = null;
            if (idVenta > 0) {
                venta = ventaDao.BuscarVenta(idVenta);
            }

            // 3. Si no se encontró por ID, leemos la Columna 0 (Folio)
            if (venta == null) {
                Object valCol0 = modelo.getValueAt(filaModelo, 0);
                if (valCol0 != null) {
                    int folio = Integer.parseInt(valCol0.toString().trim());
                    venta = ventaDao.BuscarVentaPorFolio(folio, idEmpresa);
                }
            }

            // 4. Si aún es null, mostramos un aviso
            if (venta == null) {
                JOptionPane.showMessageDialog(panel,
                        "No se pudo recuperar la venta.\n" +
                        "Verifica si BuscarVenta o BuscarVentaPorFolio corresponden con la BD.",
                        "Venta no encontrada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Aviso si se intenta reimprimir una venta cancelada
            if (venta.isCancelada()) {
                int r = JOptionPane.showConfirmDialog(panel,
                        "Esta venta está CANCELADA. ¿Reimprimir de todas formas?",
                        "Venta cancelada", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) return;
            }

            // Obtener el ID correcto de la venta recuperada
            idVenta = venta.getId();

            String tipoPago = venta.getTipopago() != null ? venta.getTipopago() : "Efectivo";
            String ticket;

            // ¿El folio es un abono (a producto o a categoría)?
            AbonoProductoDao.InfoAbono info = new AbonoProductoDao().obtenerInfoAbono(idVenta);

            if (info != null) {
                ticket = ImprimirTicket.generarTicketAbono(
                        idVenta, tipoPago, venta.getPagaCon(), venta.getCambio(),
                        venta.getComision(), info.monto, info.concepto, info.precio, info.restante,
                        parsearFechaVenta(venta.getFechaHora()));
            } else {
                switch (tipoPago.toLowerCase()) {
                    case "tarjeta":
                    case "mixto":
                        double subtotal = venta.getSubtotal();
                        double comision = venta.getComision();
                        ticket = ImprimirTicket.generarTicketTarjeta(idVenta, comision, tipoPago, subtotal,
                                parsearFechaVenta(venta.getFechaHora()));
                        break;

                    case "credito":
                        List<Object[]> detalleCredito = ventaDao.listarDetalleCreditoPorVenta(idVenta);

                        if (detalleCredito.isEmpty()) {
                            JOptionPane.showMessageDialog(panel, "No se encontró detalle de crédito para esta venta.");
                            return;
                        }

                        List<String[]> listaProductosCredito = new ArrayList<>();
                        for (Object[] fila : detalleCredito) {
                            listaProductosCredito.add(new String[]{
                                String.valueOf(fila[0]), // producto
                                String.valueOf(fila[1]), // cantidad
                                String.valueOf(fila[2])  // precio
                            });
                        }

                        ticket = ImprimirTicket.generarTicketCredito(
                                idVenta,
                                venta.getTotal(),
                                tipoPago,
                                listaProductosCredito,
                                new ArrayList<>(),
                                venta.getPagaCon(),
                                venta.getCambio(),
                                venta.getNombre_cli(),
                                parsearFechaVenta(venta.getFechaHora()));
                        break;

                    default: // Efectivo
                        ticket = ImprimirTicket.generarTicketEfectivo(
                                idVenta, venta.getPagaCon(), venta.getCambio(), tipoPago,
                                parsearFechaVenta(venta.getFechaHora()));
                        break;
                }
            }

            ImprimirTicket.imprimir(ticket);
            mostrarVistaPrevia(ticket);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Error al reimprimir: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarVistaPrevia(String ticket) {
        JTextArea area = new JTextArea(ticket);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 500));

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(panel), "Vista previa del ticket", true);
        dialog.getContentPane().add(scroll);
        dialog.pack();
        dialog.setLocationRelativeTo(panel);
        dialog.setVisible(true);
    }

    private Date parsearFechaVenta(String fechaHoraStr) {
        try {
            SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            local.setTimeZone(java.util.TimeZone.getDefault());
            return local.parse(fechaHoraStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al interpretar fecha: " + fechaHoraStr, e);
        }
    }
}