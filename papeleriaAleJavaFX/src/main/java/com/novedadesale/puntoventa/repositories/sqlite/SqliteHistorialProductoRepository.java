package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.HistorialProducto;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.HistorialProductoRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteHistorialProductoRepository implements HistorialProductoRepository {

    @Override
    public List<HistorialProducto> porProducto(int productoId) {
        List<HistorialProducto> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                "SELECT id, producto_id, accion, usuario, fecha, detalle " +
                "FROM productos_historial WHERE producto_id = ? ORDER BY id DESC")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HistorialProducto(
                            rs.getInt("id"),
                            rs.getInt("producto_id"),
                            rs.getString("accion"),
                            rs.getString("usuario"),
                            rs.getString("fecha"),
                            rs.getString("detalle")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar el historial del producto " + productoId, e);
        }
    }
}
