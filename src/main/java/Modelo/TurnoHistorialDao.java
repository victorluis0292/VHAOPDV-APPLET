package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de turnos que usa el Historial de Ventas.
 * ⚠ Cambia obtenerConexion() por la forma en que tu proyecto abre conexión (HikariCP / Conexion).
 */
public class TurnoHistorialDao {

    /** Datos de un turno para pintar el combo y el resumen. */
    public static class TurnoInfo {
        public int id;
        public String usuario;
        public Timestamp fechaInicio;
        public Timestamp fechaCierre; // null = abierto
        public double saldoInicial;
        public String estado;
    }

    private final Conexion cn = new Conexion();

    private Connection obtenerConexion() throws Exception {
        return cn.getConnection();
    }

    /** Últimos N turnos de la empresa, del más reciente al más antiguo. */
    public List<TurnoInfo> ultimosTurnos(int idEmpresa, int limite) {
        String sql = "SELECT id, usuario, fecha_inicio, fecha_cierre, saldo_inicial, estado "
                   + "FROM turnos WHERE id_empresa = ? ORDER BY id DESC LIMIT ?";
        List<TurnoInfo> lista = new ArrayList<>();
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurnoInfo t = new TurnoInfo();
                    t.id = rs.getInt("id");
                    t.usuario = rs.getString("usuario");
                    t.fechaInicio = rs.getTimestamp("fecha_inicio");
                    t.fechaCierre = rs.getTimestamp("fecha_cierre");
                    t.saldoInicial = rs.getDouble("saldo_inicial");
                    t.estado = rs.getString("estado");
                    lista.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Turno al que pertenece un folio.
     * @return -1 si el folio no existe, 0 si la venta no tiene turno, o el id del turno.
     */
    public int turnoDeFolio(int idEmpresa, int folio) {
        String sql = "SELECT id_turno FROM ventas WHERE id_empresa = ? AND folio = ? "
                   + "ORDER BY id DESC LIMIT 1";
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setInt(2, folio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idTurno = rs.getInt("id_turno");
                    return rs.wasNull() ? 0 : idTurno;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}

/*
 * ==========================================================================
 *  Pendiente en VentaDao: ListarVentasPorTurno(int idEmpresa, int idTurno)
 * ==========================================================================
 *  Copia tu ListarVentasPorFecha(...) completo y cambia SOLO el WHERE y el setXxx:
 *
 *    String filtroTurno = (idTurno == 0) ? "v.id_turno IS NULL" : "v.id_turno = ?";
 *    String sql = "... WHERE v.id_empresa = ? AND " + filtroTurno + " ORDER BY v.id DESC";
 *    ps.setInt(1, idEmpresa);
 *    if (idTurno != 0) ps.setInt(2, idTurno);
 *
 *  (conserva el mismo JOIN con clientes y el mismo mapeo a Venta: nombre_cli, cancelada, etc.)
 */