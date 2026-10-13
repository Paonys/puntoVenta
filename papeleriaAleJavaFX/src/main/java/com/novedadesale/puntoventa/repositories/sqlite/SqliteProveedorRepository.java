package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.ProveedorRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteProveedorRepository implements ProveedorRepository {

    private static final String SELECT_BASE =
            "SELECT id, nombre, contacto, telefono, email FROM proveedores ";

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("contacto"),
                rs.getString("telefono"), rs.getString("email"));
    }

    @Override
    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar proveedores", e);
        }
    }

    @Override
    public Optional<Proveedor> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar proveedor " + id, e);
        }
    }

    @Override
    public List<Proveedor> buscar(String texto) {
        String q = texto == null ? "" : texto.trim();
        List<Proveedor> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                SELECT_BASE + "WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre")) {
            ps.setString(1, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar proveedores", e);
        }
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        try {
            if (proveedor.id() <= 0) {
                String sql = "INSERT INTO proveedores (nombre, contacto, telefono, email) VALUES (?,?,?,?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    enlazar(ps, proveedor);
                    ps.executeUpdate();
                    return porId(SqlUtil.claveGenerada(ps)).orElse(proveedor);
                }
            }
            String sql = "UPDATE proveedores SET nombre = ?, contacto = ?, telefono = ?, email = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                enlazar(ps, proveedor);
                ps.setInt(5, proveedor.id());
                ps.executeUpdate();
                return porId(proveedor.id()).orElse(proveedor);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar proveedor", e);
        }
    }

    private void enlazar(PreparedStatement ps, Proveedor proveedor) throws SQLException {
        ps.setString(1, proveedor.nombre());
        ps.setString(2, proveedor.contacto());
        ps.setString(3, proveedor.telefono());
        ps.setString(4, proveedor.email());
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM proveedores WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar proveedor " + id, e);
        }
    }
}
