package Controlador;

import Modelo.Productos;
import Vista.VerduleriaPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class VerduleriaController {
    private final VerduleriaPanel panel;
    private final JTable tableVenta;
    private final Runnable listenerBascula = this::aplicarConfigBascula;

    // 👇 NUEVO: lo asigna quien crea el controller (Sistema.java), donde sí existe txtCodigoVenta
    private Runnable onProductoAgregado;

    public VerduleriaController(VerduleriaPanel panel, JTable tableVenta) {
        this.panel = panel;
        this.tableVenta = tableVenta;
        this.panel.getBtnAceptar().addActionListener(e -> agregarProductoATabla());

        aplicarConfigBascula();

        // 👇 Si cambian la config de báscula en cualquier momento (Configuraciones),
        // reaccionamos al instante sin necesidad de reiniciar la app.
        Modelo.ConfiguracionDispositivos.agregarListenerBascula(listenerBascula);
    }

    // 👇 NUEVO
    public void setOnProductoAgregado(Runnable r) {
        this.onProductoAgregado = r;
    }

    private void aplicarConfigBascula() {
        Modelo.ConfiguracionDispositivos.ConfigBascula cfg = Modelo.ConfiguracionDispositivos.cargarConfigBascula();
        panel.desconectarBascula(); // cierra cualquier conexión previa siempre primero
        if (cfg.habilitada && cfg.puerto != null && !cfg.puerto.isEmpty()) {
            panel.conectarBascula(cfg.puerto);
        }
    }

    private void agregarProductoATabla() {
        Productos producto = panel.getProductoActual();
        if (producto == null) return;
        double cantidadKg;
        try {
            cantidadKg = Double.parseDouble(panel.getTxtCantidad().getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(panel, "Cantidad inválida");
            return;
        }
        if (cantidadKg <= 0) {
            JOptionPane.showMessageDialog(panel, "Ingrese una cantidad mayor a 0");
            return;
        }
        double total = producto.getPrecioKg() * cantidadKg;
        DefaultTableModel modelo = (DefaultTableModel) tableVenta.getModel();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int idFila = Integer.parseInt(modelo.getValueAt(i, 0).toString());
            if (idFila == producto.getId()) {
                double cantidadActual = Double.parseDouble(modelo.getValueAt(i, 2).toString());
                double nuevaCantidad = cantidadActual + cantidadKg;
                double nuevoTotal = producto.getPrecioKg() * nuevaCantidad;
                modelo.setValueAt(nuevaCantidad, i, 2);
                modelo.setValueAt(nuevoTotal, i, 4);
                finalizarAgregado();   // 👈 antes: panel.mostrarGrid();
                return;
            }
        }
        modelo.addRow(new Object[]{
            producto.getId(),
            producto.getNombre(),
            cantidadKg,
            producto.getPrecioKg(),
            total
        });
        finalizarAgregado();           // 👈 antes: panel.mostrarGrid();
    }

    // 👇 NUEVO: vuelve a la cuadrícula y después pide el foco para el campo Código
    private void finalizarAgregado() {
        panel.mostrarGrid();
        if (onProductoAgregado != null) {
            SwingUtilities.invokeLater(onProductoAgregado);
        }
    }

    /** Llama esto al cerrar la pantalla de Nueva Venta, para liberar el puerto serial y el listener. */
    public void liberarBascula() {
        panel.desconectarBascula();
        Modelo.ConfiguracionDispositivos.quitarListenerBascula(listenerBascula);
    }
}