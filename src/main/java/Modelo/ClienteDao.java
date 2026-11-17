
package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * DAO para administrar clientes de Vhao PDV.
 * Compatible con Java 8.
 */
public class ClienteDao {

    Conexion cn = new Conexion();

    // ---------------------------------------------------------
    // REGISTRAR CLIENTE
    // ---------------------------------------------------------
    public boolean RegistrarCliente(Cliente cl) {

        String sql = "INSERT INTO clientes "
                   + "(dni, nombre, telefono, direccion, id_empresa, activo) "
                   + "VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, cl.getDni());
            p.setString(2, cl.getNombre());
            p.setString(3, cl.getTelefono());
            p.setString(4, cl.getDireccion());
            p.setInt(5, cl.getIdEmpresa());

            return p.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
            return false;
        }
    }

    // ---------------------------------------------------------
    // LISTAR CLIENTES ACTIVOS
    // ---------------------------------------------------------
    public List<Cliente> ListarCliente(int idEmpresa) {

        List<Cliente> ListaCl = new ArrayList<>();

        String sql = "SELECT * FROM clientes "
                   + "WHERE id_empresa = ? AND activo = 1 "
                   + "ORDER BY nombre ASC";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idEmpresa);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    Cliente cl = new Cliente();

                    cl.setId(r.getInt("id"));
                    cl.setDni(r.getString("dni"));
                    cl.setNombre(r.getString("nombre"));
                    cl.setTelefono(r.getString("telefono"));
                    cl.setDireccion(r.getString("direccion"));
                    cl.setIdEmpresa(r.getInt("id_empresa"));

                    ListaCl.add(cl);
                }
            }

        } catch (SQLException e) {
            System.out.println("[CLIENTE] Error al listar: " + e.toString());
        }

        return ListaCl;
    }

    // ---------------------------------------------------------
    // VERIFICAR SI SE PUEDE DESACTIVAR
    // null = se puede desactivar.
    // Si no, devuelve el motivo.
    // ---------------------------------------------------------
    public String motivoNoEliminar(int idCliente) {

        String sqlCliente = "SELECT id FROM clientes "
                          + "WHERE id = ?";

        String sqlCredito = "SELECT COUNT(*) "
                          + "FROM detalle_creditocliente d "
                          + "INNER JOIN clientes c "
                          + "ON c.dni = d.dni "
                          + "AND c.id_empresa = d.id_empresa "
                          + "WHERE c.id = ? "
                          + "AND COALESCE(d.pagado, 0) = 0";

        try (Connection c = cn.getConnection()) {

            // Verificar que el cliente exista
            try (PreparedStatement p = c.prepareStatement(sqlCliente)) {

                p.setInt(1, idCliente);

                try (ResultSet r = p.executeQuery()) {

                    if (!r.next()) {
                        return "El cliente no existe.";
                    }
                }
            }

            // Proteger Cliente Mostrador
            if (idCliente == 1) {
                return "El \"Cliente Mostrador\" es del sistema "
                     + "y no se puede desactivar.";
            }

            // Verificar créditos pendientes
            try (PreparedStatement p = c.prepareStatement(sqlCredito)) {

                p.setInt(1, idCliente);

                try (ResultSet r = p.executeQuery()) {

                    if (r.next() && r.getInt(1) > 0) {

                        return "Tiene un crédito activo.\n"
                             + "Liquida su saldo desde Crédito Cliente "
                             + "antes de desactivar al cliente.";
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al verificar: " + e.toString()
            );

            return "No se pudo verificar el estado del cliente. "
                 + "Intenta de nuevo.";
        }

        return null;
    }

    // ---------------------------------------------------------
    // DESACTIVAR CLIENTE (ELIMINACIÓN LÓGICA)
    // ---------------------------------------------------------
    public boolean EliminarCliente(int id) {

        // Revalidar que se pueda desactivar
        String motivo = motivoNoEliminar(id);

        if (motivo != null) {

            System.out.println(
                    "[CLIENTE] Desactivación bloqueada (id="
                    + id + "): " + motivo
            );

            return false;
        }

        String sql = "UPDATE clientes "
                   + "SET activo = 0 "
                   + "WHERE id = ? AND activo = 1";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            return p.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al desactivar: " + e.toString()
            );

            return false;
        }
    }

    // ---------------------------------------------------------
    // REACTIVAR CLIENTE
    // ---------------------------------------------------------
    public boolean ReactivarCliente(int id, int idEmpresa) {

        String sql = "UPDATE clientes "
                   + "SET activo = 1 "
                   + "WHERE id = ? AND id_empresa = ? AND activo = 0";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);
            p.setInt(2, idEmpresa);

            return p.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al reactivar: " + e.toString()
            );

            return false;
        }
    }

    // ---------------------------------------------------------
    // LISTAR CLIENTES INACTIVOS
    // ---------------------------------------------------------
    public List<Cliente> ListarClientesInactivos(int idEmpresa) {

        List<Cliente> lista = new ArrayList<>();

        String sql = "SELECT * FROM clientes "
                   + "WHERE id_empresa = ? AND activo = 0 "
                   + "ORDER BY nombre ASC";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idEmpresa);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    Cliente cl = new Cliente();

                    cl.setId(r.getInt("id"));
                    cl.setDni(r.getString("dni"));
                    cl.setNombre(r.getString("nombre"));
                    cl.setTelefono(r.getString("telefono"));
                    cl.setDireccion(r.getString("direccion"));
                    cl.setIdEmpresa(r.getInt("id_empresa"));

                    lista.add(cl);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al listar inactivos: " + e.toString()
            );
        }

        return lista;
    }

    // ---------------------------------------------------------
    // MODIFICAR CLIENTE
    // ---------------------------------------------------------
    public boolean ModificarCliente(Cliente cl) {

        String sql = "UPDATE clientes "
                   + "SET dni = ?, nombre = ?, telefono = ?, direccion = ? "
                   + "WHERE id = ? AND id_empresa = ? AND activo = 1";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, cl.getDni());
            p.setString(2, cl.getNombre());
            p.setString(3, cl.getTelefono());
            p.setString(4, cl.getDireccion());
            p.setInt(5, cl.getId());
            p.setInt(6, cl.getIdEmpresa());

            return p.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al modificar: " + e.toString()
            );

            return false;
        }
    }

    // ---------------------------------------------------------
    // BUSCAR CLIENTES ACTIVOS
    // ---------------------------------------------------------
    public List<Cliente> BuscarClientes(String texto, int idEmpresa) {

        List<Cliente> lista = new ArrayList<>();

        String sql = "SELECT * FROM clientes "
                   + "WHERE id_empresa = ? "
                   + "AND activo = 1 "
                   + "AND (CAST(id AS CHAR) LIKE ? "
                   + "OR dni LIKE ? "
                   + "OR nombre LIKE ?) "
                   + "ORDER BY nombre ASC";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            String filtro = "%" + texto.trim() + "%";

            p.setInt(1, idEmpresa);
            p.setString(2, filtro);
            p.setString(3, filtro);
            p.setString(4, filtro);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    Cliente cl = new Cliente();

                    cl.setId(r.getInt("id"));
                    cl.setDni(r.getString("dni"));
                    cl.setNombre(r.getString("nombre"));
                    cl.setTelefono(r.getString("telefono"));
                    cl.setDireccion(r.getString("direccion"));
                    cl.setIdEmpresa(r.getInt("id_empresa"));

                    lista.add(cl);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al buscar: " + e.toString()
            );
        }

        return lista;
    }

    // ---------------------------------------------------------
    // BUSCAR CLIENTE POR DNI
    // SOLAMENTE CLIENTES ACTIVOS
    // ---------------------------------------------------------
    public Cliente BuscarCliente(int dni, int idEmpresa) {

        Cliente cliente = new Cliente();

        String sql = "SELECT * FROM clientes "
                   + "WHERE dni = ? "
                   + "AND id_empresa = ? "
                   + "AND activo = 1";

        try (Connection c = cn.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, dni);
            p.setInt(2, idEmpresa);

            try (ResultSet r = p.executeQuery()) {

                if (r.next()) {

                    cliente.setId(r.getInt("id"));
                    cliente.setDni(r.getString("dni"));
                    cliente.setNombre(r.getString("nombre"));
                    cliente.setTelefono(r.getString("telefono"));
                    cliente.setDireccion(r.getString("direccion"));
                    cliente.setIdEmpresa(r.getInt("id_empresa"));
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[CLIENTE] Error al buscar por DNI: " + e.toString()
            );
        }

        return cliente;
    }
    
    //recuperar el ID autogenerado sin convertir el DNI a número.
    
public Cliente BuscarClientePorDni(String dni, int idEmpresa) {

    String sql = "SELECT * FROM clientes WHERE dni = ? AND id_empresa = ?";

    try (Connection c = cn.getConnection();
         PreparedStatement p = c.prepareStatement(sql)) {

        p.setString(1, dni);
        p.setInt(2, idEmpresa);

        try (ResultSet r = p.executeQuery()) {

            if (r.next()) {

                Cliente cl = new Cliente();

                cl.setId(r.getInt("id"));
                cl.setDni(r.getString("dni"));
                cl.setNombre(r.getString("nombre"));
                cl.setTelefono(r.getString("telefono"));
                cl.setDireccion(r.getString("direccion"));
                cl.setIdEmpresa(r.getInt("id_empresa"));

                return cl;
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}


}
