package Controlador;

import Modelo.AbrirCajaEfectivo;
import Modelo.MovimientoCaja;
import Modelo.MovimientoCajaDao;
import Modelo.TurnoModel;
import Vista.CorteCajeroView;
import Vista.MovimientosCaja;
import Vista.Sistema;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class MovimientosCajaController implements ActionListener {

    private MovimientosCaja vista;
    private MovimientoCajaDao dao;

    public MovimientosCajaController(MovimientosCaja vista) {
        this.vista = vista;
        this.dao = new MovimientoCajaDao();

        // Agregar listeners
        this.vista.btnGuardarIngreso.addActionListener(this);
        this.vista.btnGuardarEgreso.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.btnGuardarIngreso) {
            registrarIngreso();
        } else if (e.getSource() == vista.btnGuardarEgreso) {
            registrarEgreso();
        }
    }

    private void registrarIngreso() {
        try {
            String concepto = vista.txtConceptoIngreso.getText().trim();
            String nota = vista.txtNotaIngreso.getText().trim();
            String montoStr = vista.txtMontoIngreso.getText().trim();

            if (concepto.isEmpty() || montoStr.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "Concepto y Monto son obligatorios para ingreso.");
                return;
            }

            double monto = Double.parseDouble(montoStr);

            // Obtener turno actual
            TurnoModel turno = Controlador.TurnoController.getTurnoGlobal();
            if (turno == null) {
                JOptionPane.showMessageDialog(vista, "No hay turno abierto.");
                return;
            }

            MovimientoCaja ingreso = new MovimientoCaja();
            ingreso.setTipo("Ingreso");
            ingreso.setConcepto(concepto);
            ingreso.setNota(nota);
            ingreso.setMonto(monto);
            ingreso.setGasto(null); // No aplica gasto en ingreso
            ingreso.setFecha(new Date());
          ingreso.setIdTurnoActual(turno.getId());


            // --- CORRECCIÓN: asignar empresa activa ---
            ingreso.setIdEmpresa(Sistema.getIdEmpresaActiva());

            boolean guardado = dao.registrarMovimiento(ingreso);

            if (guardado) {
                JOptionPane.showMessageDialog(vista, "Ingreso guardado correctamente.");
                limpiarCamposIngreso();
                    if (vista.onGuardado != null) vista.onGuardado.run();   // <-- paso 2

                vista.dispose();
                AbrirCajaEfectivo.main(null);

                // Refrescar datos en CorteCajeroView sin pasar parámetros nulos
                CorteCajeroView corteView = CorteCajeroView.getInstance();
                if (corteView != null) {
                    corteView.refrescarDatosTurno();
                }

            } else {
                JOptionPane.showMessageDialog(vista, "Error al guardar ingreso.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Monto inválido. Ingrese un número válido.");
        }
    }

    private void registrarEgreso() {
        try {
            String concepto = vista.txtConceptoEgreso.getText().trim();
            String nota = vista.txtNotaEgreso.getText().trim();
            String montoStr = vista.txtMontoEgreso.getText().trim();
            String gasto = (String) vista.cmbGastoEgreso.getSelectedItem();

            if (concepto.isEmpty() || montoStr.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "Concepto y Monto son obligatorios para egreso.");
                return;
            }

            double monto = Double.parseDouble(montoStr);

            // Obtener turno actual
            TurnoModel turno = Controlador.TurnoController.getTurnoGlobal();
            if (turno == null) {
                JOptionPane.showMessageDialog(vista, "No hay turno abierto.");
                return;
            }

            MovimientoCaja egreso = new MovimientoCaja();
            egreso.setTipo("Egreso");
            egreso.setConcepto(concepto);
            egreso.setNota(nota);
            egreso.setMonto(monto);
            egreso.setGasto(gasto);
            egreso.setFecha(new Date());
            egreso.setIdTurnoActual(turno.getId());

            // --- CORRECCIÓN: asignar empresa activa ---
            egreso.setIdEmpresa(Sistema.getIdEmpresaActiva());

            boolean guardado = dao.registrarMovimiento(egreso);

            if (guardado) {
                JOptionPane.showMessageDialog(vista, "Egreso guardado correctamente.");
                limpiarCamposEgreso();
                    if (vista.onGuardado != null) vista.onGuardado.run();   // <-- paso 2

                vista.dispose();
                AbrirCajaEfectivo.main(null);

                // Refrescar datos en CorteCajeroView sin pasar parámetros nulos
                CorteCajeroView corteView = CorteCajeroView.getInstance();
                if (corteView != null) {
                    corteView.refrescarDatosTurno();
                }

            } else {
                JOptionPane.showMessageDialog(vista, "Error al guardar egreso.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Monto inválido. Ingrese un número válido.");
        }
    }

    private void limpiarCamposIngreso() {
        vista.txtMontoIngreso.setText("");
        vista.txtConceptoIngreso.setText("");
        vista.txtNotaIngreso.setText("");
    }

    private void limpiarCamposEgreso() {
        vista.txtMontoEgreso.setText("");
        vista.txtConceptoEgreso.setText("");
        vista.txtNotaEgreso.setText("");
        vista.cmbGastoEgreso.setSelectedIndex(0);
    }
}
