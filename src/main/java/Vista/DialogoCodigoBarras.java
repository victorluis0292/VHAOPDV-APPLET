package Vista;

import Estilos.Estilos;
import Utilidades.ImpresorasUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import javax.print.PrintService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class DialogoCodigoBarras extends JDialog {

    private static final double MM_A_PT = 72.0 / 25.4;
    private static final String PREF_IMPRESORA = "impresora_etiquetas";
    private static final String PREF_PERFIL = "etiqueta_perfil";
    private static final String PREF_ANCHO = "etiqueta_ancho";
    private static final String PREF_ALTO = "etiqueta_alto";

    private static class Perfil {
        final String nombre;
        final double anchoMm, altoMm, margenMm;
        final boolean personalizado;

        Perfil(String nombre, double anchoMm, double altoMm, double margenMm, boolean personalizado) {
            this.nombre = nombre;
            this.anchoMm = anchoMm;
            this.altoMm = altoMm;
            this.margenMm = margenMm;
            this.personalizado = personalizado;
        }

        @Override
        public String toString() { return nombre; }
    }

    private static final Perfil[] PERFILES = {
        new Perfil("Rollo 58 mm (pruebas)", 58, 32, 5, false),
        new Perfil("Etiqueta 50 x 30 mm", 50, 30, 2, false),
        new Perfil("Etiqueta 40 x 30 mm", 40, 30, 2, false),
        new Perfil("Etiqueta 50 x 25 mm", 50, 25, 2, false),
        new Perfil("Personalizado...", 40, 30, 2, true)
    };

    private final Preferences prefs = Preferences.userNodeForPackage(DialogoCodigoBarras.class);

    private final String nombre, codigo, precio;
    private BitMatrix matriz;
    private boolean esEan;
    private String errorCodigo;

    private final JComboBox<String> cbxImpresora = new JComboBox<>();
    private final JComboBox<Perfil> cbxTamano = new JComboBox<>(PERFILES);
    private final JSpinner spAncho = new JSpinner(new SpinnerNumberModel(40.0, 15.0, 200.0, 1.0));
    private final JSpinner spAlto = new JSpinner(new SpinnerNumberModel(30.0, 10.0, 300.0, 1.0));
    private final JSpinner spCopias = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JCheckBox chkPrecio = new JCheckBox("Incluir precio en la etiqueta", true);
    private final JCheckBox chkRecordar = new JCheckBox("Recordar impresora y tamaño", true);
    private final JLabel lblAviso = new JLabel(" ");
    private final JLabel lblMedidas = etiqueta("Medidas (mm):", 13, true);
    private final JPanel panelMedidas = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    private final PanelEtiqueta panelEtiqueta = new PanelEtiqueta();

    public DialogoCodigoBarras(Frame owner, String nombre, String codigo, String precio) {
        super(owner, "Generador de código de barras", true);
        this.nombre = nombre == null ? "" : nombre;
        this.codigo = codigo == null ? "" : codigo;
        this.precio = precio == null ? "" : precio;

        generarMatriz();
        cargarImpresoras();
        cargarPreferenciasTamano();
        
        estilizarSpinner(spCopias, 80);
        estilizarSpinner(spAncho, 75);
        estilizarSpinner(spAlto, 75);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // --- Datos ---
        JPanel datos = new JPanel(new GridLayout(0, 1, 2, 2));
        datos.setOpaque(false);
        datos.setBorder(new EmptyBorder(15, 20, 5, 20));
        datos.add(etiqueta("Producto:  " + this.nombre, 15, true));
        datos.add(etiqueta("Código:  " + this.codigo + "      Precio: $" + this.precio, 13, false));
        add(datos, BorderLayout.NORTH);

        // --- Opciones ---
        panelMedidas.setOpaque(false);
        panelMedidas.add(new JLabel("Ancho"));
        panelMedidas.add(spAncho);
        panelMedidas.add(new JLabel("Alto"));
        panelMedidas.add(spAlto);

        chkPrecio.setOpaque(false);
        chkRecordar.setOpaque(false);
        lblAviso.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel opciones = new JPanel(new GridBagLayout());
        opciones.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 5, 4, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        int f = 0;
        g.gridx = 0; g.gridy = f; g.weightx = 0; opciones.add(etiqueta("Impresora:", 13, true), g);
        g.gridx = 1; g.weightx = 1; opciones.add(cbxImpresora, g);
        f++;
        g.gridx = 0; g.gridy = f; g.weightx = 0; opciones.add(etiqueta("Tamaño:", 13, true), g);
        g.gridx = 1; g.weightx = 1; opciones.add(cbxTamano, g);
        f++;
        g.gridx = 0; g.gridy = f; g.weightx = 0; opciones.add(lblMedidas, g);
        g.gridx = 1; g.weightx = 1; opciones.add(panelMedidas, g);
        f++;
        g.gridx = 0; g.gridy = f; g.weightx = 0; opciones.add(etiqueta("Copias:", 13, true), g);
        JPanel panelCopias = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCopias.setOpaque(false);
        panelCopias.add(spCopias);
        g.gridx = 1; g.weightx = 1; opciones.add(panelCopias, g);
        f++;
        g.gridx = 1; g.gridy = f; opciones.add(chkPrecio, g);
        f++;
        g.gridx = 1; g.gridy = f; opciones.add(chkRecordar, g);
        f++;
        g.gridx = 0; g.gridy = f; g.gridwidth = 2; opciones.add(lblAviso, g);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(0, 15, 0, 15));
        centro.add(panelEtiqueta, BorderLayout.CENTER);
        centro.add(opciones, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // --- Botones ---
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnImprimir = new JButton("Imprimir");
        Estilos.estiloBotonAzul(btnCancelar);
        Estilos.estiloBotonAzul(btnImprimir);
        btnImprimir.setEnabled(matriz != null && cbxImpresora.isEnabled());
        btnCancelar.addActionListener(e -> dispose());
        btnImprimir.addActionListener(e -> imprimir());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        sur.setOpaque(false);
        sur.add(btnCancelar);
        sur.add(btnImprimir);
        add(sur, BorderLayout.SOUTH);

        // --- Eventos de actualización ---
        cbxTamano.addActionListener(e -> refrescar());
        spAncho.addChangeListener(e -> refrescar());
        spAlto.addChangeListener(e -> refrescar());
        chkPrecio.addActionListener(e -> refrescar());
        refrescar();

        setSize(540, 700);
        setLocationRelativeTo(owner);
    }

    // ============================================================
    // CÓDIGO DE BARRAS (matriz natural, sin escalar)
    // ============================================================

    private void generarMatriz() {
        try {
            esEan = esEan13Valido(codigo);
            BarcodeFormat fmt = esEan ? BarcodeFormat.EAN_13 : BarcodeFormat.CODE_128;
            matriz = new MultiFormatWriter().encode(codigo, fmt, 1, 1);
        } catch (Exception ex) {
            matriz = null;
            errorCodigo = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
    }

    private static boolean esEan13Valido(String c) {
        if (c == null || !c.matches("\\d{13}")) return false;
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int d = c.charAt(i) - '0';
            suma += (i % 2 == 0) ? d : d * 3;
        }
        return (10 - suma % 10) % 10 == c.charAt(12) - '0';
    }

    // ============================================================
    // DIBUJO DE LA ETIQUETA (lo usan la vista previa y la impresión)
    // ============================================================

    private void dibujarEtiqueta(Graphics2D g2, double w, double h, double margen, boolean conPrecio) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setColor(Color.BLACK);

        FontRenderContext frc = g2.getFontRenderContext();
        double x0 = margen;
        double ancho = w - 2 * margen;
        double alto = h - 2 * margen;
        double y = margen;

        float tamNombre = (float) Math.max(7, Math.min(13, alto * 0.17));
        float tamPie = (float) Math.max(8, Math.min(12, alto * 0.15));
        Font fNombre = new Font("SansSerif", Font.BOLD, 10).deriveFont(tamNombre);
        Font fPie = new Font("Monospaced", Font.PLAIN, 10).deriveFont(tamPie);
        Font fPrecio = new Font("SansSerif", Font.BOLD, 10).deriveFont(tamPie + 2);

        // Nombre (1 o 2 líneas)
        int maxLineas = alto >= 66 ? 2 : 1;
        List<String> lineas = partirTexto(nombre, fNombre, frc, ancho, maxLineas);
        g2.setFont(fNombre);
        for (String linea : lineas) {
            java.awt.font.LineMetrics lm = fNombre.getLineMetrics(linea, frc);
            double lw = fNombre.getStringBounds(linea, frc).getWidth();
            y += lm.getAscent();
            g2.drawString(linea, (float) (x0 + (ancho - lw) / 2), (float) y);
            y += lm.getDescent() + lm.getLeading();
        }
        y += 2;

        // Pie
        java.awt.font.LineMetrics lmPie = fPie.getLineMetrics(codigo, frc);
        double altoPie = Math.max(lmPie.getHeight(), fPrecio.getLineMetrics("$0", frc).getHeight());

        if (matriz == null) {
            g2.setFont(fPie);
            g2.drawString("Código no válido", (float) x0, (float) (y + lmPie.getAscent()));
            return;
        }

        // Barras vectoriales a todo el ancho útil
        double altoBarras = Math.max(12, (margen + alto) - y - altoPie - 2);
        int n = matriz.getWidth();
        double modulo = ancho / n;
        int i = 0;
        while (i < n) {
            if (matriz.get(i, 0)) {
                int ini = i;
                while (i < n && matriz.get(i, 0)) i++;
                g2.fill(new Rectangle2D.Double(x0 + ini * modulo, y, (i - ini) * modulo, altoBarras));
            } else {
                i++;
            }
        }
        y += altoBarras + 2;

        // Código legible y precio
        g2.setFont(fPie);
        double baseline = y + lmPie.getAscent();
        double wCodigo = fPie.getStringBounds(codigo, frc).getWidth();
        String textoPrecio = "$" + precio;
        if (conPrecio && !precio.isEmpty()) {
            g2.drawString(codigo, (float) x0, (float) baseline);
            g2.setFont(fPrecio);
            double wPrecio = fPrecio.getStringBounds(textoPrecio, frc).getWidth();
            g2.drawString(textoPrecio, (float) (x0 + ancho - wPrecio), (float) baseline);
        } else {
            g2.drawString(codigo, (float) (x0 + (ancho - wCodigo) / 2), (float) baseline);
        }
    }

    private List<String> partirTexto(String texto, Font font, FontRenderContext frc, double ancho, int maxLineas) {
        List<String> lineas = new ArrayList<>();
        String actual = "";
        for (String palabra : texto.trim().split("\\s+")) {
            if (palabra.isEmpty()) continue;
            String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (anchoTexto(prueba, font, frc) <= ancho) {
                actual = prueba;
            } else {
                if (!actual.isEmpty()) { lineas.add(actual); actual = ""; }
                while (anchoTexto(palabra, font, frc) > ancho && palabra.length() > 1) {
                    int k = palabra.length();
                    while (k > 1 && anchoTexto(palabra.substring(0, k), font, frc) > ancho) k--;
                    lineas.add(palabra.substring(0, k));
                    palabra = palabra.substring(k);
                }
                actual = palabra;
            }
        }
        if (!actual.isEmpty()) lineas.add(actual);
        if (lineas.isEmpty()) lineas.add("");

        if (lineas.size() > maxLineas) {
            lineas = new ArrayList<>(lineas.subList(0, maxLineas));
            String ult = lineas.get(maxLineas - 1);
            while (ult.length() > 1 && anchoTexto(ult + "...", font, frc) > ancho) {
                ult = ult.substring(0, ult.length() - 1);
            }
            lineas.set(maxLineas - 1, ult + "...");
        }
        return lineas;
    }

    private double anchoTexto(String s, Font f, FontRenderContext frc) {
        return f.getStringBounds(s, frc).getWidth();
    }

    // ============================================================
    // VISTA PREVIA
    // ============================================================

    private class PanelEtiqueta extends JPanel {
        PanelEtiqueta() {
            setBackground(new Color(225, 229, 235));
            setPreferredSize(new Dimension(0, 230));
            setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Perfil p = perfilActual();
            double w = p.anchoMm * MM_A_PT;
            double h = p.altoMm * MM_A_PT;
            double escala = Math.min((getWidth() - 30) / w, (getHeight() - 30) / h);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate((getWidth() - w * escala) / 2, (getHeight() - h * escala) / 2);
            g2.scale(escala, escala);

            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Double(0, 0, w, h));
            dibujarEtiqueta(g2, w, h, p.margenMm * MM_A_PT, chkPrecio.isSelected());

            g2.setColor(new Color(150, 150, 150));
            g2.setStroke(new BasicStroke((float) (1 / escala)));
            g2.draw(new Rectangle2D.Double(0, 0, w, h));
            g2.dispose();
        }
    }

    private void refrescar() {
        boolean custom = perfilSeleccionado().personalizado;
        lblMedidas.setVisible(custom);
        panelMedidas.setVisible(custom);

        if (matriz == null) {
            lblAviso.setForeground(new Color(180, 0, 0));
            lblAviso.setText("No se pudo generar el código: " + errorCodigo);
        } else {
            Perfil p = perfilActual();
            double mod = (p.anchoMm - 2 * p.margenMm) / matriz.getWidth();
            String formato = esEan ? "EAN-13" : "Code 128";
            String medida = String.format("%.2f", mod);
            if (mod >= 0.25) {
                lblAviso.setForeground(new Color(0, 130, 60));
                lblAviso.setText(formato + " - ancho de barra " + medida + " mm (OK para lector)");
            } else {
                lblAviso.setForeground(new Color(200, 110, 0));
                lblAviso.setText(formato + " - barras muy finas (" + medida + " mm). Usa una etiqueta más ancha.");
            }
        }
        panelEtiqueta.repaint();
    }

    private Perfil perfilSeleccionado() {
        Perfil p = (Perfil) cbxTamano.getSelectedItem();
        return p == null ? PERFILES[0] : p;
    }

    private Perfil perfilActual() {
        Perfil p = perfilSeleccionado();
        if (!p.personalizado) return p;
        return new Perfil("Personalizado",
                ((Number) spAncho.getValue()).doubleValue(),
                ((Number) spAlto.getValue()).doubleValue(),
                p.margenMm, true);
    }

    // ============================================================
    // IMPRESORAS Y PREFERENCIAS
    // ============================================================

    private void cargarImpresoras() {
        List<PrintService> servicios = ImpresorasUtil.listar();
        if (servicios.isEmpty()) {
            cbxImpresora.addItem("No se detectaron impresoras");
            cbxImpresora.setEnabled(false);
            return;
        }
        for (PrintService s : servicios) cbxImpresora.addItem(s.getName());
        String guardada = prefs.get(PREF_IMPRESORA, null);
        if (guardada != null) cbxImpresora.setSelectedItem(guardada);
    }

    private void cargarPreferenciasTamano() {
        int idx = prefs.getInt(PREF_PERFIL, 0);
        if (idx >= 0 && idx < PERFILES.length) cbxTamano.setSelectedIndex(idx);
        spAncho.setValue(prefs.getDouble(PREF_ANCHO, 40.0));
        spAlto.setValue(prefs.getDouble(PREF_ALTO, 30.0));
    }
    private void estilizarSpinner(JSpinner spinner, int ancho) {
    spinner.setPreferredSize(new Dimension(ancho, 32));
    JTextField campo = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
    campo.setFont(new Font("Segoe UI", Font.BOLD, 16));
    campo.setHorizontalAlignment(JTextField.CENTER);
    }
    private PrintService servicioSeleccionado() {
        String sel = (String) cbxImpresora.getSelectedItem();
        for (PrintService s : ImpresorasUtil.listar()) {
            if (s.getName().equals(sel)) return s;
        }
        return null;
    }

    // ============================================================
    // IMPRESIÓN
    // ============================================================

    private void imprimir() {
        PrintService servicio = servicioSeleccionado();
        if (servicio == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una impresora válida.");
            return;
        }

        if (chkRecordar.isSelected()) {
            prefs.put(PREF_IMPRESORA, servicio.getName());
            prefs.putInt(PREF_PERFIL, cbxTamano.getSelectedIndex());
            prefs.putDouble(PREF_ANCHO, ((Number) spAncho.getValue()).doubleValue());
            prefs.putDouble(PREF_ALTO, ((Number) spAlto.getValue()).doubleValue());
        }

        final Perfil perfil = perfilActual();
        final boolean conPrecio = chkPrecio.isSelected();
        final int copias = (Integer) spCopias.getValue();

        final double wPt = perfil.anchoMm * MM_A_PT;
        final double hPt = perfil.altoMm * MM_A_PT;
        final double margenPt = perfil.margenMm * MM_A_PT;

        new Thread(() -> {
            try {
                Paper paper = new Paper();
                paper.setSize(wPt, hPt);
                paper.setImageableArea(0, 0, wPt, hPt);

                PageFormat pf = new PageFormat();
                pf.setOrientation(PageFormat.PORTRAIT);
                pf.setPaper(paper);

                Printable printable = (Graphics gr, PageFormat fmt, int pagina) -> {
                    if (pagina > 0) return Printable.NO_SUCH_PAGE;
                    Graphics2D g2 = (Graphics2D) gr;
                    g2.translate(fmt.getImageableX(), fmt.getImageableY());
                    dibujarEtiqueta(g2, fmt.getImageableWidth(), fmt.getImageableHeight(), margenPt, conPrecio);
                    return Printable.PAGE_EXISTS;
                };

                for (int i = 0; i < copias; i++) {
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPrintService(servicio);
                    job.setPrintable(printable, pf);
                    job.print();
                }
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Etiqueta enviada a " + servicio.getName());
                    dispose();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private static JLabel etiqueta(String texto, int size, boolean bold) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(50, 50, 50));
        return l;
    }
}