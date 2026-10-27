package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.Pago;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Totales;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.repositories.ClienteRepository;
import com.novedadesale.puntoventa.repositories.PagoRepository;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.repositories.VentaRepository;
import com.novedadesale.puntoventa.services.PromocionService;
import com.novedadesale.puntoventa.services.VentaService;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;
import com.novedadesale.puntoventa.shared.Validadores;
import com.novedadesale.puntoventa.ui.Sesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final PagoRepository pagoRepo;
    private final PromocionService promociones;
    private final ClienteRepository clienteRepo;

    public VentaServiceImpl(VentaRepository ventaRepo, ProductoRepository productoRepo,
                            PagoRepository pagoRepo, PromocionService promociones,
                            ClienteRepository clienteRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.pagoRepo = pagoRepo;
        this.promociones = promociones;
        this.clienteRepo = clienteRepo;
    }

    @Override
    public Venta registrar(SolicitudVenta solicitud) {
        Autorizacion.exigirSesion("registrar ventas");
        if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty()) {
            throw new ValidacionException("La venta debe tener al menos un producto");
        }
        Validadores.rangoPorcentaje(solicitud.descuentoPorcentaje(), "Descuento");

        // El descuento manual adicional es exclusivo del administrador; el
        // descuento configurado del cliente lo resuelve el servicio.
        BigDecimal manualPct = solicitud.descuentoPorcentaje() == null
                ? BigDecimal.ZERO
                : solicitud.descuentoPorcentaje();
        if (manualPct.signum() > 0) {
            Autorizacion.exigirAdmin("aplicar descuento manual");
        }
        BigDecimal clientePct = solicitud.clienteId() == null
                ? BigDecimal.ZERO
                : clienteRepo.porId(solicitud.clienteId())
                        .map(c -> c.descuento() == null ? BigDecimal.ZERO : c.descuento())
                        .orElseThrow(() -> new ServiceException(
                                "Cliente no encontrado: " + solicitud.clienteId()));

        // 1) Agrupa cantidades; precios y stock se releen del catálogo, nunca
        //    se confía en lo que traiga la solicitud.
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemVenta pedido : solicitud.items()) {
            if (pedido.cantidad() <= 0) {
                throw new ValidacionException("La cantidad debe ser mayor a cero");
            }
            cantidades.merge(pedido.productoId(), pedido.cantidad(), Integer::sum);
        }

        // 2) Catálogo, validación de stock y folio se leen dentro de la misma
        //    transacción que los escribe, y el descuento de stock es aritmético
        //    en SQL (`stock >= ?`): dos procesos sobre el mismo archivo no
        //    pueden vender dos veces el mismo stock.
        //    shortcut: el folio sigue siendo MAX+1, así que con dos procesos a
        //    la vez el segundo muere por UNIQUE y debe reintentar; subir a
        //    BEGIN IMMEDIATE cuando haya terminales concurrentes.
        return BaseDatos.enTransaccion(() -> {
            List<ItemVenta> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> linea : cantidades.entrySet()) {
                Producto producto = productoRepo.porId(linea.getKey())
                        .orElseThrow(() -> new ServiceException("Producto no encontrado: " + linea.getKey()));
                if (producto.stock() < linea.getValue()) {
                    throw new ValidacionException("Stock insuficiente para \"" + producto.nombre() + "\"");
                }
                items.add(new ItemVenta(producto.id(), producto.codigo(), producto.nombre(),
                        producto.precio(), linea.getValue()));
            }

            // 3) Regla de negocio de descuentos: las promociones las resuelve el
            //    servicio (no la UI); se suman al descuento del cliente y al manual,
            //    con tope del 100%.
            BigDecimal promoPct = promociones.calcularDescuento(items, LocalDate.now());
            BigDecimal descuentoPct = promoPct.add(clientePct).add(manualPct)
                    .min(BigDecimal.valueOf(100));

            Totales totales = CalculadoraTotales.calcular(items, descuentoPct);
            MetodoPago metodo = solicitud.metodoPago() == null ? MetodoPago.EFECTIVO : solicitud.metodoPago();
            BigDecimal recibido = solicitud.montoRecibido() == null ? totales.total() : solicitud.montoRecibido();
            BigDecimal cambio = recibido.subtract(totales.total());

            // La identidad sale de la sesión, nunca de la solicitud.
            Venta venta = new Venta(0, siguienteFolio(), LocalDateTime.now(),
                    Sesion.id(), Sesion.nombre(),
                    solicitud.clienteId(), solicitud.clienteNombre(),
                    List.copyOf(items),
                    totales.subtotal(), totales.descuento(), totales.iva(), totales.total(),
                    metodo, recibido, cambio, EstadoVenta.COMPLETADA);

            // 4) Persistencia atómica: venta + renglones + stock + pago.
            Venta guardada = ventaRepo.guardar(venta);
            for (ItemVenta item : items) {
                if (!productoRepo.descontarStock(item.productoId(), item.cantidad())) {
                    throw new ValidacionException("Stock insuficiente para \"" + item.nombre() + "\"");
                }
            }
            pagoRepo.guardar(new Pago(0, guardada.id(), metodo, totales.total(), guardada.fecha()));
            return guardada;
        });
    }

    @Override
    public List<Venta> listar() {
        Autorizacion.exigirSesion("listar ventas");
        return ventaRepo.listar();
    }

    @Override
    public Optional<Venta> porId(int id) {
        Autorizacion.exigirSesion("consultar ventas");
        return ventaRepo.porId(id);
    }

    @Override
    public Optional<Venta> porFolio(String folio) {
        Autorizacion.exigirSesion("consultar ventas");
        return ventaRepo.porFolio(folio);
    }

    @Override
    public List<Venta> porFecha(LocalDate fecha) {
        Autorizacion.exigirSesion("consultar ventas");
        return ventaRepo.porFecha(fecha);
    }

    @Override
    public String siguienteFolio() {
        Autorizacion.exigirSesion("consultar folios");
        return String.format("V-%05d", ventaRepo.proximoNumero());
    }

    @Override
    public void cancelar(int ventaId) {
        Autorizacion.exigirAdmin("cancelar ventas");
        Venta venta = ventaRepo.porId(ventaId)
                .orElseThrow(() -> new ServiceException("Venta no encontrada: " + ventaId));
        if (venta.estado() == EstadoVenta.CANCELADA) {
            return;
        }
        BaseDatos.enTransaccion(() -> {
            for (ItemVenta item : venta.items()) {
                productoRepo.porId(item.productoId())
                        .ifPresent(p -> productoRepo.actualizarStock(p.id(), p.stock() + item.cantidad()));
            }
            ventaRepo.actualizarEstado(ventaId, EstadoVenta.CANCELADA);
            return null;
        });
    }
}
