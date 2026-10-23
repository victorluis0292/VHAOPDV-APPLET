package Vista;

import Estilos.EstilosBannersCreditocliente;
import Modelo.Conexion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


/**
 * Banners de clientes con fiados pendientes para la pantalla de venta.
 *
 * - Solo se pintan los clientes con saldo > 0; si no hay ninguno, el panel
 *   queda transparente y vacío (la pantalla se ve igual que sin banners).
 * - Se actualiza solo: al mostrarse la pestaña, al reactivarse la ventana
 *   (por ejemplo al cerrar cobrar o el historial) y después de un tap.
 * - Al dar tap en un banner llama a alTocar(dni, nombre); quien lo instancia
 *   decide qué abrir (el historial del cliente).
 * - Orden: del fiado más antiguo al más reciente (rojo, ámbar, verde).
 * - Todo el diseño (colores, fuentes, tamaño, dibujo de la tarjeta) vive en
 *   Estilos.EstilosBannersCreditocliente.
 */
public class BannersCreditoCliente extends JPanel {

    // Evita consultas repetidas cuando varios eventos llegan juntos
    private static final long MIN_MS_ENTRE_CARGAS = 2000;

    private static final String SQL =
            "SELECT d.dni, d.cliente, "
          + "SUM(GREATEST(d.total - IFNULL(a.abonado, 0), 0)) AS saldo, "
          + "MIN(d.fecha) AS fiado_desde "
          + "FROM detalle_creditocliente d "
          + "LEFT JOIN (SELECT id_detalle, SUM(monto) AS abonado "
          + "           FROM abonos_credito WHERE id_empresa = ? "
          + "           GROUP BY id_detalle) a ON a.id_detalle = d.id "
          + "WHERE d.id_empresa = ? AND d.pagado = 0 "
          + "GROUP BY d.dni, d.cliente "
          + "HAVING saldo > 0 "
          + "ORDER BY saldo DESC";

    private final BiConsumer<Integer, String> alTocar;
    private final JLabel titulo = new JLabel();
    private final JPanel fila = new JPanel(
            new FlowLayout(FlowLayout.LEFT, EstilosBannersCreditocliente.SEPARACION, 2));
    private final JScrollPane scroll;

    private Window ventana;
    private WindowAdapter oyenteVentana;
    private long ultimaCarga = 0;
    private boolean cargando = false;

    public BannersCreditoCliente(BiConsumer<Integer, String> alTocar) {
        this.alTocar = alTocar;
        setLayout(new BorderLayout(0, 2));
        setOpaque(false);

        EstilosBannersCreditocliente.estiloTitulo(titulo);

        // 1) primero se crea el scroll (sin barras)
        scroll = new JScrollPane(fila,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 2) y hasta después se le aplica el estilo
        EstilosBannersCreditocliente.estiloContenedor(this);
        EstilosBannersCreditocliente.estiloContenedor(fila);
        EstilosBannersCreditocliente.estiloContenedor(scroll);
        EstilosBannersCreditocliente.estiloContenedor(scroll.getViewport());
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        EstilosBannersCreditocliente.habilitarArrastre(scroll, fila);

        // la rueda del mouse desplaza los banners de lado
        scroll.addMouseWheelListener(e -> {
            JScrollBar b = scroll.getHorizontalScrollBar();
            b.setValue(b.getValue() + e.getWheelRotation() * 60);
        });

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        titulo.setVisible(false);
        scroll.setVisible(false);

        // al volver a la pestaña de venta
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refrescar();
            }
        });
    }

    // ------------------------------------------------------------------
    // Refresco
    // ------------------------------------------------------------------

    /** Refresca si el panel está a la vista y pasaron al menos 2 s desde la última carga. */
    public void refrescar() {
        cargar(false);
    }

    /** Refresca sin esperar (por ejemplo después de cobrar o abonar). */
    public void refrescarAhora() {
        cargar(true);
    }

    private void cargar(boolean forzar) {
        if (cargando || !isShowing()) return;
        long ahora = System.currentTimeMillis();
        if (!forzar && ahora - ultimaCarga < MIN_MS_ENTRE_CARGAS) return;
        final int idEmpresa = Sistema.getIdEmpresaActiva();
        if (idEmpresa <= 0) return;

        cargando = true;
        ultimaCarga = ahora;
        new SwingWorker<List<Fiado>, Void>() {
            @Override
            protected List<Fiado> doInBackground() throws Exception {
                return consultar(idEmpresa);
            }

            @Override
            protected void done() {
                cargando = false;
                try {
                    pintar(get());
                } catch (Exception ex) {
                    // si falla la consulta, la venta sigue normal y los banners se ocultan
                    System.err.println("[BANNERS] No se pudieron cargar los fiados: " + ex.getMessage());
                    pintar(new ArrayList<Fiado>());
                }
            }
        }.execute();
    }

    private List<Fiado> consultar(int idEmpresa) throws Exception {
        List<Fiado> lista = new ArrayList<>();
        // AJUSTA SOLO ESTA LÍNEA si tu Conexion entrega la conexión de otra forma
        try (Connection cn = new Conexion().getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL)) {
            ps.setInt(1, idEmpresa);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Fiado(
                            rs.getInt("dni"),
                            rs.getString("cliente"),
                            rs.getDouble("saldo"),
                            parsearFecha(rs.getString("fiado_desde"))));
                }
            }
        }

        // Más antiguo primero (rojo, luego ámbar, luego verde).
        // Si empatan en días, el de mayor saldo va antes.
        lista.sort((a, b) -> {
            int porDias = Long.compare(b.dias(), a.dias());
            return porDias != 0 ? porDias : Double.compare(b.saldo, a.saldo);
        });

        return lista;
    }

    /** Acepta "yyyy-MM-dd..." o "dd/MM/yyyy". Cualquier otra cosa (incluida 0000-00-00) devuelve null. */
    private static LocalDate parsearFecha(String s) {
        if (s == null) return null;
        s = s.trim();
        try {
            if (s.length() >= 10 && s.charAt(4) == '-') {
                LocalDate d = LocalDate.parse(s.substring(0, 10));
                return d.getYear() < 1900 ? null : d;
            }
            if (s.length() >= 10 && s.charAt(2) == '/') {
                return LocalDate.parse(s.substring(0, 10),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (Exception ignorada) {
            // fecha inválida: se trata como sin fecha
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Pintado
    // ------------------------------------------------------------------

    private void pintar(List<Fiado> lista) {
        fila.removeAll();
        boolean hay = !lista.isEmpty();
        titulo.setVisible(hay);
        scroll.setVisible(hay);
        if (hay) {
            titulo.setText("Fiados pendientes (" + lista.size() + ")");
            for (Fiado f : lista) {
                fila.add(crearBanner(f));
            }
        }
        revalidate();
        repaint();
    }

    private JPanel crearBanner(final Fiado f) {
        JPanel t = EstilosBannersCreditocliente.crearTarjeta(
                f.cliente,
                "#" + f.dni,
                f.saldo,
                f.dias(),
                () -> {
                    if (alTocar != null) {
                        alTocar.accept(f.dni, f.cliente);
                    }
                    refrescarAhora();
                });
        EstilosBannersCreditocliente.habilitarArrastre(scroll, t);
        return t;
    }

    // ------------------------------------------------------------------
    // Reactivación de la ventana (al cerrar cobrar, historial, etc.)
    // ------------------------------------------------------------------

    @Override
    public void addNotify() {
        super.addNotify();
        ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana != null && oyenteVentana == null) {
            oyenteVentana = new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    refrescar();
                }
            };
            ventana.addWindowListener(oyenteVentana);
        }
    }

    @Override
    public void removeNotify() {
        if (ventana != null && oyenteVentana != null) {
            ventana.removeWindowListener(oyenteVentana);
            oyenteVentana = null;
        }
        super.removeNotify();
    }

    // ------------------------------------------------------------------

    private static final class Fiado {
        final int dni;
        final String cliente;
        final double saldo;
        final LocalDate desde;

        Fiado(int dni, String cliente, double saldo, LocalDate desde) {
            this.dni = dni;
            this.cliente = cliente;
            this.saldo = saldo;
            this.desde = desde;
        }

        long dias() {
            if (desde == null) return 0;
            return Math.max(0, ChronoUnit.DAYS.between(desde, LocalDate.now()));
        }
    }
}