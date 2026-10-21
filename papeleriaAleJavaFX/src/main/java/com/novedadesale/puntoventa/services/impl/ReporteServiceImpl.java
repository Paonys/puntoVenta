package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.MetodoPago;
import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.ResumenJornada;
import com.novedadesale.puntoventa.models.Venta;
import com.novedadesale.puntoventa.repositories.VentaRepository;
import com.novedadesale.puntoventa.services.ReporteService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepo;

    public ReporteServiceImpl(VentaRepository ventaRepo) {
        this.ventaRepo = ventaRepo;
    }

    private List<Venta> completadasEntre(LocalDate desde, LocalDate hasta) {
        return ventaRepo.entre(desde, hasta).stream()
                .filter(v -> v.estado() == EstadoVenta.COMPLETADA)
                .toList();
    }

    @Override
    public ResumenJornada resumenJornada(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        List<Venta> ventas = completadasEntre(dia, dia);

        BigDecimal total = ventas.stream().map(Venta::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        int numero = ventas.size();
        BigDecimal promedio = numero == 0
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(numero), 2, RoundingMode.HALF_UP);

        return new ResumenJornada(dia, total, numero, promedio,
                totalPorMetodo(ventas, MetodoPago.EFECTIVO),
                totalPorMetodo(ventas, MetodoPago.TARJETA),
                totalPorMetodo(ventas, MetodoPago.TRANSFERENCIA));
    }

    private BigDecimal totalPorMetodo(List<Venta> ventas, MetodoPago metodo) {
        return ventas.stream()
                .filter(v -> v.metodoPago() == metodo)
                .map(Venta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<ProductoMasVendido> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite) {
        record Acumulado(String codigo, String nombre, int cantidad, BigDecimal importe) {
        }

        Map<Integer, Acumulado> acumulado = completadasEntre(desde, hasta).stream()
                .flatMap(v -> v.items().stream())
                .collect(Collectors.toMap(
                        ItemVenta::productoId,
                        it -> new Acumulado(it.codigo(), it.nombre(), it.cantidad(), it.importe()),
                        (a, b) -> new Acumulado(a.codigo(), a.nombre(),
                                a.cantidad() + b.cantidad(), a.importe().add(b.importe()))));

        return acumulado.values().stream()
                .sorted(Comparator.comparingInt(Acumulado::cantidad).reversed())
                .limit(limite <= 0 ? Long.MAX_VALUE : limite)
                .map(a -> new ProductoMasVendido(a.codigo(), a.nombre(), a.cantidad(), a.importe()))
                .toList();
    }

    @Override
    public BigDecimal ingresos(LocalDate desde, LocalDate hasta) {
        // Una sola fuente para el dinero: las ventas COMPLETADA del rango.
        // El pago se registra en la misma transacción que la venta, así que
        // ambos totales nunca pueden divergir.
        return completadasEntre(desde, hasta).stream()
                .map(Venta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Venta> ventas(LocalDate desde, LocalDate hasta) {
        return completadasEntre(desde, hasta);
    }
}
