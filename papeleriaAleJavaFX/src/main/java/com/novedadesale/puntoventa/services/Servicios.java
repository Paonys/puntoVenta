package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.services.impl.InMemoryCategoriaService;
import com.novedadesale.puntoventa.services.impl.InMemoryClienteService;
import com.novedadesale.puntoventa.services.impl.InMemoryPagoService;
import com.novedadesale.puntoventa.services.impl.InMemoryProductoService;
import com.novedadesale.puntoventa.services.impl.InMemoryPromocionService;
import com.novedadesale.puntoventa.services.impl.InMemoryProveedorService;
import com.novedadesale.puntoventa.services.impl.InMemoryReporteService;
import com.novedadesale.puntoventa.services.impl.InMemoryUsuarioService;
import com.novedadesale.puntoventa.services.impl.InMemoryVentaService;

/**
 * Punto único de acceso a los servicios de la aplicación (Service Locator).
 * <p>
 * CONTRATO CONGELADO: la UI ({@code controllers}) debe obtener los servicios
 * SIEMPRE desde aquí, nunca instanciando implementaciones directamente.
 * Así el Agente A puede cambiar las implementaciones (p. ej. a SQLite) sin
 * romper la UI.
 */
public final class Servicios {

    private static final CategoriaService CATEGORIAS = new InMemoryCategoriaService();
    private static final ProductoService PRODUCTOS = new InMemoryProductoService();
    private static final UsuarioService USUARIOS = new InMemoryUsuarioService();
    private static final ClienteService CLIENTES = new InMemoryClienteService();
    private static final ProveedorService PROVEEDORES = new InMemoryProveedorService();
    private static final PromocionService PROMOCIONES = new InMemoryPromocionService();
    private static final VentaService VENTAS = new InMemoryVentaService();
    private static final PagoService PAGOS = new InMemoryPagoService();
    private static final ReporteService REPORTES = new InMemoryReporteService();

    private Servicios() {
    }

    public static CategoriaService categorias() {
        return CATEGORIAS;
    }

    public static ProductoService productos() {
        return PRODUCTOS;
    }

    public static UsuarioService usuarios() {
        return USUARIOS;
    }

    public static ClienteService clientes() {
        return CLIENTES;
    }

    public static ProveedorService proveedores() {
        return PROVEEDORES;
    }

    public static PromocionService promociones() {
        return PROMOCIONES;
    }

    public static VentaService ventas() {
        return VENTAS;
    }

    public static PagoService pagos() {
        return PAGOS;
    }

    public static ReporteService reportes() {
        return REPORTES;
    }
}
