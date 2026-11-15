package Controlador;

import Modelo.ImprimirTicket;
import Modelo.Venta;
import Modelo.VentaDao;
import Modelo.AbonoProductoDao;
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
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.event.KeyEvent;

/**
 * Controlador de Historial de Ventas.
 */
public class HistorialVentasController {

    private final HistorialVentasPanel panel;
    private final VentaDao ventaDao = new VentaDao();
    private final int idEmpresa;
    private LocalDate diaActual = LocalDate.now();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HistorialVentasController(HistorialVentasPanel panel, int idEmpresa) {
        this.panel = panel;
        this.idEmpresa = idEmpresa;

        this.panel.getBtnPdf().addActionListener(this::onGenerarPdf);
        instalarBotonReimprimir();
        instalarDobleClicDetalle();
        instalarEnterDetalle();

        this.panel.getBtnDiaAnterior().addActionListener(e -> {
            diaActual = diaActual.minusDays(1);
            cargarVentas();
        });

        this.panel.getBtnDiaSiguiente().addActionListener(e -> {
            if (diaActual.isBefore(LocalDate.now())) {
                diaActual = diaActual.plusDays(1);
                cargarVentas();
            }
        });
    }

    /** Carga (o recarga) la tabla con las ventas de la empresa activa. */
    public void cargarVentas() {
        panel.limpiar();

        java.sql.Date fechaSql = java.sql.Date.valueOf(diaActual);
        List<Venta> lista = ventaDao.ListarVentasPorFecha(idEmpresa, fechaSql);

        for (Venta v : lista) {
    String estatus = v.isCancelada() ? "Cancelada" : "Exitosa";
    panel.agregarFila(v.getFolio(), v.getNombre_cli(), v.getVendedor(),
                      v.getTotal(), estatus, v.getId());
}

        actualizarEtiquetaFecha();
    }

    private void actualizarEtiquetaFecha() {
        String texto;
        if (diaActual.equals(LocalDate.now())) {
            texto = "Hoy (" + diaActual.format(FORMATO_FECHA) + ")";
        } else {
            texto = diaActual.format(FORMATO_FECHA);
        }
        panel.getLblFechaActual().setText(texto);
        panel.getBtnDiaSiguiente().setEnabled(diaActual.isBefore(LocalDate.now()));
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
            cargarVentas();
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
     * Botón "Reimprimir" embebido en la columna 4 de la tabla.
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
            return valorCelda; // Devuelve el ID original para no sobrescribir el modelo
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.filaActual = row;
            this.valorCelda = value; // Guarda el valor entero almacenado en la celda (id_venta)
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

            // 2. Leemos la fila del modelo
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

            Date fechaLocal = local.parse(fechaHoraStr);

            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            utc.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            System.out.println("Fecha recibida de BD: " + fechaHoraStr);
            System.out.println("Interpretada como hora local: " + local.format(fechaLocal));
            System.out.println("El mismo instante en UTC: " + utc.format(fechaLocal));

            return fechaLocal;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al interpretar fecha: " + fechaHoraStr, e);
        }
    }
}