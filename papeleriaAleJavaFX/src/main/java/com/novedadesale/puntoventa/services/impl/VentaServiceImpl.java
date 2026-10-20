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
import com.novedadesale.puntoventa.repositories.PagoRepository;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.repositories.VentaRepository;
import com.novedadesale.puntoventa.services.PromocionService;
import com.novedadesale.puntoventa.services.VentaService;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;
import com.novedadesale.puntoventa.shared.Validadores;

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

    public VentaServiceImpl(VentaRepository ventaRepo, ProductoRepository productoRepo,
                            PagoRepository pagoRepo, PromocionService promociones) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.pagoRepo = pagoRepo;
        this.promociones = promociones;
    }

    @Override
    public Venta registrar(SolicitudVenta solicitud) {
        if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty()) {
            throw new ValidacionException("La venta debe tener al menos un producto");
        }
        Validadores.rangoPorcentaje(solicitud.descuentoPorcentaje(), "Descuento");

        // 1) Agrupa cantidades y relee precios/nombres del catálogo: nunca se
        //    confía en el precio que traiga la solicitud.
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemVenta pedido : solicitud.items()) {
            if (pedido.cantidad() <= 0) {
                throw new ValidacionException("La cantidad debe ser mayor a cero");
            }
            cantidades.merge(pedido.productoId(), pedido.cantidad(), Integer::sum);
        }

        List<ItemVenta> items = new ArrayList<>();
        Map<Integer, Producto> catalogo = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> linea : cantidades.entrySet()) {
            Producto producto = productoRepo.porId(linea.getKey())
                    .orElseThrow(() -> new ServiceException("Producto no encontrado: " + linea.getKey()));
            if (producto.stock() < linea.getValue()) {
                throw new ValidacionException("Stock insuficiente para \"" + producto.nombre() + "\"");
            }
            catalogo.put(producto.id(), producto);
            items.add(new ItemVenta(producto.id(), producto.codigo(), producto.nombre(),
                    producto.precio(), linea.getValue()));
        }

        // 2) Regla de negocio de descuentos: las promociones las resuelve el
        //    servicio (no la UI) y se suman al descuento adicional del cliente,
        //    con tope del 100%.
        BigDecimal promoPct = promociones.calcularDescuento(items, LocalDate.now());
        BigDecimal clientePct = solicitud.descuentoPorcentaje() == null
                ? BigDecimal.ZERO
                : solicitud.descuentoPorcentaje();
        BigDecimal descuentoPct = promoPct.add(clientePct).min(BigDecimal.valueOf(100));

        Totales totales = CalculadoraTotales.calcular(items, descuentoPct);
        MetodoPago metodo = solicitud.metodoPago() == null ? MetodoPago.EFECTIVO : solicitud.metodoPago();
        BigDecimal recibido = solicitud.montoRecibido() == null ? totales.total() : solicitud.montoRecibido();
        BigDecimal cambio = recibido.subtract(totales.total());

        Venta venta = new Venta(0, siguienteFolio(), LocalDateTime.now(),
                solicitud.usuarioId(), solicitud.usuarioNombre(),
                solicitud.clienteId(), solicitud.clienteNombre(),
                List.copyOf(items),
                totales.subtotal(), totales.descuento(), totales.iva(), totales.total(),
                metodo, recibido, cambio, EstadoVenta.COMPLETADA);

        // 3) Persistencia atómica: venta + renglones + descuento de stock + pago.
        return BaseDatos.enTransaccion(() -> {
            Venta guardada = ventaRepo.guardar(venta);
            for (ItemVenta item : items) {
                Producto producto = catalogo.get(item.productoId());
                productoRepo.actualizarStock(producto.id(), producto.stock() - item.cantidad());
            }
            pagoRepo.guardar(new Pago(0, guardada.id(), metodo, totales.total(), guardada.fecha()));
            return guardada;
        });
    }

    @Override
    public List<Venta> listar() {
        return ventaRepo.listar();
    }

    @Override
    public Optional<Venta> porId(int id) {
        return ventaRepo.porId(id);
    }

    @Override
    public Optional<Venta> porFolio(String folio) {
        return ventaRepo.porFolio(folio);
    }

    @Override
    public List<Venta> porFecha(LocalDate fecha) {
        return ventaRepo.porFecha(fecha);
    }

    @Override
    public String siguienteFolio() {
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
