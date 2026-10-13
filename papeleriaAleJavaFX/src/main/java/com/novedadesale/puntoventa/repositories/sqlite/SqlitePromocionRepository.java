package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.PromocionRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePromocionRepository implements PromocionRepository {

    private static final String SELECT_BASE =
            "SELECT id, nombre, tipo, valor, fecha_inicio, fecha_fin, activa, categoria_id FROM promociones ";

    private Promocion mapear(ResultSet rs) throws SQLException {
        String inicio = rs.getString("fecha_inicio");
        String fin = rs.getString("fecha_fin");
        return new Promocion(
                rs.getInt("id"),
                rs.getString("nombre"),
                TipoPromocion.valueOf(rs.getString("tipo")),
                SqlUtil.bd(rs.getString("valor")),
                inicio == null ? null : LocalDate.parse(inicio),
                fin == null ? null : LocalDate.parse(fin),
                rs.getInt("activa") == 1,
                SqlUtil.enteroONulo(rs, "categoria_id"));
    }

    @Override
    public List<Promocion> listar() {
        List<Promocion> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar promociones", e);
        }
    }

    @Override
    public Optional<Promocion> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar promoción " + id, e);
        }
    }

    @Override
    public List<Promocion> activas(LocalDate fecha) {
        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        List<Promocion> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                SELECT_BASE + "WHERE activa = 1 AND fecha_inicio <= ? AND fecha_fin >= ? ORDER BY id")) {
            ps.setString(1, f.toString());
            ps.setString(2, f.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar promociones activas", e);
        }
    }

    @Override
    public Promocion guardar(Promocion promocion) {
        try {
            if (promocion.id() <= 0) {
                String sql = "INSERT INTO promociones (nombre, tipo, valor, fecha_inicio, fecha_fin, activa, categoria_id) VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    enlazar(ps, promocion);
                    ps.executeUpdate();
                    return porId(SqlUtil.claveGenerada(ps)).orElse(promocion);
                }
            }
            String sql = "UPDATE promociones SET nombre = ?, tipo = ?, valor = ?, fecha_inicio = ?, fecha_fin = ?, activa = ?, categoria_id = ? WHERE id = ?";
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
                enlazar(ps, promocion);
                ps.setInt(8, promocion.id());
                ps.executeUpdate();
                return porId(promocion.id()).orElse(promocion);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar promoción", e);
        }
    }

    private void enlazar(PreparedStatement ps, Promocion p) throws SQLException {
        ps.setString(1, p.nombre());
        ps.setString(2, p.tipo().name());
        ps.setString(3, SqlUtil.txt(p.valor()));
        ps.setString(4, p.fechaInicio() == null ? null : p.fechaInicio().toString());
        ps.setString(5, p.fechaFin() == null ? null : p.fechaFin().toString());
        ps.setInt(6, p.activa() ? 1 : 0);
        if (p.categoriaId() == null) {
            ps.setNull(7, java.sql.Types.INTEGER);
        } else {
            ps.setInt(7, p.categoriaId());
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("DELETE FROM promociones WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar promoción " + id, e);
        }
    }
}
