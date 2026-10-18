package Modelo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromocionDao {

    private final Conexion cn = new Conexion();

    // ------------------------------------------------------------
    // GUARDAR (promoción + sus productos en una sola transacción)
    // ------------------------------------------------------------
    public boolean guardar(Promocion p) {
        String sql = "INSERT INTO promociones (id_empresa, descripcion, cantidad_requerida, precio_paquete, "
                   + "fecha_inicio, fecha_fin, activa) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection con = cn.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getIdEmpresa());
                ps.setString(2, p.getDescripcion());
                ps.setInt(3, p.getCantidadRequerida());
                ps.setDouble(4, p.getPrecioPaquete());
                ps.setDate(5, Date.valueOf(p.getFechaInicio()));
                ps.setDate(6, Date.valueOf(p.getFechaFin()));
                ps.executeUpdate();

                int id = 0;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) id = rs.getInt(1);
                }
                if (id == 0) throw new SQLException("No se generó el id de la promoción");

                insertarProductos(con, id, p.getIdProductos());
                con.commit();
                p.setId(id);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error en PromocionDao.guardar: " + e);
            return false;
        }
    }

    // ------------------------------------------------------------
    // ACTUALIZAR
    // ------------------------------------------------------------
    public boolean actualizar(Promocion p) {
        String sql = "UPDATE promociones SET descripcion = ?, cantidad_requerida = ?, precio_paquete = ?, "
                   + "fecha_inicio = ?, fecha_fin = ?, activa = ? WHERE id = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getDescripcion());
                ps.setInt(2, p.getCantidadRequerida());
                ps.setDouble(3, p.getPrecioPaquete());
                ps.setDate(4, Date.valueOf(p.getFechaInicio()));
                ps.setDate(5, Date.valueOf(p.getFechaFin()));
                ps.setInt(6, p.isActiva() ? 1 : 0);
                ps.setInt(7, p.getId());
                ps.setInt(8, p.getIdEmpresa());
                if (ps.executeUpdate() == 0) throw new SQLException("Promoción no encontrada");

                try (PreparedStatement del = con.prepareStatement(
                        "DELETE FROM promocion_productos WHERE id_promocion = ?")) {
                    del.setInt(1, p.getId());
                    del.executeUpdate();
                }
                insertarProductos(con, p.getId(), p.getIdProductos());
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error en PromocionDao.actualizar: " + e);
            return false;
        }
    }

    private void insertarProductos(Connection con, int idPromo, List<Integer> ids) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO promocion_productos (id_promocion, id_producto) VALUES (?, ?)")) {
            for (int idProducto : ids) {
                ps.setInt(1, idPromo);
                ps.setInt(2, idProducto);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ------------------------------------------------------------
    // ELIMINAR
    // Devuelve: 1 = eliminada, 2 = desactivada (ya tenía ventas), 0 = error
    // ------------------------------------------------------------
    public int eliminar(int idPromo, int idEmpresa) {
        try (Connection con = cn.getConnection()) {
            int usos = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM venta_promociones WHERE id_promocion = ?")) {
                ps.setInt(1, idPromo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) usos = rs.getInt(1);
                }
            }

            if (usos > 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE promociones SET activa = 0 WHERE id = ? AND id_empresa = ?")) {
                    ps.setInt(1, idPromo);
                    ps.setInt(2, idEmpresa);
                    return ps.executeUpdate() > 0 ? 2 : 0;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM promociones WHERE id = ? AND id_empresa = ?")) {
                ps.setInt(1, idPromo);
                ps.setInt(2, idEmpresa);
                return ps.executeUpdate() > 0 ? 1 : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en PromocionDao.eliminar: " + e);
            return 0;
        }
    }

    // ------------------------------------------------------------
    // LISTAR
    // ------------------------------------------------------------
    public List<Promocion> listar(int idEmpresa) {
        return consultar(idEmpresa, null);
    }

    public List<Promocion> listarVigentes(int idEmpresa, LocalDate fecha) {
        return consultar(idEmpresa, fecha);
    }

    private List<Promocion> consultar(int idEmpresa, LocalDate soloVigentesEn) {
        List<Promocion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
              "SELECT p.id, p.id_empresa, p.descripcion, p.cantidad_requerida, p.precio_paquete, "
            + "p.fecha_inicio, p.fecha_fin, p.activa, "
            + "GROUP_CONCAT(pp.id_producto) AS ids, GROUP_CONCAT(pr.nombre SEPARATOR ', ') AS nombres "
            + "FROM promociones p "
            + "LEFT JOIN promocion_productos pp ON pp.id_promocion = p.id "
            + "LEFT JOIN productos pr ON pr.id = pp.id_producto "
            + "WHERE p.id_empresa = ? ");
        if (soloVigentesEn != null) {
            sql.append("AND p.activa = 1 AND ? BETWEEN p.fecha_inicio AND p.fecha_fin ");
        }
        sql.append("GROUP BY p.id, p.id_empresa, p.descripcion, p.cantidad_requerida, p.precio_paquete, "
                 + "p.fecha_inicio, p.fecha_fin, p.activa ORDER BY p.id DESC");

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, idEmpresa);
            if (soloVigentesEn != null) ps.setDate(2, Date.valueOf(soloVigentesEn));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Promocion p = new Promocion();
                    p.setId(rs.getInt("id"));
                    p.setIdEmpresa(rs.getInt("id_empresa"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCantidadRequerida(rs.getInt("cantidad_requerida"));
                    p.setPrecioPaquete(rs.getDouble("precio_paquete"));
                    p.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    p.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
                    p.setActiva(rs.getInt("activa") == 1);

                    List<Integer> ids = new ArrayList<>();
                    String idsTxt = rs.getString("ids");
                    if (idsTxt != null && !idsTxt.isEmpty()) {
                        for (String s : idsTxt.split(",")) ids.add(Integer.parseInt(s.trim()));
                    }
                    p.setIdProductos(ids);

                    String nombres = rs.getString("nombres");
                    p.setNombresProductos(nombres == null ? "" : nombres);
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en PromocionDao.consultar: " + e);
        }
        return lista;
    }

    // ------------------------------------------------------------
    // VALIDACIÓN: un producto solo puede estar en una promo activa por fechas
    // Devuelve la descripción de la promo que choca, o null si no hay choque
    // ------------------------------------------------------------
    public String promoEnConflicto(int idEmpresa, int idProducto, LocalDate ini, LocalDate fin, int excluirId) {
        String sql = "SELECT p.descripcion FROM promociones p "
                   + "JOIN promocion_productos pp ON pp.id_promocion = p.id "
                   + "WHERE p.id_empresa = ? AND pp.id_producto = ? AND p.activa = 1 AND p.id <> ? "
                   + "AND p.fecha_inicio <= ? AND p.fecha_fin >= ? LIMIT 1";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setInt(2, idProducto);
            ps.setInt(3, excluirId);
            ps.setDate(4, Date.valueOf(fin));
            ps.setDate(5, Date.valueOf(ini));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("Error en PromocionDao.promoEnConflicto: " + e);
        }
        return null;
    }
}