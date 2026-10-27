package com.novedadesale.puntoventa.persistence;

import com.novedadesale.puntoventa.shared.Seguridad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/** Datos de ejemplo que se insertan solo cuando la base está vacía. */
final class Seeder {

    private Seeder() {
    }

    static void sembrarSiVacio(Connection c) throws SQLException {
        if (contar(c, "categorias") > 0) {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO categorias (nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            for (String nombre : new String[]{"Escolares", "Oficina", "Regalos", "Arte"}) {
                ps.setString(1, nombre);
                ps.executeUpdate();
            }
        }

        insertarUsuario(c, "Administrador", "admin", "admin123", "ADMIN");
        insertarUsuario(c, "Caja 1", "caja1", "caja123", "CAJERO");

        long papelera = insertarProveedor(c, "Papelera del Norte", "Rosa Martínez", "555-111-2222", "ventas@papelnorte.mx");
        long distribuidora = insertarProveedor(c, "Distribuidora Escolar SA", "Luis Gómez", "555-333-4444", "contacto@descescolar.mx");

        insertarProducto(c, "P-001", "Cuaderno espiral 100 hojas", 1, "25.50", 48, 10, (int) papelera);
        insertarProducto(c, "P-002", "Plumas de gel (caja 12)", 2, "89.90", 8, 10, (int) distribuidora);
        insertarProducto(c, "P-003", "Peluche oso mediano", 3, "149.00", 12, 10, (int) distribuidora);
        insertarProducto(c, "P-004", "Lápices de colores 24 piezas", 4, "79.00", 0, 10, (int) distribuidora);
        insertarProducto(c, "P-005", "Mochila escolar", 1, "399.00", 6, 10, (int) papelera);
        insertarProducto(c, "P-006", "Taza personalizada", 3, "120.00", 20, 10, (int) distribuidora);
        insertarProducto(c, "P-007", "Marcadores permanentes", 2, "45.00", 35, 10, (int) papelera);
        insertarProducto(c, "P-008", "Block de dibujo profesional", 4, "55.00", 9, 10, (int) papelera);

        insertarCliente(c, "María López", "555-100-2000", "maria@example.com", "5");
        insertarCliente(c, "Juan Pérez", "555-300-4000", "juan@example.com", "0");

        insertarPromocion(c, "Regreso a clases -10%", "PORCENTAJE", "10",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), 1);
    }

    private static int contar(Connection c, String tabla) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static long insertarUsuario(Connection c, String nombre, String usuario,
                                        String password, String rol) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO usuarios (nombre, usuario, password_hash, rol, activo, debe_cambiar_password) "
                + "VALUES (?,?,?,?,1,1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, Seguridad.hash(password));
            ps.setString(4, rol);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private static void insertarProducto(Connection c, String codigo, String nombre, int categoriaId,
                                         String precio, int stock, int stockMinimo,
                                         int proveedorId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO productos (codigo, nombre, categoria_id, proveedor_id, precio, stock, stock_minimo) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setInt(3, categoriaId);
            ps.setInt(4, proveedorId);
            ps.setString(5, precio);
            ps.setInt(6, stock);
            ps.setInt(7, stockMinimo);
            ps.executeUpdate();
        }
    }

    private static void insertarCliente(Connection c, String nombre, String telefono,
                                        String email, String descuento) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO clientes (nombre, telefono, email, descuento) VALUES (?,?,?,?)")) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, email);
            ps.setString(4, descuento);
            ps.executeUpdate();
        }
    }

    private static long insertarProveedor(Connection c, String nombre, String contacto,
                                          String telefono, String email) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO proveedores (nombre, contacto, telefono, email) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, contacto);
            ps.setString(3, telefono);
            ps.setString(4, email);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private static void insertarPromocion(Connection c, String nombre, String tipo, String valor,
                                          LocalDate inicio, LocalDate fin, Integer categoriaId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO promociones (nombre, tipo, valor, fecha_inicio, fecha_fin, activa, categoria_id) VALUES (?,?,?,?,?,1,?)")) {
            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setString(3, valor);
            ps.setString(4, inicio.toString());
            ps.setString(5, fin.toString());
            if (categoriaId == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, categoriaId);
            }
            ps.executeUpdate();
        }
    }

    private static long claveGenerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getLong(1) : -1;
        }
    }
}
