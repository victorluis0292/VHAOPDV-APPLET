package Modelo;

import java.sql.*;
import java.util.List;

public class AbonoProductoDao {

    // Datos para reimprimir el ticket de un abono
    public static class InfoAbono {
        public String concepto;
        public double monto, precio, restante;
    }

    // Guarda el abono ligado a su venta y a su producto
    public boolean registrar(int idVenta, double monto, String nota, int dni, int idEmpresa, int idDetalle) {
        String sql = "INSERT INTO abonos_credito "
                   + "(id_venta, fecha, monto, tipo_pago, nota, dni, id_empresa, aplicado, id_detalle) "
                   + "VALUES (?, NOW(), ?, 'ABONO', ?, ?, ?, 0, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setDouble(2, monto);
            ps.setString(3, nota);
            ps.setInt(4, dni);
            ps.setInt(5, idEmpresa);
            ps.setInt(6, idDetalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Abono por categoría: una fila por producto, todas en una sola transacción
    public boolean registrarVarios(int idVenta, List<double[]> partes, String nota,
                                   int dni, int idEmpresa) {
        String sql = "INSERT INTO abonos_credito "
                   + "(id_venta, fecha, monto, tipo_pago, nota, dni, id_empresa, aplicado, id_detalle) "
                   + "VALUES (?, NOW(), ?, 'ABONO_CAT', ?, ?, ?, 0, ?)";
        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (double[] parte : partes) {          // parte = {idDetalle, monto}
                    ps.setInt(1, idVenta);
                    ps.setDouble(2, parte[1]);
                    ps.setString(3, nota);
                    ps.setInt(4, dni);
                    ps.setInt(5, idEmpresa);
                    ps.setInt(6, (int) parte[0]);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Compensación: si el abono no se pudo guardar, se quita la venta recién creada
    public void eliminarVenta(int idVenta) {
        String sql = "DELETE FROM ventas WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Datos para reimprimir el ticket de un abono; null si el folio no es un abono
    public InfoAbono obtenerInfoAbono(int idVenta) {
        String sqlAb = "SELECT id, id_detalle, monto, dni, id_empresa, tipo_pago "
                     + "FROM abonos_credito WHERE id_venta = ? ORDER BY id";
        try (Connection con = Conexion.getConnection()) {
            double monto = 0;
            long maxId = 0;
            int dni = 0, idEmpresa = 0, idDetalle = 0, n = 0;
            String tipo = "";

            try (PreparedStatement ps = con.prepareStatement(sqlAb)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (n == 0) {
                            idDetalle = rs.getInt("id_detalle");
                            dni = rs.getInt("dni");
                            idEmpresa = rs.getInt("id_empresa");
                            tipo = rs.getString("tipo_pago");
                        }
                        monto += rs.getDouble("monto");
                        maxId = Math.max(maxId, rs.getLong("id"));
                        n++;
                    }
                }
            }
            if (n == 0) return null;

            String nombre = null, categoria = null;
            String sqlDet = "SELECT d.nombre, p.categoria FROM detalle_creditocliente d "
                          + "LEFT JOIN productos p ON p.id = d.id_pro WHERE d.id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDet)) {
                ps.setInt(1, idDetalle);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;      // el crédito ya se cobró y se borró
                    nombre = rs.getString("nombre");
                    categoria = rs.getString("categoria");
                }
            }

            boolean esCategoria = "ABONO_CAT".equalsIgnoreCase(tipo);
            double total = 0, abonado = 0;

            if (esCategoria) {
                String sql = "SELECT IFNULL(SUM(d.total), 0) AS total, IFNULL(SUM(ab.abonado), 0) AS abonado "
                           + "FROM detalle_creditocliente d JOIN productos p ON p.id = d.id_pro "
                           + "LEFT JOIN (SELECT id_detalle, SUM(monto) AS abonado FROM abonos_credito "
                           + "           WHERE id <= ? GROUP BY id_detalle) ab ON ab.id_detalle = d.id "
                           + "WHERE d.dni = ? AND d.id_empresa = ? AND p.categoria = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, maxId);
                    ps.setInt(2, dni);
                    ps.setInt(3, idEmpresa);
                    ps.setString(4, categoria);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            total = rs.getDouble("total");
                            abonado = rs.getDouble("abonado");
                        }
                    }
                }
            } else {
                String sql = "SELECT d.total, IFNULL((SELECT SUM(monto) FROM abonos_credito "
                           + "WHERE id_detalle = d.id AND id <= ?), 0) AS abonado "
                           + "FROM detalle_creditocliente d WHERE d.id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, maxId);
                    ps.setInt(2, idDetalle);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            total = rs.getDouble("total");
                            abonado = rs.getDouble("abonado");
                        }
                    }
                }
            }

            InfoAbono info = new InfoAbono();
            info.monto = monto;
            info.concepto = esCategoria ? "Categoría " + categoria : nombre;
            info.precio = total;
            info.restante = Math.max(0, total - abonado);
            return info;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}