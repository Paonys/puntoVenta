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
import com.novedadesale.puntoventa.repositories.PagoRepository;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.repositories.VentaRepository;
import com.novedadesale.puntoventa.services.VentaService;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final PagoRepository pagoRepo;

    public VentaServiceImpl(VentaRepository ventaRepo, ProductoRepository productoRepo, PagoRepository pagoRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.pagoRepo = pagoRepo;
    }

    @Override
    public Venta registrar(SolicitudVenta solicitud) {
        if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty()) {
            throw new ValidacionException("La venta debe tener al menos un producto");
        }
        for (ItemVenta item : solicitud.items()) {
            Producto p = productoRepo.porId(item.productoId())
                    .orElseThrow(() -> new ServiceException("Producto no encontrado: " + item.productoId()));
            if (p.stock() < item.cantidad()) {
                throw new ValidacionException("Stock insuficiente para \"" + p.nombre() + "\"");
            }
        }

        Totales totales = CalculadoraTotales.calcular(solicitud.items(), solicitud.descuentoPorcentaje());
        MetodoPago metodo = solicitud.metodoPago() == null ? MetodoPago.EFECTIVO : solicitud.metodoPago();
        BigDecimal recibido = solicitud.montoRecibido() == null ? totales.total() : solicitud.montoRecibido();
        BigDecimal cambio = recibido.subtract(totales.total());

        Venta venta = new Venta(0, siguienteFolio(), LocalDateTime.now(),
                solicitud.usuarioId(), solicitud.usuarioNombre(),
                solicitud.clienteId(), solicitud.clienteNombre(),
                List.copyOf(solicitud.items()),
                totales.subtotal(), totales.descuento(), totales.iva(), totales.total(),
                metodo, recibido, cambio, EstadoVenta.COMPLETADA);

        Venta guardada = ventaRepo.guardar(venta);

        for (ItemVenta item : solicitud.items()) {
            productoRepo.porId(item.productoId())
                    .ifPresent(p -> productoRepo.actualizarStock(p.id(), p.stock() - item.cantidad()));
        }
        pagoRepo.guardar(new Pago(0, guardada.id(), metodo, totales.total(), guardada.fecha()));
        return guardada;
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
        return String.format("V-%05d", ventaRepo.contar() + 1);
    }

    @Override
    public void cancelar(int ventaId) {
        Venta venta = ventaRepo.porId(ventaId)
                .orElseThrow(() -> new ServiceException("Venta no encontrada: " + ventaId));
        if (venta.estado() == EstadoVenta.CANCELADA) {
            return;
        }
        for (ItemVenta item : venta.items()) {
            productoRepo.porId(item.productoId())
                    .ifPresent(p -> productoRepo.actualizarStock(p.id(), p.stock() + item.cantidad()));
        }
        ventaRepo.actualizarEstado(ventaId, EstadoVenta.CANCELADA);
    }
}
