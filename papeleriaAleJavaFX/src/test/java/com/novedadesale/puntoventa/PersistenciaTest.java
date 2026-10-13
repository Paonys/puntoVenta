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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de humo de la capa de persistencia + servicios (SQLite temporal). */
class PersistenciaTest {

    private static Path db;

    @BeforeAll
    static void preparar() throws Exception {
        db = Files.createTempFile("novedadesale-test-", ".db");
        Files.deleteIfExists(db);
        BaseDatos.usarRuta(db.toString());
    }

    @AfterAll
    static void limpiar() throws Exception {
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
        Producto antes = Servicios.productos().porCodigo("P-001").orElseThrow();
        int stockAntes = antes.stock();

        SolicitudVenta solicitud = new SolicitudVenta(
                List.of(new ItemVenta(antes.id(), antes.codigo(), antes.nombre(), antes.precio(), 2)),
                null, null, new BigDecimal("10"), MetodoPago.EFECTIVO, new BigDecimal("100"), 1, "Administrador");

        Venta venta = Servicios.ventas().registrar(solicitud);

        assertTrue(venta.folio().startsWith("V-"));
        assertEquals(0, venta.total().compareTo(new BigDecimal("53.24")));
        assertEquals(0, venta.cambio().compareTo(new BigDecimal("46.76")));

        Producto despues = Servicios.productos().porCodigo("P-001").orElseThrow();
        assertEquals(stockAntes - 2, despues.stock());
    }
}
