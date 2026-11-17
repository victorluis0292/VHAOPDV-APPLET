package Modelo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CashInBoxDAO {

    // Registra saldo inicial solo si NO hay un turno abierto para el usuario y la empresa
 public boolean registrarSaldoInicial(CashInBox cash, int idEmpresa) {
    String sql = "INSERT INTO initialcashbalance (amount, usuario, fecha, id_empresa) VALUES (?, ?, NOW(), ?)";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setBigDecimal(1, cash.getAmount());
        ps.setString(2, cash.getUsuario());
        ps.setInt(3, idEmpresa);
        int rows = ps.executeUpdate();
        return rows > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


    // Verifica si existe saldo inicial para el día actual en la empresa
    public boolean existeSaldoHoy(int idEmpresa) {
        String sql = "SELECT COUNT(*) FROM initialcashbalance WHERE DATE(fecha) = CURDATE() AND id_empresa=?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtiene saldo inicial del día (último registro) filtrado por empresa
    public BigDecimal obtenerSaldoInicialDelDia(int idEmpresa) {
        String sql = "SELECT amount FROM initialcashbalance WHERE DATE(fecha) = CURDATE() AND id_empresa=? ORDER BY id DESC LIMIT 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("amount");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener saldo inicial del día: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal obtenerSaldoInicialSemana(int anio, int semana, int idEmpresa) {
        String sql = "SELECT amount FROM initialcashbalance " +
                     "WHERE YEAR(fecha) = ? AND WEEK(fecha, 1) = ? AND id_empresa=? ORDER BY fecha ASC LIMIT 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, semana);
            ps.setInt(3, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("amount");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener saldo inicial de la semana: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal obtenerSaldoInicialMes(int anio, int mes, int idEmpresa) {
        String sql = "SELECT amount FROM initialcashbalance " +
                     "WHERE YEAR(fecha) = ? AND MONTH(fecha) = ? AND id_empresa=? ORDER BY fecha ASC LIMIT 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, mes);
            ps.setInt(3, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("amount");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener saldo inicial del mes: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal obtenerSaldoInicialAnio(int anio, int idEmpresa) {
        String sql = "SELECT amount FROM initialcashbalance " +
                     "WHERE YEAR(fecha) = ? AND id_empresa=? ORDER BY fecha ASC LIMIT 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("amount");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener saldo inicial del año: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal obtenerSaldoInicialRango(String fechaInicio, String fechaFin, int idEmpresa) {
        String sql = "SELECT amount FROM initialcashbalance " +
                     "WHERE DATE(fecha) BETWEEN ? AND ? AND id_empresa=? ORDER BY fecha ASC LIMIT 1";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("amount");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener saldo inicial del rango: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    // Registra el saldo inicial ligado a un turno
public boolean registrarSaldoInicial(CashInBox cash, int idEmpresa, int idTurno) {
    String sql = "INSERT INTO initialcashbalance (amount, usuario, fecha, id_empresa, id_turno) VALUES (?, ?, NOW(), ?, ?)";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setBigDecimal(1, cash.getAmount());
        ps.setString(2, cash.getUsuario());
        ps.setInt(3, idEmpresa);
        ps.setInt(4, idTurno);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// Saldo inicial de un turno específico
public BigDecimal obtenerSaldoInicialPorTurno(int idTurno) {
    String sql = "SELECT amount FROM initialcashbalance WHERE id_turno = ? ORDER BY id DESC LIMIT 1";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idTurno);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal("amount");
        }
    } catch (SQLException e) {
        System.out.println("Error al obtener saldo inicial del turno: " + e.getMessage());
    }
    return BigDecimal.ZERO;
}
    
}
