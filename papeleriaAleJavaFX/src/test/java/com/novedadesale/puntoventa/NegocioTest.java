package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.models.Totales;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la lógica de negocio (capa de servicios) sobre una base SQLite
 * temporal: totales/IVA, promociones, registro y cancelación de ventas y reportes.
 */
class NegocioTest {

    private Path db;

    @BeforeEach
    void preparar() throws Exception {
        db = Files.createTempFile("novedadesale-negocio-", ".db");
        Files.deleteIfExists(db);
        BaseDatos.usarRuta(db.toString());
    }

    @AfterEach
    void limpiar() throws Exception {
        BaseDatos.cerrar();
        Files.deleteIfExists(db);
    }

    // ------------------------------------------------------------------
    // CalculadoraTotales
    // ------------------------------------------------------------------

    @Test
    void totalesSinDescuentoAplicanIva16() {
        List<ItemVenta> items = List.of(new ItemVenta(1, "P-001", "Cuaderno", new BigDecimal("25.50"), 2));

        Totales t = CalculadoraTotales.calcular(items, BigDecimal.ZERO);

        assertEquals(0, t.subtotal().compareTo(new BigDecimal("51.00")));
        assertEquals(0, t.descuento().compareTo(new BigDecimal("0.00")));
        assertEquals(0, t.iva().compareTo(new BigDecimal("8.16")));
        assertEquals(0, t.total().compareTo(new BigDecimal("59.16")));
    }

    @Test
    void totalesConEntradaNulaOVaciaSonCero() {
        Totales nulo = CalculadoraTotales.calcular(null, null);
        assertEquals(0, nulo.total().compareTo(BigDecimal.ZERO));

        Totales vacio = CalculadoraTotales.calcular(List.of(), new BigDecimal("10"));
        assertEquals(0, vacio.subtotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, vacio.total().compareTo(BigDecimal.ZERO));
    }

    @Test
    void ivaRedondeaMitadHaciaArriba() {
        List<ItemVenta> items = List.of(new ItemVenta(1, "X", "x", new BigDecimal("33.33"), 3));

        Totales t = CalculadoraTotales.calcular(items, null);

        assertEquals(0, t.subtotal().compareTo(new BigDecimal("99.99")));
        assertEquals(0, t.iva().compareTo(new BigDecimal("16.00")));
        assertEquals(0, t.total().compareTo(new BigDecimal("115.99")));
    }

    // ------------------------------------------------------------------
    // Promociones
    // ------------------------------------------------------------------

    @Test
    void promocionPorCategoriaDevuelvePorcentaje() {
        Producto cuaderno = producto("P-001");
        List<ItemVenta> items = List.of(item(cuaderno, 2));

        BigDecimal pct = Servicios.promociones().calcularDescuento(items, LocalDate.now());

        assertEquals(0, pct.compareTo(new BigDecimal("10.00")));
    }

    @Test
    void promocionNoAplicaAProductosDeOtraCategoria() {
        Producto peluche = producto("P-003");
        List<ItemVenta> items = List.of(item(peluche, 1));

        BigDecimal pct = Servicios.promociones().calcularDescuento(items, LocalDate.now());

        assertEquals(0, pct.compareTo(BigDecimal.ZERO));
    }

    @Test
    void promocionMixtaCalculaPorcentajeGlobal() {
        List<ItemVenta> items = List.of(item(producto("P-001"), 2), item(producto("P-003"), 1));

        BigDecimal pct = Servicios.promociones().calcularDescuento(items, LocalDate.now());

        assertEquals(0, pct.compareTo(new BigDecimal("2.55")));
    }

    @Test
    void descuentoSeLimitaAlImporteDelRenglon() {
        Servicios.promociones().guardar(new Promocion(0, "Bono loco", TipoPromocion.MONTO_FIJO,
                new BigDecimal("9999"), LocalDate.now(), LocalDate.now(), true, 1));

        List<ItemVenta> items = List.of(item(producto("P-001"), 2));

        BigDecimal pct = Servicios.promociones().calcularDescuento(items, LocalDate.now());

        assertEquals(0, pct.compareTo(new BigDecimal("100.00")));
    }

    // ------------------------------------------------------------------
    // VentaService
    // ------------------------------------------------------------------

    @Test
    void ventaSinProductosFalla() {
        assertThrows(ValidacionException.class, () -> {
            SolicitudVenta solicitud = new SolicitudVenta(List.of(), null, null,
                    BigDecimal.ZERO, MetodoPago.EFECTIVO, null, 1, "Cajero");
            Servicios.ventas().registrar(solicitud);
        });
    }

    @Test
    void ventaStockInsuficienteFalla() {
        Producto plumas = producto("P-002");
        assertThrows(ValidacionException.class,
                () -> registrar(item(plumas, 999), null, MetodoPago.EFECTIVO, new BigDecimal("99999")));
    }

    @Test
    void ventaDescuentaStockYRegistraPago() {
        Producto plumas = producto("P-002");
        int stockAntes = plumas.stock();

        Venta venta = registrar(item(plumas, 2), null, MetodoPago.EFECTIVO, new BigDecimal("200"));

        assertEquals(stockAntes - 2, producto("P-002").stock());
        assertEquals(1, Servicios.pagos().porVenta(venta.id()).size());
        assertEquals(EstadoVenta.COMPLETADA, venta.estado());
    }

    @Test
    void foliosNoSeRepiten() {
        Venta a = registrar(item(producto("P-001"), 1), null, MetodoPago.EFECTIVO, new BigDecimal("100"));
        Venta b = registrar(item(producto("P-001"), 1), null, MetodoPago.EFECTIVO, new BigDecimal("100"));

        assertNotEquals(a.folio(), b.folio());
    }

    @Test
    void cancelarReponeStockYMarcaCancelada() {
        Producto plumas = producto("P-002");
        Venta venta = registrar(item(plumas, 2), null, MetodoPago.EFECTIVO, new BigDecimal("200"));

        Servicios.ventas().cancelar(venta.id());

        assertEquals(8, producto("P-002").stock());
        assertEquals(EstadoVenta.CANCELADA, Servicios.ventas().porId(venta.id()).orElseThrow().estado());
    }

    /**
     * Caracteriza una limitación conocida: el servicio registra la venta con el
     * precio que llega en la {@code SolicitudVenta} en lugar de releerlo del
     * catálogo. Si se endurece {@code VentaServiceImpl}, esta prueba debe fallar.
     */
    @Test
    void ventaUsaElPrecioDeLaSolicitudNoElDelCatalogo() {
        Producto cuaderno = producto("P-001");
        assertTrue(cuaderno.precio().compareTo(new BigDecimal("0.01")) > 0);

        Venta venta = registrar(new ItemVenta(cuaderno.id(), cuaderno.codigo(),
                cuaderno.nombre(), new BigDecimal("0.01"), 1), null, MetodoPago.EFECTIVO, new BigDecimal("0.01"));

        assertEquals(0, venta.total().compareTo(new BigDecimal("0.01")));
    }

    // ------------------------------------------------------------------
    // ReporteService
    // ------------------------------------------------------------------

    @Test
    void resumenJornadaAgrupaPorMetodo() {
        registrar(item(producto("P-001"), 2), null, MetodoPago.EFECTIVO, new BigDecimal("100"));
        registrar(item(producto("P-003"), 1), null, MetodoPago.TARJETA, null);

        var resumen = Servicios.reportes().resumenJornada(LocalDate.now());

        assertEquals(2, resumen.numeroVentas());
        // El descuento por promoción NO lo calcula el servicio: viaja como
        // porcentaje en la SolicitudVenta (lo calcula la UI). Sin descuento:
        // P-001 x2 = 59.16 (51.00 + IVA) y P-003 = 172.84 (149.00 + IVA).
        assertEquals(0, resumen.totalVentas().compareTo(new BigDecimal("232.00")));
        assertEquals(0, resumen.totalEfectivo().compareTo(new BigDecimal("59.16")));
        assertEquals(0, resumen.totalTarjeta().compareTo(new BigDecimal("172.84")));
        assertEquals(0, resumen.totalTransferencia().compareTo(BigDecimal.ZERO));
    }

    @Test
    void productosMasVendidosOrdenaPorCantidad() {
        registrar(item(producto("P-001"), 3), null, MetodoPago.EFECTIVO, new BigDecimal("200"));
        registrar(item(producto("P-003"), 1), null, MetodoPago.EFECTIVO, new BigDecimal("300"));

        List<ProductoMasVendido> top = Servicios.reportes().productosMasVendidos(
                LocalDate.now(), LocalDate.now(), 10);

        assertEquals("P-001", top.get(0).codigo());
        assertEquals(3, top.get(0).cantidad());
    }

    @Test
    void ventasCanceladasNoCuentanEnReportes() {
        Venta venta = registrar(item(producto("P-003"), 1), null, MetodoPago.EFECTIVO, new BigDecimal("300"));
        assertEquals(0, Servicios.reportes().ingresos(LocalDate.now(), LocalDate.now())
                .compareTo(new BigDecimal("172.84")));

        Servicios.ventas().cancelar(venta.id());

        assertEquals(0, Servicios.reportes().ingresos(LocalDate.now(), LocalDate.now())
                .compareTo(BigDecimal.ZERO));
        assertTrue(Servicios.reportes().ventas(LocalDate.now(), LocalDate.now()).isEmpty());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Producto producto(String codigo) {
        return Servicios.productos().porCodigo(codigo).orElseThrow();
    }

    private ItemVenta item(Producto p, int cantidad) {
        return new ItemVenta(p.id(), p.codigo(), p.nombre(), p.precio(), cantidad);
    }

    private Venta registrar(ItemVenta item, BigDecimal descuentoPct, MetodoPago metodo, BigDecimal recibido) {
        return Servicios.ventas().registrar(new SolicitudVenta(
                List.of(item), null, null, descuentoPct, metodo, recibido, 1, "Administrador"));
    }
}
