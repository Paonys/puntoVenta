package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.ui.Sesion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A8: autorización por rol en la capa de servicios. La UI oculta botones según
 * el rol, pero la frontera de seguridad real está en los servicios: un CAJERO
 * (o código sin sesión) no debe poder mutar catálogo, usuarios ni ventas.
 */
class AutorizacionTest {

    private Path db;

    @BeforeEach
    void preparar() throws Exception {
        db = Files.createTempFile("novedadesale-auth-", ".db");
        Files.deleteIfExists(db);
        BaseDatos.usarRuta(db.toString());
    }

    @AfterEach
    void limpiar() throws Exception {
        Sesion.cerrar();
        BaseDatos.cerrar();
        Files.deleteIfExists(db);
    }

    // ------------------------------------------------------------------
    // CAJERO: solo vende y consulta
    // ------------------------------------------------------------------

    @Test
    void cajeroNoPuedeGuardarProductos() {
        entrarComoCajero();

        assertThrows(ServiceException.class, () -> Servicios.productos().guardar(
                new com.novedadesale.puntoventa.models.Producto(0, "X-901", "Producto nuevo",
                        1, null, null, null, new BigDecimal("25.00"), 10, 2, null)));
    }

    @Test
    void cajeroNoPuedeAjustarStockNiEliminar() {
        entrarComoCajero();
        int productoId = Servicios.productos().porCodigo("P-001").orElseThrow().id();

        assertThrows(ServiceException.class, () -> Servicios.productos().ajustarStock(productoId, 10));
        assertThrows(ServiceException.class, () -> Servicios.productos().eliminar(productoId));
    }

    @Test
    void cajeroNoPuedeCancelarVentas() {
        Venta venta = registrarVentaComoAdmin();

        entrarComoCajero();
        assertThrows(ServiceException.class, () -> Servicios.ventas().cancelar(venta.id()));

        assertEquals(EstadoVenta.COMPLETADA, Servicios.ventas().porId(venta.id()).orElseThrow().estado());
    }

    @Test
    void cajeroNoPuedeGestionarUsuarios() {
        entrarComoCajero();

        assertThrows(ServiceException.class, () ->
                Servicios.usuarios().crear("Nuevo", "nuevo", "pass123", Rol.CAJERO));
        assertThrows(ServiceException.class, () -> Servicios.usuarios().cambiarPassword(1, "otra"));
        assertThrows(ServiceException.class, () -> Servicios.usuarios().eliminar(1));
    }

    @Test
    void cajeroNoPuedeGestionarClientesOfertasNiProveedores() {
        entrarComoCajero();

        assertThrows(ServiceException.class, () -> Servicios.clientes().guardar(
                new com.novedadesale.puntoventa.models.Cliente(0, "Sin Permiso", null, null, BigDecimal.ZERO)));
        assertThrows(ServiceException.class, () -> Servicios.proveedores().guardar(
                new com.novedadesale.puntoventa.models.Proveedor(0, "Sin Permiso", null, null, null)));
    }

    @Test
    void cajeroSiPuedeVenderYConsultar() {
        entrarComoCajero();

        Venta venta = Servicios.ventas().registrar(new SolicitudVenta(
                List.of(item("P-001", 1)), null, null, null,
                MetodoPago.EFECTIVO, new BigDecimal("100.00"), Sesion.id(), Sesion.nombre()));

        assertEquals(EstadoVenta.COMPLETADA, venta.estado());
        assertFalse(Servicios.productos().listar().isEmpty());
        assertFalse(Servicios.reportes().ventas(java.time.LocalDate.now(), java.time.LocalDate.now()).isEmpty());
    }

    // ------------------------------------------------------------------
    // ADMIN: sí puede
    // ------------------------------------------------------------------

    @Test
    void adminSiPuedeGestionarCatalogoIVentas() {
        entrarComoAdmin();

        var producto = Servicios.productos().guardar(new com.novedadesale.puntoventa.models.Producto(
                0, "X-902", "Producto admin", 1, null, null, null, new BigDecimal("30.00"), 5, 1, null));
        assertTrue(Servicios.productos().porCodigo("X-902").isPresent());

        Venta venta = registrarVentaComoAdmin();
        Servicios.ventas().cancelar(venta.id());
        assertEquals(EstadoVenta.CANCELADA, Servicios.ventas().porId(venta.id()).orElseThrow().estado());

        Servicios.productos().eliminar(producto.id());
        assertTrue(Servicios.productos().porId(producto.id()).isEmpty());
    }

    // ------------------------------------------------------------------
    // Invariante: el sistema nunca se queda sin ADMIN activo
    // ------------------------------------------------------------------

    @Test
    void noSePuedeEliminarLaPropiaCuenta() {
        entrarComoAdmin();
        int miId = Sesion.id();

        assertThrows(ServiceException.class, () -> Servicios.usuarios().eliminar(miId));
    }

    @Test
    void noSePuedeDesactivarAlUnicoAdminActivo() {
        entrarComoAdmin();
        Usuario admin = Servicios.usuarios().porId(Sesion.id()).orElseThrow();

        assertThrows(ServiceException.class, () -> Servicios.usuarios().actualizar(
                admin.id(), admin.nombre(), admin.usuario(), Rol.ADMIN, false, null));
        assertTrue(Servicios.usuarios().porId(admin.id()).orElseThrow().activo());
    }

    @Test
    void noSePuedeDegradarAlUnicoAdminActivo() {
        entrarComoAdmin();
        Usuario admin = Servicios.usuarios().porId(Sesion.id()).orElseThrow();

        assertThrows(ServiceException.class, () -> Servicios.usuarios().actualizar(
                admin.id(), admin.nombre(), admin.usuario(), Rol.CAJERO, true, null));
        assertEquals(Rol.ADMIN, Servicios.usuarios().porId(admin.id()).orElseThrow().rol());
    }

    @Test
    void conDosAdminsSiSePuedeDesactivarAlPrimero() {
        entrarComoAdmin();
        Usuario primero = Servicios.usuarios().porId(Sesion.id()).orElseThrow();
        Servicios.usuarios().crear("Segundo Admin", "admin2", "pass2", Rol.ADMIN);

        Servicios.usuarios().actualizar(primero.id(), primero.nombre(), primero.usuario(),
                Rol.ADMIN, false, null);

        assertFalse(Servicios.usuarios().porId(primero.id()).orElseThrow().activo());
        assertTrue(Servicios.usuarios().listar().stream()
                .anyMatch(u -> u.rol() == Rol.ADMIN && u.activo()));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void entrarComoAdmin() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());
    }

    private void entrarComoCajero() {
        Sesion.iniciar(Servicios.usuarios().autenticar("caja1", "caja123").orElseThrow());
    }

    private Venta registrarVentaComoAdmin() {
        entrarComoAdmin();
        return Servicios.ventas().registrar(new SolicitudVenta(
                List.of(item("P-001", 1)), null, null, null,
                MetodoPago.EFECTIVO, new BigDecimal("100.00"), Sesion.id(), Sesion.nombre()));
    }

    private ItemVenta item(String codigo, int cantidad) {
        var p = Servicios.productos().porCodigo(codigo).orElseThrow();
        return new ItemVenta(p.id(), p.codigo(), p.nombre(), p.precio(), cantidad);
    }
}
