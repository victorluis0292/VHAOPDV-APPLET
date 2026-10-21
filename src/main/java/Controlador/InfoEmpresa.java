package Controlador;

import Modelo.Empresa;
import Modelo.EmpresaDao;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.function.IntSupplier;

/**
 * Lógica de la pantalla Config (datos de la empresa).
 * Sistema solo le pasa los campos y le pide cargar() o actualizar().
 */
public class InfoEmpresa {

    private final EmpresaDao empDao = new EmpresaDao();

    private final JTextField txtId, txtRuc, txtNombre, txtTelefono, txtDireccion, txtMensaje;
    private final IntSupplier idEmpresaActiva;

    public InfoEmpresa(JTextField txtId, JTextField txtRuc, JTextField txtNombre,
                       JTextField txtTelefono, JTextField txtDireccion, JTextField txtMensaje,
                       IntSupplier idEmpresaActiva) {
        this.txtId = txtId;
        this.txtRuc = txtRuc;
        this.txtNombre = txtNombre;
        this.txtTelefono = txtTelefono;
        this.txtDireccion = txtDireccion;
        this.txtMensaje = txtMensaje;
        this.idEmpresaActiva = idEmpresaActiva;
    }

    /** Trae los datos de la empresa activa y llena los campos. */
    public void cargar() {
        int idEmpresa = idEmpresaActiva.getAsInt();
        if (idEmpresa <= 0) {
            System.out.println("DEBUG InfoEmpresa.cargar: no hay empresa activa todavía.");
            return;
        }

        Empresa emp = empDao.BuscarDatos(idEmpresa);
        if (emp == null) {
            JOptionPane.showMessageDialog(null, "No se encontró la empresa con ID " + idEmpresa);
            return;
        }

        txtId.setText(String.valueOf(emp.getId_empresa()));
        txtRuc.setText(nvl(emp.getRuc()));
        txtNombre.setText(nvl(emp.getNombre()));
        txtTelefono.setText(nvl(emp.getTelefono()));
        txtDireccion.setText(nvl(emp.getDireccion()));
        txtMensaje.setText(nvl(emp.getMensaje()));
    }

    /** Valida los campos y guarda los cambios. */
    public void actualizar() {
        if (txtRuc.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()
                || txtTelefono.getText().trim().isEmpty()
                || txtDireccion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Los campos están vacíos");
            return;
        }

        // Si Config no cargó los datos, el ID está vacío y no se debe guardar
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No se cargaron los datos de la empresa. Vuelve a abrir Config.");
            return;
        }

        Empresa empresa = new Empresa();
        empresa.setRuc(txtRuc.getText().trim());
        empresa.setNombre(txtNombre.getText().trim());
        empresa.setTelefono(txtTelefono.getText().trim());
        empresa.setDireccion(txtDireccion.getText().trim());
        empresa.setMensaje(txtMensaje.getText().trim());
        empresa.setId_empresa(Integer.parseInt(txtId.getText().trim()));
        empresa.setEstado(1);   // mantiene la empresa activa

        if (empDao.ModificarDatos(empresa)) {
            JOptionPane.showMessageDialog(null, "Datos de la empresa modificados correctamente");
            cargar();   // refrescar datos
        } else {
            JOptionPane.showMessageDialog(null, "Error al modificar los datos de la empresa");
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}