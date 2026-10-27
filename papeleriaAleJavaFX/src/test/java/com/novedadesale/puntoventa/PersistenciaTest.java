package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Totales;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;
import com.novedadesale.puntoventa.ui.Sesion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de humo de la capa de persistencia + servicios (SQLite temporal). */
class PersistenciaTest {

    private static Path db;

    @BeforeAll
    static void preparar() throws Exception {
        db = Files.createTempFile("novedadesale-test-", ".db");
        Files.deleteIfExists(db);
        BaseDatos.usarRuta(db.toString());
        // La fase 1 exige sesión en todos los servicios salvo autenticar.
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
    }

    @AfterAll
    static void limpiar() throws Exception {
        Sesion.cerrar();
        BaseDatos.cerrar();
        Files.deleteIfExists(db);
    }

    @Test
    void catalogoSembrado() {
        if (!Sesion.activa()) {
            Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        }
        assertTrue(Servicios.productos().listar().size() >= 8);
        assertTrue(Servicios.categorias().listar().size() >= 4);
        assertTrue(Servicios.productos().porCodigo("P-001").isPresent());
    }

    @Test
    void productosSembradosQuedanAsociadosAProveedor() {
        if (!Sesion.activa()) {
            Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        }
        Producto producto = Servicios.productos().porCodigo("P-001").orElseThrow();

        assertTrue(producto.proveedorId() != null && producto.proveedorId() > 0);
        assertFalse(producto.proveedor().isBlank());
    }

    @Test
    void guardarProductoConProveedorEImagenLosPersisteYActualiza() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        try {
            var proveedor = Servicios.proveedores().listar().get(0);
            String imagen = "C:/referencias/cuaderno.png";

            Producto creado = Servicios.productos().guardar(new Producto(
                    0, "X-903", "Producto con proveedor", 1, null,
                    proveedor.id(), null, new BigDecimal("15.00"), 3, 1, imagen));

            Producto leido = Servicios.productos().porId(creado.id()).orElseThrow();
            assertEquals(proveedor.id(), leido.proveedorId());
            assertEquals(proveedor.nombre(), leido.proveedor());
            assertEquals(imagen, leido.imagen());

            Servicios.productos().guardar(new Producto(
                    leido.id(), leido.codigo(), leido.nombre(), leido.categoriaId(), leido.categoria(),
                    null, null, leido.precio(), leido.stock(), leido.stockMinimo(), null));

            Producto limpiado = Servicios.productos().porId(leido.id()).orElseThrow();
            assertNull(limpiado.proveedorId());
            assertNull(limpiado.imagen());
        } finally {
            Sesion.cerrar();
        }
    }

    @Test
    void guardarProductoRegistraHistorialConUsuarioYFecha() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        try {
            Producto creado = Servicios.productos().guardar(new Producto(
                    0, "X-904", "Producto con historial", 1, null,
                    null, null, new BigDecimal("10.00"), 5, 2, null));

            var historial = Servicios.productos().historial(creado.id());
            assertEquals(1, historial.size());
            assertEquals("CREADO", historial.get(0).accion());
            assertEquals("Administrador", historial.get(0).usuario());
            assertFalse(historial.get(0).fecha().isBlank());

            Servicios.productos().guardar(new Producto(
                    creado.id(), "X-904", "Producto editado", 1, null,
                    null, null, new BigDecimal("12.00"), 5, 2, null));

            var actualizado = Servicios.productos().historial(creado.id());
            assertEquals(2, actualizado.size());
            assertEquals("ACTUALIZADO", actualizado.get(0).accion());
            assertTrue(actualizado.get(0).detalle().contains("Nombre"));
            assertTrue(actualizado.get(0).detalle().contains("Precio"));
        } finally {
            Sesion.cerrar();
        }
    }

    @Test
    void autenticacion() {
        assertTrue(Servicios.usuarios().autenticar("admin", "admin123").isPresent());
        assertFalse(Servicios.usuarios().autenticar("admin", "incorrecta").isPresent());
        assertFalse(Servicios.usuarios().autenticar("nadie", "x").isPresent());
    }

    @Test
    void calculadoraTotalesConDescuento() {
        List<ItemVenta> items = List.of(
                new ItemVenta(1, "P-001", "Cuaderno", new BigDecimal("25.50"), 2));
        Totales t = CalculadoraTotales.calcular(items, new BigDecimal("10"));
        assertEquals(0, t.subtotal().compareTo(new BigDecimal("51.00")));
        assertEquals(0, t.descuento().compareTo(new BigDecimal("5.10")));
        assertEquals(0, t.iva().compareTo(new BigDecimal("7.34")));
        assertEquals(0, t.total().compareTo(new BigDecimal("53.24")));
    }

    @Test
    void registrarVentaDescuentaStockYCalculaCambio() {
        if (!Sesion.activa()) {
            Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        }
        Producto antes = Servicios.productos().porCodigo("P-001").orElseThrow();
        int stockAntes = antes.stock();

        // P-001 (Escolares) tiene una promoción sembrada del 10% que el servicio
        // aplica por sí mismo; la solicitud no lleva descuento de cliente.
        SolicitudVenta solicitud = new SolicitudVenta(
                List.of(new ItemVenta(antes.id(), antes.codigo(), antes.nombre(), antes.precio(), 2)),
                null, null, BigDecimal.ZERO, MetodoPago.EFECTIVO, new BigDecimal("100"));

        Venta venta = Servicios.ventas().registrar(solicitud);

        assertTrue(venta.folio().startsWith("V-"));
        assertEquals(0, venta.descuento().compareTo(new BigDecimal("5.10")));
        assertEquals(0, venta.total().compareTo(new BigDecimal("53.24")));
        assertEquals(0, venta.cambio().compareTo(new BigDecimal("46.76")));

        Producto despues = Servicios.productos().porCodigo("P-001").orElseThrow();
        assertEquals(stockAntes - 2, despues.stock());
    }

    @Test
    void usuariosSembradosDebenCambiarPassword() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        try {
            var sembrados = Servicios.usuarios().listar();
            assertTrue(sembrados.stream()
                    .filter(u -> u.usuario().equals("admin")).findFirst().orElseThrow().debeCambiarPassword());
            assertTrue(sembrados.stream()
                    .filter(u -> u.usuario().equals("caja1")).findFirst().orElseThrow().debeCambiarPassword());
        } finally {
            Sesion.cerrar();
        }
    }

    @Test
    void usuarioCreadoPorAdminDebeCambiarPassword() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        try {
            Usuario nuevo = Servicios.usuarios().crear("Prueba S8", "prueba-s8", "Clave123", Rol.CAJERO);
            assertTrue(nuevo.debeCambiarPassword());
            assertEquals(0, nuevo.intentosFallidos());
            assertNull(nuevo.bloqueadoHasta());
        } finally {
            Sesion.cerrar();
        }
    }

    @Test
    void migracionDeBaseExistenteAgregaCredencialesSinPerderUsuarios() throws Exception {
        Path legado = Files.createTempFile("novedadesale-legado-", ".db");
        Files.deleteIfExists(legado);
        String hash = com.novedadesale.puntoventa.shared.Seguridad.hash("Clave123");
        try (java.sql.Connection c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + legado);
             java.sql.Statement st = c.createStatement()) {
            st.execute("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, "
                    + "usuario TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, rol TEXT NOT NULL, "
                    + "activo INTEGER NOT NULL DEFAULT 1)");
            st.execute("CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL)");
            st.execute("INSERT INTO categorias (nombre) VALUES ('Existente')");
            st.execute("INSERT INTO usuarios (nombre, usuario, password_hash, rol, activo) VALUES "
                    + "('Legado', 'legado', '" + hash + "', 'CAJERO', 1)");
        }
        BaseDatos.cerrar();
        BaseDatos.usarRuta(legado.toString());
        try {
            Usuario usuario = Servicios.usuarios().autenticar("legado", "Clave123").orElseThrow();
            assertFalse(usuario.debeCambiarPassword());
            assertEquals(0, usuario.intentosFallidos());
            assertNull(usuario.bloqueadoHasta());
        } finally {
            BaseDatos.cerrar();
            Files.deleteIfExists(legado);
            BaseDatos.usarRuta(db.toString());
        }
    }

    /**
     * Revisión ponytail #6: la migración de {@code pagos} es lo único que puede
     * dañar la base del usuario y antes no tenía ninguna prueba.
     */
    @Test
    void migracionDePagosAgregaForeignKeysSinPerderPagos() throws Exception {
        Path legado = Files.createTempFile("novedadesale-pagos-", ".db");
        Files.deleteIfExists(legado);
        try (java.sql.Connection c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + legado);
             java.sql.Statement st = c.createStatement()) {
            st.execute("CREATE TABLE pagos (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "venta_id INTEGER NOT NULL, metodo TEXT NOT NULL, "
                    + "monto TEXT NOT NULL DEFAULT '0', fecha TEXT NOT NULL)");
            st.execute("INSERT INTO pagos (venta_id, metodo, monto, fecha) "
                    + "VALUES (7, 'EFECTIVO', '10.00', '2026-10-20T10:00:00')");
        }
        BaseDatos.cerrar();
        BaseDatos.usarRuta(legado.toString());
        try {
            BaseDatos.conexion();

            assertTrue(pagosTieneForeignKeys(), "la tabla migrada debe llevar la FK a ventas");
            assertEquals(1, contarPagos());
            assertEquals("10.00", montoDePagos());
            assertFalse(existeTabla("pagos_tmp"), "no debe quedar pagos_tmp huérfano");

            BaseDatos.cerrar();
            BaseDatos.conexion();
            assertEquals(1, contarPagos(), "una segunda apertura no debe volver a migrar ni perder pagos");
        } finally {
            BaseDatos.cerrar();
            Files.deleteIfExists(legado);
            BaseDatos.usarRuta(db.toString());
        }
    }

    /**
     * Revisión ponytail #1: un intento que murió entre DROP y RENAME dejaba los
     * pagos en {@code pagos_tmp} y DDL creaba un {@code pagos} vacío, perdiendo
     * el historial. El rescate debe ocurrir antes del DDL.
     */
    @Test
    void migracionRescataPagosDejadosEnPagosTmpPorUnIntentoInterrumpido() throws Exception {
        Path legado = Files.createTempFile("novedadesale-pagos-tmp-", ".db");
        Files.deleteIfExists(legado);
        try (java.sql.Connection c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + legado);
             java.sql.Statement st = c.createStatement()) {
            st.execute("CREATE TABLE ventas (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "folio TEXT NOT NULL UNIQUE, fecha TEXT NOT NULL, metodo_pago TEXT NOT NULL, "
                    + "estado TEXT NOT NULL)");
            st.execute("CREATE TABLE pagos_tmp (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "venta_id INTEGER NOT NULL, metodo TEXT NOT NULL, "
                    + "monto TEXT NOT NULL DEFAULT '0', fecha TEXT NOT NULL, "
                    + "FOREIGN KEY (venta_id) REFERENCES ventas(id))");
            st.execute("INSERT INTO pagos_tmp (venta_id, metodo, monto, fecha) "
                    + "VALUES (1, 'EFECTIVO', '25.50', '2026-10-20T11:00:00')");
        }
        BaseDatos.cerrar();
        BaseDatos.usarRuta(legado.toString());
        try {
            BaseDatos.conexion();

            assertEquals(1, contarPagos(), "los pagos del intento interrumpido deben recuperarse");
            assertEquals("25.50", montoDePagos());
            assertFalse(existeTabla("pagos_tmp"));
            assertTrue(pagosTieneForeignKeys());
        } finally {
            BaseDatos.cerrar();
            Files.deleteIfExists(legado);
            BaseDatos.usarRuta(db.toString());
        }
    }

    /**
     * Revisión ponytail #5: el stock se descuenta con aritmética SQL
     * ({@code stock = stock - ? WHERE stock >= ?}), no con una lectura vieja.
     */
    @Test
    void descontarStockNoSobrevendeYNuncaQuedaNegativo() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
        var repo = new com.novedadesale.puntoventa.repositories.sqlite.SqliteProductoRepository();

        Producto p = Servicios.productos().porCodigo("P-004").orElseThrow();
        int original = p.stock();

        repo.actualizarStock(p.id(), 5);
        assertTrue(repo.descontarStock(p.id(), 3));
        assertEquals(2, Servicios.productos().porId(p.id()).orElseThrow().stock());

        assertFalse(repo.descontarStock(p.id(), 3), "sin stock suficiente no debe descontar");
        assertEquals(2, Servicios.productos().porId(p.id()).orElseThrow().stock(),
                "el stock no puede quedar negativo");

        repo.actualizarStock(p.id(), original);
    }

    /** #5: una venta rechazada por stock no deja venta ni modifica el stock. */
    @Test
    void ventaSinStockNoDejaVentaNiModificaStock() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());

        Producto p = Servicios.productos().porCodigo("P-004").orElseThrow();
        assertEquals(0, p.stock(), "P-004 se siembra sin stock");
        int ventasAntes = Servicios.reportes().ventas(LocalDate.now(), LocalDate.now()).size();

        assertThrows(com.novedadesale.puntoventa.exceptions.ValidacionException.class,
                () -> Servicios.ventas().registrar(new SolicitudVenta(
                        List.of(new ItemVenta(p.id(), p.codigo(), p.nombre(), p.precio(), 1)),
                        null, null, BigDecimal.ZERO, MetodoPago.EFECTIVO, new BigDecimal("100"))));

        assertEquals(0, Servicios.productos().porId(p.id()).orElseThrow().stock());
        assertEquals(ventasAntes, Servicios.reportes().ventas(LocalDate.now(), LocalDate.now()).size());
    }

    private int contarPagos() throws Exception {
        try (java.sql.Statement st = BaseDatos.conexion().createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pagos")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String montoDePagos() throws Exception {
        try (java.sql.Statement st = BaseDatos.conexion().createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT monto FROM pagos ORDER BY id LIMIT 1")) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private boolean pagosTieneForeignKeys() throws Exception {
        try (java.sql.Statement st = BaseDatos.conexion().createStatement();
             java.sql.ResultSet rs = st.executeQuery("PRAGMA foreign_key_list(pagos)")) {
            return rs.next();
        }
    }

    private boolean existeTabla(String tabla) throws Exception {
        try (java.sql.PreparedStatement ps = BaseDatos.conexion().prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, tabla);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
