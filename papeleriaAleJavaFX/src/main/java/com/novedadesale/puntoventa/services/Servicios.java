package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.repositories.CategoriaRepository;
import com.novedadesale.puntoventa.repositories.ClienteRepository;
import com.novedadesale.puntoventa.repositories.HistorialProductoRepository;
import com.novedadesale.puntoventa.repositories.PagoRepository;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.repositories.PromocionRepository;
import com.novedadesale.puntoventa.repositories.ProveedorRepository;
import com.novedadesale.puntoventa.repositories.UsuarioRepository;
import com.novedadesale.puntoventa.repositories.VentaRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteCategoriaRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteClienteRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteHistorialProductoRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqlitePagoRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteProductoRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqlitePromocionRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteProveedorRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteUsuarioRepository;
import com.novedadesale.puntoventa.repositories.sqlite.SqliteVentaRepository;
import com.novedadesale.puntoventa.services.impl.CategoriaServiceImpl;
import com.novedadesale.puntoventa.services.impl.ClienteServiceImpl;
import com.novedadesale.puntoventa.services.impl.PagoServiceImpl;
import com.novedadesale.puntoventa.services.impl.ProductoServiceImpl;
import com.novedadesale.puntoventa.services.impl.PromocionServiceImpl;
import com.novedadesale.puntoventa.services.impl.ProveedorServiceImpl;
import com.novedadesale.puntoventa.services.impl.ReporteServiceImpl;
import com.novedadesale.puntoventa.services.impl.UsuarioServiceImpl;
import com.novedadesale.puntoventa.services.impl.VentaServiceImpl;

/**
 * Punto único de acceso a los servicios de la aplicación (Service Locator).
 * <p>
 * CONTRATO CONGELADO: la UI ({@code controllers}) debe obtener los servicios
 * SIEMPRE desde aquí, nunca instanciando implementaciones directamente.
 * <p>
 * Persistencia actual: SQLite (ver {@code persistence.BaseDatos}).
 */
public final class Servicios {

    private static final CategoriaRepository CATEGORIA_REPO = new SqliteCategoriaRepository();
    private static final ProductoRepository PRODUCTO_REPO = new SqliteProductoRepository();
    private static final HistorialProductoRepository HISTORIAL_PRODUCTO_REPO = new SqliteHistorialProductoRepository();
    private static final ClienteRepository CLIENTE_REPO = new SqliteClienteRepository();
    private static final ProveedorRepository PROVEEDOR_REPO = new SqliteProveedorRepository();
    private static final UsuarioRepository USUARIO_REPO = new SqliteUsuarioRepository();
    private static final PromocionRepository PROMOCION_REPO = new SqlitePromocionRepository();
    private static final VentaRepository VENTA_REPO = new SqliteVentaRepository();
    private static final PagoRepository PAGO_REPO = new SqlitePagoRepository();

    private static final CategoriaService CATEGORIAS = new CategoriaServiceImpl(CATEGORIA_REPO);
    private static final ProductoService PRODUCTOS = new ProductoServiceImpl(PRODUCTO_REPO, HISTORIAL_PRODUCTO_REPO);
    private static final UsuarioService USUARIOS = new UsuarioServiceImpl(USUARIO_REPO);
    private static final ClienteService CLIENTES = new ClienteServiceImpl(CLIENTE_REPO);
    private static final ProveedorService PROVEEDORES = new ProveedorServiceImpl(PROVEEDOR_REPO);
    private static final PromocionService PROMOCIONES = new PromocionServiceImpl(PROMOCION_REPO, PRODUCTO_REPO);
    private static final VentaService VENTAS = new VentaServiceImpl(
            VENTA_REPO, PRODUCTO_REPO, PAGO_REPO, PROMOCIONES, CLIENTE_REPO);
    private static final PagoService PAGOS = new PagoServiceImpl(PAGO_REPO);
    private static final ReporteService REPORTES = new ReporteServiceImpl(VENTA_REPO);

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
