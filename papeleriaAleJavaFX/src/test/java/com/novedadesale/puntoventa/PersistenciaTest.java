package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Totales;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
}
