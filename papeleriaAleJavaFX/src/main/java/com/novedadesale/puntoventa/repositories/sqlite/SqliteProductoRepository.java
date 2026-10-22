package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.ProductoRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteProductoRepository implements ProductoRepository {

    private static final String SELECT_BASE =
            "SELECT p.id, p.codigo, p.nombre, p.categoria_id, " +
            "COALESCE(c.nombre, '') AS categoria, p.proveedor_id, " +
            "COALESCE(pr.nombre, '') AS proveedor, p.precio, p.stock, p.stock_minimo, p.imagen " +
            "FROM productos p LEFT JOIN categorias c ON c.id = p.categoria_id " +
            "LEFT JOIN proveedores pr ON pr.id = p.proveedor_id ";

    private Producto mapear(ResultSet rs) throws SQLException {
        int proveedorId = rs.getInt("proveedor_id");
        Integer proveedorIdNulo = rs.wasNull() ? null : proveedorId;
        return new Producto(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getInt("categoria_id"),
                rs.getString("categoria"),
                proveedorIdNulo,
                rs.getString("proveedor"),
                SqlUtil.bd(rs.getString("precio")),
                rs.getInt("stock"),
                rs.getInt("stock_minimo"),
                rs.getString("imagen"));
    }

    @Override
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY p.codigo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar productos", e);
        }
    }

    @Override
    public Optional<Producto> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar producto " + id, e);
        }
    }

    @Override
    public Optional<Producto> porCodigo(String codigo) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE LOWER(p.codigo) = LOWER(?)")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar producto por código", e);
        }
    }

    @Override
    public List<Producto> buscar(String texto) {
        String q = texto == null ? "" : texto.trim();
        String sql = SELECT_BASE + "WHERE LOWER(p.nombre) LIKE LOWER(?) OR LOWER(p.codigo) LIKE LOWER(?) ORDER BY p.codigo";
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
            ps.setString(1, "%" + q + "%");
            ps.setString(2, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar productos", e);
        }
    }

    @Override
    public List<Producto> stockBajo() {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                SELECT_BASE + "WHERE p.stock <= p.stock_minimo ORDER BY p.stock");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar stock bajo", e);
        }
    }

    @Override
    public Producto guardar(Producto producto) {
        try {
            if (producto.id() <= 0) {
                String sql = "INSERT INTO productos (codigo, nombre, categoria_id, proveedor_id, imagen, precio, stock, stock_minimo) VALUES (?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, producto.codigo());
                    ps.setString(2, producto.nombre());
                    ps.setInt(3, producto.categoriaId());
                    enlazarProveedor(ps, 4, producto.proveedorId());
                    ps.setString(5, producto.imagen());
                    ps.setString(6, SqlUtil.txt(producto.precio()));
                    ps.setInt(7, producto.stock());
                    ps.setInt(8, producto.stockMinimo());
                    ps.executeUpdate();
                    int id = SqlUtil.claveGenerada(ps);
                    return porId(id).orElse(producto);
                }
            }
            String sql = "UPDATE productos SET codigo = ?, nombre = ?, categoria_id = ?, proveedor_id = ?, imagen = ?, precio = ?, stock = ?, stock_minimo = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                ps.setString(1, producto.codigo());
                ps.setString(2, producto.nombre());
                ps.setInt(3, producto.categoriaId());
                enlazarProveedor(ps, 4, producto.proveedorId());
                ps.setString(5, producto.imagen());
                ps.setString(6, SqlUtil.txt(producto.precio()));
                ps.setInt(7, producto.stock());
                ps.setInt(8, producto.stockMinimo());
                ps.setInt(9, producto.id());
                ps.executeUpdate();
                return porId(producto.id()).orElse(producto);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar producto (¿código duplicado?): " + e.getMessage(), e);
        }
    }

    private void enlazarProveedor(PreparedStatement ps, int indice, Integer proveedorId) throws SQLException {
        if (proveedorId == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, proveedorId);
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM productos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar producto " + id, e);
        }
    }

    @Override
    public void actualizarStock(int productoId, int nuevoStock) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("UPDATE productos SET stock = ? WHERE id = ?")) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar stock del producto " + productoId, e);
        }
    }
}
