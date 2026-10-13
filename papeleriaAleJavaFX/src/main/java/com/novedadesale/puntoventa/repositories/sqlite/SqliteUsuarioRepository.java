package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.UsuarioRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUsuarioRepository implements UsuarioRepository {

    private static final String SELECT_BASE =
            "SELECT id, nombre, usuario, password_hash, rol, activo FROM usuarios ";

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("usuario"),
                rs.getString("password_hash"), Rol.valueOf(rs.getString("rol")), rs.getInt("activo") == 1);
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar usuarios", e);
        }
    }

    @Override
    public Optional<Usuario> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar usuario " + id, e);
        }
    }

    @Override
    public Optional<Usuario> porUsuario(String usuario) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar usuario", e);
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        try {
            if (usuario.id() <= 0) {
                String sql = "INSERT INTO usuarios (nombre, usuario, password_hash, rol, activo) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, usuario.nombre());
                    ps.setString(2, usuario.usuario());
                    ps.setString(3, usuario.passwordHash());
                    ps.setString(4, usuario.rol().name());
                    ps.setInt(5, usuario.activo() ? 1 : 0);
                    ps.executeUpdate();
                    return porId(SqlUtil.claveGenerada(ps)).orElse(usuario);
                }
            }
            String sql = "UPDATE usuarios SET nombre = ?, usuario = ?, password_hash = ?, rol = ?, activo = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                ps.setString(1, usuario.nombre());
                ps.setString(2, usuario.usuario());
                ps.setString(3, usuario.passwordHash());
                ps.setString(4, usuario.rol().name());
                ps.setInt(5, usuario.activo() ? 1 : 0);
                ps.setInt(6, usuario.id());
                ps.executeUpdate();
                return porId(usuario.id()).orElse(usuario);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar usuario (¿usuario duplicado?): " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar usuario " + id, e);
        }
    }
}
