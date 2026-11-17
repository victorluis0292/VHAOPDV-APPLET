package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductosDao {
    Conexion cn = new Conexion();

    // Registrar productos incluyendo id_empresa, categoria, id_catalogo_global e id_categoria
    public boolean RegistrarProductos(Productos pro) {
        String sql = "INSERT INTO productos (codigo, nombre, categoria, proveedor, stock, precio, preciocompra, id_empresa, imagen_url, id_catalogo_global, id_categoria) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pro.getCodigo());
            ps.setString(2, pro.getNombre());
            ps.setString(3, pro.getCategoria());
            ps.setInt(4, pro.getProveedor());
            ps.setInt(5, pro.getStock());
            ps.setDouble(6, pro.getPrecio());
            ps.setDouble(7, pro.getPreciocompra());
            ps.setInt(8, pro.getId_empresa());
            ps.setString(9, pro.getImagenUrl());

            if (pro.getIdCatalogoGlobal() != null) {
                ps.setInt(10, pro.getIdCatalogoGlobal());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            if (pro.getIdCategoria() != null) {
                ps.setInt(11, pro.getIdCategoria());
            } else {
                ps.setNull(11, Types.INTEGER);
            }

            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error en RegistrarProductos: " + e.toString());
            return false;
        }
    }

    // Listar productos filtrando por id_empresa
    public List<Productos> ListarProductos(int idEmpresa) {
        List<Productos> Listapro = new ArrayList<>();
        String sql = "SELECT pr.id AS id_proveedor, pr.nombre AS nombre_proveedor, p.* " +
                     "FROM proveedor pr INNER JOIN productos p ON pr.id = p.proveedor " +
                     "WHERE p.id_empresa = ? ORDER BY p.id DESC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Productos pro = new Productos();
                    pro.setId(rs.getInt("id"));
                    pro.setCodigo(rs.getString("codigo"));
                    pro.setNombre(rs.getString("nombre"));
                    pro.setCategoria(rs.getString("categoria")); 
                    pro.setProveedor(rs.getInt("id_proveedor"));
                    pro.setProveedorPro(rs.getString("nombre_proveedor"));
                    pro.setStock(rs.getInt("stock"));
                    pro.setPrecio(rs.getDouble("precio"));
                    pro.setPrecioKg(pro.getPrecio()); 
                    pro.setPreciocompra(rs.getDouble("preciocompra"));
                    pro.setId_empresa(idEmpresa);
                    pro.setImagenUrl(rs.getString("imagen_url")); // ✅ ¡Agregado aquí!

                    int idCatGlobal = rs.getInt("id_catalogo_global");
                    pro.setIdCatalogoGlobal(rs.wasNull() ? null : idCatGlobal); // ✅ Vínculo con catalogo_global

                    int idCategoria = rs.getInt("id_categoria");
                    pro.setIdCategoria(rs.wasNull() ? null : idCategoria); // ✅ Vínculo con categorias

                    Listapro.add(pro);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en ListarProductos: " + e.toString());
        }
        return Listapro;
    }

    // Modificar producto incluyendo id_empresa, categoria, id_catalogo_global e id_categoria
   public boolean ModificarProductos(Productos pro) {
        String sql = "UPDATE productos SET codigo=?, nombre=?, categoria=?, proveedor=?, stock=?, precio=?, preciocompra=?, id_empresa=?, imagen_url=?, id_catalogo_global=?, id_categoria=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pro.getCodigo());
            ps.setString(2, pro.getNombre());
            ps.setString(3, pro.getCategoria());
            ps.setInt(4, pro.getProveedor());
            ps.setInt(5, pro.getStock());
            ps.setDouble(6, pro.getPrecio());
            ps.setDouble(7, pro.getPreciocompra());
            ps.setInt(8, pro.getId_empresa());
            ps.setString(9, pro.getImagenUrl()); 

            if (pro.getIdCatalogoGlobal() != null) {
                ps.setInt(10, pro.getIdCatalogoGlobal());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            if (pro.getIdCategoria() != null) {
                ps.setInt(11, pro.getIdCategoria());
            } else {
                ps.setNull(11, Types.INTEGER);
            }

            ps.setInt(12, pro.getId());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al modificar producto: " + e.getMessage());
            return false;
        }
    }

    // Buscar producto por código filtrando por id_empresa
    public Productos BuscarPro(String cod, int idEmpresa) {
        Productos producto = new Productos();
        String sql = "SELECT * FROM productos WHERE codigo = ? AND id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cod);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    producto.setId(rs.getInt("id"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setCategoria(rs.getString("categoria")); 
                    producto.setPrecio(rs.getDouble("precio"));
                    producto.setPrecioKg(producto.getPrecio());
                    producto.setPreciocompra(rs.getDouble("preciocompra"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setId_empresa(idEmpresa);
                    producto.setImagenUrl(rs.getString("imagen_url")); // ✅ ¡Agregado aquí!

                    int idCatGlobal = rs.getInt("id_catalogo_global");
                    producto.setIdCatalogoGlobal(rs.wasNull() ? null : idCatGlobal); // ✅ Vínculo con catalogo_global

                    int idCategoria = rs.getInt("id_categoria");
                    producto.setIdCategoria(rs.wasNull() ? null : idCategoria); // ✅ Vínculo con categorias
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en BuscarPro: " + e.toString());
        }
        return producto;
    }

    // Buscar producto por ID filtrando por id_empresa
    public Productos BuscarId(int id, int idEmpresa) {
        Productos pro = new Productos();
        String sql = "SELECT pr.id AS id_proveedor, pr.nombre AS nombre_proveedor, p.* " +
                     "FROM proveedor pr INNER JOIN productos p ON p.proveedor = pr.id " +
                     "WHERE p.id = ? AND p.id_empresa = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pro.setId(rs.getInt("id"));
                    pro.setCodigo(rs.getString("codigo"));
                    pro.setNombre(rs.getString("nombre"));
                    pro.setCategoria(rs.getString("categoria")); 
                    pro.setProveedor(rs.getInt("proveedor"));
                    pro.setProveedorPro(rs.getString("nombre_proveedor"));
                    pro.setStock(rs.getInt("stock"));
                    pro.setPrecio(rs.getDouble("precio"));
                    pro.setPrecioKg(pro.getPrecio());
                    pro.setPreciocompra(rs.getDouble("preciocompra"));
                    pro.setId_empresa(idEmpresa);
                    pro.setImagenUrl(rs.getString("imagen_url")); // ✅ ¡Agregado aquí!

                    int idCatGlobal = rs.getInt("id_catalogo_global");
                    pro.setIdCatalogoGlobal(rs.wasNull() ? null : idCatGlobal); // ✅ Vínculo con catalogo_global

                    int idCategoria = rs.getInt("id_categoria");
                    pro.setIdCategoria(rs.wasNull() ? null : idCategoria); // ✅ Vínculo con categorias
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en BuscarId: " + e.toString());
        }
        return pro;
    }

    // Eliminar productos
    public boolean EliminarProductos(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error en EliminarProductos: " + e.toString());
            return false;
        }
    }
    public int obtenerSiguienteId() {
    String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM productos";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        Utilidades.LogUtil.error("Error al obtener siguiente id de producto", e);
    }
    return -1;
}
}