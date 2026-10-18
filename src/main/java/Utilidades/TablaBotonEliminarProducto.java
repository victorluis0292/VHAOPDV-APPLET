package Utilidades;

import Modelo.VentaDao;
import Vista.ConsultaCreditoCliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;

public class TablaBotonEliminarProducto {

    // Renderiza el botón con imagen
    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        private final ImageIcon iconoEliminar;

        public ButtonRenderer() {
            iconoEliminar = new ImageIcon(getClass().getResource("/Img/eliminar.png"));
            setIcon(iconoEliminar);
            setText(null);
            setOpaque(true);
            setBorderPainted(false);
            setContentAreaFilled(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    // Editor del botón con imagen y acción eliminar
    public static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private JTable table;
        private final ImageIcon iconoEliminar;
        private ConsultaCreditoCliente ventana;

        public ButtonEditor(JCheckBox checkBox, JTable table, ConsultaCreditoCliente ventana) {
            super(checkBox);
            this.table = table;
            this.ventana = ventana;

            iconoEliminar = new ImageIcon(getClass().getResource("/Img/eliminar.png"));

            button = new JButton();
            button.setIcon(iconoEliminar);
            button.setText(null);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selectedRow = table.getSelectedRow();
                    System.out.println("[LOG] Botón eliminar presionado. Fila seleccionada: " + selectedRow);

                    if (selectedRow != -1) {
                        Object nombreObj = table.getValueAt(selectedRow, 2);
                        if (nombreObj == null) {
                            System.out.println("[LOG] ⚠ Columna 2 es null, no se puede eliminar.");
                            JOptionPane.showMessageDialog(null, "Error: no se encontró el nombre del registro");
                            return;
                        }

                        String nombre = nombreObj.toString();
                        System.out.println("[LOG] Nombre del registro: " + nombre);

                        int respuesta = JOptionPane.showConfirmDialog(
                                null,
                                "¿Realmente desea eliminar el registro \"" + nombre + "\"?",
                                "Confirmar eliminación",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        if (respuesta == JOptionPane.YES_OPTION) {
                            try {
                                Object idObj = table.getValueAt(selectedRow, 0);
                                if (idObj == null) {
                                    System.out.println("[LOG] ⚠ ID es null, no se puede eliminar.");
                                    JOptionPane.showMessageDialog(null, "Error: no se encontró el ID del registro");
                                    return;
                                }

                                int id = Integer.parseInt(idObj.toString());
                                System.out.println("[LOG] ID a eliminar: " + id);

                                VentaDao dao = new VentaDao();
                                boolean eliminado = false;

                                if (nombre.toUpperCase().startsWith("ABONO REALIZADO")) {
                                    System.out.println("[LOG] Eliminando abono de crédito...");
                                    eliminado = dao.eliminarAbonoCreditoPorId(id);
                                } else {
                                    System.out.println("[LOG] Eliminando producto de crédito...");
                                eliminado = dao.eliminarProdCreditoPorId(id, ventana.getIdEmpresaActiva());

                                }

                                System.out.println("[LOG] Resultado de eliminación: " + eliminado);

                                if (eliminado) {
                    cancelCellEditing();              // 1º: suelta el editor
                    if (ventana != null) {
                        ventana.listar();             // 2º: recarga la tabla y el total desde la BD
                    }
                    JOptionPane.showMessageDialog(null, "Registro eliminado correctamente");
                    } else {
                    cancelCellEditing();
                    JOptionPane.showMessageDialog(null, "Error al eliminar el registro en la base de datos");
                    }
                                        } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, "Error interno al intentar eliminar");
                            }
                        } else {
                            cancelCellEditing();
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
