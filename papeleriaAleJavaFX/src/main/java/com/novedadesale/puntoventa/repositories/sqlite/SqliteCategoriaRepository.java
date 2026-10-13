package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.CategoriaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCategoriaRepository implements CategoriaRepository {

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT id, nombre FROM categorias ORDER BY nombre";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar categorías", e);
        }
    }

    @Override
    public Optional<Categoria> porId(int id) {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Categoria(rs.getInt("id"), rs.getString("nombre")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar categoría " + id, e);
        }
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        try {
            if (categoria.id() <= 0) {
                String sql = "INSERT INTO categorias (nombre) VALUES (?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, categoria.nombre());
                    ps.executeUpdate();
                    return new Categoria(SqlUtil.claveGenerada(ps), categoria.nombre());
                }
            }
            String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                ps.setString(1, categoria.nombre());
                ps.setInt(2, categoria.id());
                ps.executeUpdate();
                return categoria;
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar categoría", e);
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar categoría " + id, e);
        }
    }
}
