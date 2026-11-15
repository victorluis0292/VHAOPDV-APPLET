package Modelo;

import java.math.BigDecimal;
import java.sql.*;

public class TurnoDAO {

    // Insertar nuevo turno con id_empresa
    public boolean abrirTurno(TurnoModel turno) {
        String sql = "INSERT INTO turnos (usuario, id_empresa, fecha_inicio, saldo_inicial, estado) VALUES (?, ?, ?, ?, 'ABIERTO')";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, turno.getUsuario());
            ps.setInt(2, turno.getIdEmpresa()); // <-- nuevo campo
            ps.setTimestamp(3, turno.getFechaInicio());
            ps.setBigDecimal(4, turno.getSaldoInicial());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    turno.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtener turno abierto por usuario y empresa
   // Obtener turno abierto por usuario y empresa
public TurnoModel obtenerTurnoAbierto(String usuario, int idEmpresa) {
    if (usuario == null || usuario.trim().isEmpty()) {
        System.out.println("⚠️ Usuario nulo o vacío al buscar turno.");
        return null;
    }

    usuario = usuario.trim();
    String sql = "SELECT * FROM turnos WHERE estado = 'ABIERTO' "
               + "AND LOWER(TRIM(usuario)) = LOWER(TRIM(?)) "
               + "AND id_empresa = ? "
               + "ORDER BY id DESC LIMIT 1";

    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, usuario);
        ps.setInt(2, idEmpresa);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                TurnoModel turno = new TurnoModel();
                turno.setId(rs.getInt("id"));
                turno.setUsuario(rs.getString("usuario"));
                turno.setIdEmpresa(rs.getInt("id_empresa")); // <-- asignar id_empresa
                turno.setFechaInicio(rs.getTimestamp("fecha_inicio"));
                turno.setSaldoInicial(rs.getBigDecimal("saldo_inicial"));
                turno.setEstado(rs.getString("estado"));

                System.out.println("Turno abierto encontrado con ID: " + turno.getId());
                return turno;
            } else {
                System.out.println("No se encontró turno abierto para usuario: '" 
                                    + usuario + "' y empresa: " + idEmpresa);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}

    // Cerrar turno
    public boolean cerrarTurno(TurnoModel turno) {
        String sql = "UPDATE turnos SET fecha_cierre = ?, total_ventas = ?, ingresos = ?, egresos = ?, ganancia = ?, total_en_caja = ?, estado = ? WHERE id = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, turno.getFechaCierre());
            ps.setBigDecimal(2, turno.getTotalVentas());
            ps.setBigDecimal(3, turno.getIngresos());
            ps.setBigDecimal(4, turno.getEgresos());
            ps.setBigDecimal(5, turno.getGanancia());
            ps.setBigDecimal(6, turno.getTotalEnCaja());
            ps.setString(7, "CERRADO");
            ps.setInt(8, turno.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Iniciar turno con empresa
    public int iniciarTurno(String usuario, BigDecimal saldoInicial, int idEmpresa) {
        TurnoModel turno = new TurnoModel();
        turno.setUsuario(usuario);
        turno.setIdEmpresa(idEmpresa);
        turno.setFechaInicio(new Timestamp(System.currentTimeMillis()));
        turno.setSaldoInicial(saldoInicial);

        if (abrirTurno(turno)) {
            return turno.getId();  // El id se setea en abrirTurno con getGeneratedKeys()
        }
        return -1;
    }

    // Métodos para obtener ventas, ingresos, egresos, ganancia...
    public BigDecimal obtenerVentas(int turnoId) {
    BigDecimal total = BigDecimal.ZERO;
    String sql = "SELECT SUM(total) AS total_ventas FROM ventas WHERE id_turno = ? AND cancelada = 0";
   

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turnoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getBigDecimal("total_ventas");
                    if (total == null) total = BigDecimal.ZERO;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ventas en tiempo real: " + e.getMessage());
        }

        return total;
    }

    public BigDecimal obtenerIngresos(int turnoId) {
        BigDecimal resultado = BigDecimal.ZERO;
        String sql = "SELECT SUM(monto) FROM movimientos_caja WHERE tipo = 'Ingreso' AND id_turno = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, turnoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getBigDecimal(1);
                    if (resultado == null) resultado = BigDecimal.ZERO;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ingresos del turno: " + e.getMessage());
        }

        return resultado;
    }

    public BigDecimal obtenerEgresos(int turnoId) {
        BigDecimal resultado = BigDecimal.ZERO;
        String sql = "SELECT SUM(monto) FROM movimientos_caja WHERE tipo = 'Egreso' AND id_turno = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, turnoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getBigDecimal(1);
                    if (resultado == null) resultado = BigDecimal.ZERO;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener egresos del turno: " + e.getMessage());
        }

        return resultado;
    }

    public BigDecimal obtenerGanancia(int turnoId) {
        String sql = "SELECT ganancia FROM turnos WHERE id = ?";
        return obtenerDecimalPorConsulta(sql, turnoId);
    }

    private BigDecimal obtenerDecimalPorConsulta(String sql, int turnoId) {
        BigDecimal resultado = BigDecimal.ZERO;
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turnoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getBigDecimal(1);
                    if (resultado == null) resultado = BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
