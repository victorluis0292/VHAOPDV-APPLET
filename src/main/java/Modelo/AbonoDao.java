package Modelo;

import Vista.Sistema;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbonoDao {

    /**
     * Registra un abono en la base de datos.
     */
    public boolean registrarAbono(int idVenta, double monto, String nota, String tipoPago, int dniCliente) {
        String sql = "INSERT INTO abonos_credito " +
                     "(id_venta, fecha, monto, tipo_pago, nota, dni, aplicado, id_empresa) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setObject(2, LocalDateTime.now(ZoneId.of("America/Monterrey")));
            ps.setDouble(3, monto);
            ps.setString(4, tipoPago);
            ps.setString(5, nota);
            ps.setInt(6, dniCliente);

            // Empresa activa
            ps.setInt(7, Sistema.getIdEmpresaActiva());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar abono: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marca los abonos como aplicados y los vincula
     * con el folio de la liquidación.
     *
     * Conserva el id_venta original del abono.
     */
    public boolean actualizarAbonosAplicados(int dni, int idVenta) {

        String sql = "UPDATE abonos_credito " +
                     "SET aplicado = 1, id_venta_abono = ? " +
                     "WHERE dni = ? " +
                     "AND aplicado = 0 " +
                     "AND id_empresa = ? " +
                     "AND id_venta_abono IS NULL";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setInt(2, dni);
            ps.setInt(3, Sistema.getIdEmpresaActiva());

            int actualizados = ps.executeUpdate();

            System.out.println("Abonos vinculados a liquidación: " + actualizados);
            System.out.println("Folio liquidación: " + idVenta);

            return actualizados >= 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar abonos aplicados: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los abonos de una venta filtrando por empresa
     */
    public List<Abono> obtenerAbonosPorVenta(int idVenta) {
        List<Abono> abonos = new ArrayList<>();
        String sql = "SELECT monto, fecha, " +
             "id_venta_abono AS id_venta, id_empresa, aplicado " +
             "FROM abonos_credito " +
             "WHERE id_venta_abono = ? " +
             "AND aplicado = 1 " +
             "AND id_empresa = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, Sistema.getIdEmpresaActiva());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double monto = rs.getDouble("monto");
                    Date fecha = rs.getDate("fecha");
                    int idEmp = rs.getInt("id_empresa");
                    int idV = rs.getInt("id_venta");
                    boolean aplicado = rs.getBoolean("aplicado");

                    abonos.add(new Abono(monto, fecha, idV, idEmp, aplicado));
                    System.out.println("Abono encontrado: Monto=" + monto + ", Fecha=" + fecha);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener abonos: " + e.getMessage());
        }
        System.out.println("Total abonos encontrados para venta " + idVenta + ": " + abonos.size());
        return abonos;
    }

    /**
     * Cierra los productos de un cliente que ya quedaron liquidados con abonos
     * (total - abonos <= 0): los marca pagado y marca sus abonos como aplicados.
     * No borra nada, para que el Corte Ventas conserve el proveedor de cada abono.
     */
    public boolean cerrarCreditoLiquidado(String dni) {
        int idEmpresa = Sistema.getIdEmpresaActiva();

        String sqlDetalle =
            "UPDATE detalle_creditocliente d SET d.pagado = 1 " +
            "WHERE d.dni = ? AND d.id_empresa = ? AND COALESCE(d.pagado, 0) = 0 " +
            "AND ROUND(d.total - IFNULL((SELECT SUM(a.monto) FROM abonos_credito a " +
            "    WHERE a.id_detalle = d.id AND a.aplicado = 0), 0), 2) <= 0";

        String sqlAbonos =
            "UPDATE abonos_credito a " +
            "JOIN detalle_creditocliente d ON d.id = a.id_detalle " +
            "SET a.aplicado = 1 " +
            "WHERE d.dni = ? AND d.id_empresa = ? AND d.pagado = 1 AND a.aplicado = 0";

        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sqlDetalle);
                 PreparedStatement ps2 = con.prepareStatement(sqlAbonos)) {

                ps1.setString(1, dni);
                ps1.setInt(2, idEmpresa);
                ps1.executeUpdate();

                ps2.setString(1, dni);
                ps2.setInt(2, idEmpresa);
                ps2.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar crédito liquidado: " + e.getMessage());
            return false;
        }
    }
}