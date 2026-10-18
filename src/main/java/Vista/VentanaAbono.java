package Vista;

import Modelo.AbonoDao;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class VentanaAbono extends JDialog {

    private JTextField txtMontoAbono;
    private JTextField txtNota;
    private JButton btnAceptar;
    private JTable tabla;
    private JTextField txtTotalCredito;
    private int idVenta;
    private int idCliente;
    private String dni;
    private JFrame parent;

    // Solo permite dígitos y un punto decimal (máximo 2 decimales)
    private static class FiltroDecimal extends DocumentFilter {
        private static final String REGEX = "\\d*(\\.\\d{0,2})?";

        private boolean esValido(FilterBypass fb, int offset, int length, String text)
                throws BadLocationException {
            String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String nuevo = actual.substring(0, offset)
                    + (text == null ? "" : text)
                    + actual.substring(offset + length);
            return nuevo.matches(REGEX);
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet attr)
                throws BadLocationException {
            if (str != null && esValido(fb, offset, 0, str)) {
                super.insertString(fb, offset, str, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (esValido(fb, offset, length, text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    public VentanaAbono(JFrame parent, JTable tablaCredito, JTextField txtTotalCredito,
                        int idVenta, int idCliente, String dni) {
        super(parent, "Registrar Abono", true);
        this.parent = parent;
        this.tabla = tablaCredito;
        this.txtTotalCredito = txtTotalCredito;
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.dni = dni;

        setLayout(null);
        setSize(320, 230);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);

        JLabel lblMonto = new JLabel("Abonar $:");
        lblMonto.setBounds(30, 30, 60, 25);
        add(lblMonto);

        txtMontoAbono = new JTextField();
        ((AbstractDocument) txtMontoAbono.getDocument()).setDocumentFilter(new FiltroDecimal());
        txtMontoAbono.setBounds(100, 30, 160, 25);
        add(txtMontoAbono);

        JLabel lblNota = new JLabel("Nota:");
        lblNota.setBounds(30, 70, 60, 25);
        add(lblNota);

        txtNota = new JTextField();
        txtNota.setBounds(100, 70, 160, 25);
        add(txtNota);

        btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(100, 120, 100, 30);
        btnAceptar.setBackground(new Color(0, 153, 76));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFocusPainted(false);
        add(btnAceptar);

        btnAceptar.addActionListener(e -> registrarAbono());

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

 private void registrarAbono() {
    String montoTexto = txtMontoAbono.getText().trim();
    String notaTexto = txtNota.getText().trim();

    if (montoTexto.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingrese un monto válido para abonar.");
        return;
    }

    double monto;
    try {
        monto = Double.parseDouble(montoTexto);
        if (monto <= 0) {
            JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero.");
            return;
        }

        double totalCredito = Double.parseDouble(txtTotalCredito.getText().trim());
        if (monto >= totalCredito) {
            JOptionPane.showMessageDialog(this, "El abono debe ser menor que el total del crédito.");
            return;
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.");
        return;
    }

    String tipoPago = "ABONO";

    try {
        AbonoDao abonoDao = new AbonoDao();
        // Llamar registrarAbono sin fecha, según tu AbonoDao
        boolean exito = abonoDao.registrarAbono(idVenta, monto, notaTexto, tipoPago, Integer.parseInt(dni));

        if (exito) {
            JOptionPane.showMessageDialog(this, "Abono registrado correctamente.");
            if (parent instanceof ConsultaCreditoCliente) {
                ConsultaCreditoCliente ventanaPadre = (ConsultaCreditoCliente) parent;
                ventanaPadre.listar();
                ventanaPadre.actualizarTotalDesdeTabla();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar abono.");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error al registrar abono: " + ex.getMessage());
        ex.printStackTrace();
    }
}

}
