package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Pago;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.PagoRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlitePagoRepository implements PagoRepository {

    private static final String SELECT_BASE =
            "SELECT id, venta_id, metodo, monto, fecha FROM pagos ";

    private Pago mapear(ResultSet rs) throws SQLException {
        return new Pago(rs.getInt("id"), rs.getInt("venta_id"),
                MetodoPago.valueOf(rs.getString("metodo")),
                SqlUtil.bd(rs.getString("monto")),
                LocalDateTime.parse(rs.getString("fecha")));
    }

    @Override
    public List<Pago> listar() {
        List<Pago> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar pagos", e);
        }
    }

    @Override
    public List<Pago> porVenta(int ventaId) {
        List<Pago> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE venta_id = ? ORDER BY id")) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar pagos de la venta " + ventaId, e);
        }
    }

    @Override
    public Pago guardar(Pago pago) {
        String sql = "INSERT INTO pagos (venta_id, metodo, monto, fecha) VALUES (?,?,?,?)";
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.ventaId());
            ps.setString(2, pago.metodo().name());
            ps.setString(3, SqlUtil.txt(pago.monto()));
            ps.setString(4, (pago.fecha() == null ? LocalDateTime.now() : pago.fecha()).toString());
            ps.executeUpdate();
            int id = SqlUtil.claveGenerada(ps);
            return new Pago(id, pago.ventaId(), pago.metodo(), pago.monto(), pago.fecha());
        } catch (SQLException e) {
            throw new ServiceException("Error al registrar el pago", e);
        }
    }
}
