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
import com.novedadesale.puntoventa.services.VentaService;
import com.novedadesale.puntoventa.shared.CalculadoraTotales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryVentaService implements VentaService {

    private final Datos d = Datos.get();

    @Override
    public Venta registrar(SolicitudVenta solicitud) {
        if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty()) {
            throw new ValidacionException("La venta debe tener al menos un producto");
        }
        for (ItemVenta item : solicitud.items()) {
            Producto p = d.productos.get(item.productoId());
            if (p == null) {
                throw new ServiceException("Producto no encontrado: " + item.productoId());
            }
            if (p.stock() < item.cantidad()) {
                throw new ValidacionException("Stock insuficiente para \"" + p.nombre() + "\"");
            }
        }

        Totales t = CalculadoraTotales.calcular(solicitud.items(), solicitud.descuentoPorcentaje());
        int id = d.ventaSeq.incrementAndGet();
        String folio = String.format("V-%05d", id);
        MetodoPago metodo = solicitud.metodoPago() == null ? MetodoPago.EFECTIVO : solicitud.metodoPago();
        BigDecimal recibido = solicitud.montoRecibido() == null ? t.total() : solicitud.montoRecibido();
        BigDecimal cambio = recibido.subtract(t.total());

        Venta venta = new Venta(id, folio, LocalDateTime.now(),
                solicitud.usuarioId(), solicitud.usuarioNombre(),
                solicitud.clienteId(), solicitud.clienteNombre(),
                List.copyOf(solicitud.items()),
                t.subtotal(), t.descuento(), t.iva(), t.total(),
                metodo, recibido, cambio, EstadoVenta.COMPLETADA);

        for (ItemVenta item : solicitud.items()) {
            Producto p = d.productos.get(item.productoId());
            d.productos.put(p.id(), p.conStock(p.stock() - item.cantidad()));
        }
        d.ventas.add(venta);

        int pagoId = d.pagoSeq.incrementAndGet();
        d.pagos.add(new Pago(pagoId, venta.id(), metodo, t.total(), venta.fecha()));
        return venta;
    }

    @Override
    public List<Venta> listar() {
        return new ArrayList<>(d.ventas);
    }

    @Override
    public Optional<Venta> porId(int id) {
        return d.ventas.stream().filter(v -> v.id() == id).findFirst();
    }

    @Override
    public Optional<Venta> porFolio(String folio) {
        return d.ventas.stream().filter(v -> v.folio().equalsIgnoreCase(folio)).findFirst();
    }

    @Override
    public List<Venta> porFecha(LocalDate fecha) {
        return d.ventas.stream()
                .filter(v -> v.fecha().toLocalDate().equals(fecha))
                .toList();
    }

    @Override
    public String siguienteFolio() {
        return String.format("V-%05d", d.ventaSeq.get() + 1);
    }

    @Override
    public void cancelar(int ventaId) {
        for (int i = 0; i < d.ventas.size(); i++) {
            Venta v = d.ventas.get(i);
            if (v.id() != ventaId) {
                continue;
            }
            if (v.estado() == EstadoVenta.CANCELADA) {
                return;
            }
            for (ItemVenta item : v.items()) {
                Producto p = d.productos.get(item.productoId());
                if (p != null) {
                    d.productos.put(p.id(), p.conStock(p.stock() + item.cantidad()));
                }
            }
            d.ventas.set(i, new Venta(v.id(), v.folio(), v.fecha(), v.usuarioId(), v.usuarioNombre(),
                    v.clienteId(), v.clienteNombre(), v.items(), v.subtotal(), v.descuento(),
                    v.iva(), v.total(), v.metodoPago(), v.montoRecibido(), v.cambio(), EstadoVenta.CANCELADA));
            return;
        }
        throw new ServiceException("Venta no encontrada: " + ventaId);
    }
}
