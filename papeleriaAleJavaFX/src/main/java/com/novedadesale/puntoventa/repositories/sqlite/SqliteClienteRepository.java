package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.ClienteRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteClienteRepository implements ClienteRepository {

    private static final String SELECT_BASE =
            "SELECT id, nombre, telefono, email, descuento FROM clientes ";

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("telefono"),
                rs.getString("email"), SqlUtil.bd(rs.getString("descuento")));
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar clientes", e);
        }
    }

    @Override
    public Optional<Cliente> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar cliente " + id, e);
        }
    }

    @Override
    public List<Cliente> buscar(String texto) {
        String q = texto == null ? "" : texto.trim();
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                SELECT_BASE + "WHERE LOWER(nombre) LIKE LOWER(?) OR telefono LIKE ? ORDER BY nombre")) {
            ps.setString(1, "%" + q + "%");
            ps.setString(2, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar clientes", e);
        }
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        try {
            if (cliente.id() <= 0) {
                String sql = "INSERT INTO clientes (nombre, telefono, email, descuento) VALUES (?,?,?,?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    enlazar(ps, cliente);
                    ps.executeUpdate();
                    return porId(SqlUtil.claveGenerada(ps)).orElse(cliente);
                }
            }
            String sql = "UPDATE clientes SET nombre = ?, telefono = ?, email = ?, descuento = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                enlazar(ps, cliente);
                ps.setInt(5, cliente.id());
                ps.executeUpdate();
                return porId(cliente.id()).orElse(cliente);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar cliente", e);
        }
    }

    private void enlazar(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.nombre());
        ps.setString(2, cliente.telefono());
        ps.setString(3, cliente.email());
        ps.setString(4, SqlUtil.txt(cliente.descuento()));
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar cliente " + id, e);
        }
    }
}
