package com.novedadesale.puntoventa.repositories.sqlite;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.VentaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteVentaRepository implements VentaRepository {

    private static final String SELECT_BASE =
            "SELECT id, folio, fecha, usuario_id, usuario_nombre, cliente_id, cliente_nombre, " +
            "subtotal, descuento, iva, total, metodo_pago, monto_recibido, cambio, estado FROM ventas ";

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        return new Venta(
                rs.getInt("id"),
                rs.getString("folio"),
                LocalDateTime.parse(rs.getString("fecha")),
                rs.getInt("usuario_id"),
                rs.getString("usuario_nombre"),
                SqlUtil.enteroONulo(rs, "cliente_id"),
                rs.getString("cliente_nombre"),
                cargarItems(rs.getInt("id")),
                SqlUtil.bd(rs.getString("subtotal")),
                SqlUtil.bd(rs.getString("descuento")),
                SqlUtil.bd(rs.getString("iva")),
                SqlUtil.bd(rs.getString("total")),
                MetodoPago.valueOf(rs.getString("metodo_pago")),
                SqlUtil.bd(rs.getString("monto_recibido")),
                SqlUtil.bd(rs.getString("cambio")),
                EstadoVenta.valueOf(rs.getString("estado")));
    }

    private List<ItemVenta> cargarItems(int ventaId) throws SQLException {
        List<ItemVenta> items = new ArrayList<>();
        String sql = "SELECT producto_id, codigo, nombre, precio_unitario, cantidad FROM venta_items WHERE venta_id = ? ORDER BY id";
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new ItemVenta(rs.getInt("producto_id"), rs.getString("codigo"),
                            rs.getString("nombre"), SqlUtil.bd(rs.getString("precio_unitario")),
                            rs.getInt("cantidad")));
                }
            }
        }
        return items;
    }

    @Override
    public List<Venta> listar() {
        List<Venta> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearVenta(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al listar ventas", e);
        }
    }

    @Override
    public List<Venta> porFecha(LocalDate fecha) {
        return consultar(SELECT_BASE + "WHERE date(fecha) = ? ORDER BY id DESC", fecha.toString());
    }

    @Override
    public List<Venta> entre(LocalDate desde, LocalDate hasta) {
        List<Venta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1");
        if (desde != null) {
            sql.append(" AND date(fecha) >= ?");
        }
        if (hasta != null) {
            sql.append(" AND date(fecha) <= ?");
        }
        sql.append(" ORDER BY id DESC");
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql.toString())) {
            int i = 1;
            if (desde != null) {
                ps.setString(i++, desde.toString());
            }
            if (hasta != null) {
                ps.setString(i, hasta.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVenta(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar ventas por rango", e);
        }
    }

    private List<Venta> consultar(String sql, String parametro) {
        List<Venta> lista = new ArrayList<>();
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVenta(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ServiceException("Error al consultar ventas", e);
        }
    }

    @Override
    public Optional<Venta> porId(int id) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearVenta(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar venta " + id, e);
        }
    }

    @Override
    public Optional<Venta> porFolio(String folio) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(SELECT_BASE + "WHERE folio = ?")) {
            ps.setString(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearVenta(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar venta por folio", e);
        }
    }

    @Override
    public Venta guardar(Venta venta) {
        String sql = "INSERT INTO ventas (folio, fecha, usuario_id, usuario_nombre, cliente_id, cliente_nombre, " +
                "subtotal, descuento, iva, total, metodo_pago, monto_recibido, cambio, estado) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            int ventaId;
            try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, venta.folio());
                ps.setString(2, venta.fecha().toString());
                ps.setInt(3, venta.usuarioId());
                ps.setString(4, venta.usuarioNombre());
                if (venta.clienteId() == null) {
                    ps.setNull(5, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(5, venta.clienteId());
                }
                ps.setString(6, venta.clienteNombre());
                ps.setString(7, SqlUtil.txt(venta.subtotal()));
                ps.setString(8, SqlUtil.txt(venta.descuento()));
                ps.setString(9, SqlUtil.txt(venta.iva()));
                ps.setString(10, SqlUtil.txt(venta.total()));
                ps.setString(11, venta.metodoPago().name());
                ps.setString(12, SqlUtil.txt(venta.montoRecibido()));
                ps.setString(13, SqlUtil.txt(venta.cambio()));
                ps.setString(14, venta.estado().name());
                ps.executeUpdate();
                ventaId = SqlUtil.claveGenerada(ps);
            }
            guardarItems(ventaId, venta.items());
            return porId(ventaId).orElse(venta);
        } catch (SQLException e) {
            throw new ServiceException("Error al registrar la venta: " + e.getMessage(), e);
        }
    }

    private void guardarItems(int ventaId, List<ItemVenta> items) throws SQLException {
        String sql = "INSERT INTO venta_items (venta_id, producto_id, codigo, nombre, precio_unitario, cantidad, importe) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement(sql)) {
            for (ItemVenta item : items) {
                ps.setInt(1, ventaId);
                ps.setInt(2, item.productoId());
                ps.setString(3, item.codigo());
                ps.setString(4, item.nombre());
                ps.setString(5, SqlUtil.txt(item.precioUnitario()));
                ps.setInt(6, item.cantidad());
                ps.setString(7, SqlUtil.txt(item.importe()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void actualizarEstado(int ventaId, EstadoVenta estado) {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("UPDATE ventas SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, ventaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar estado de la venta " + ventaId, e);
        }
    }

    @Override
    public int contar() {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("SELECT COUNT(*) FROM ventas");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new ServiceException("Error al contar ventas", e);
        }
    }

    @Override
    public int proximoNumero() {
        try (PreparedStatement ps = BaseDatos.conexion().prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 FROM ventas");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        } catch (SQLException e) {
            throw new ServiceException("Error al calcular el siguiente folio", e);
        }
    }
}
