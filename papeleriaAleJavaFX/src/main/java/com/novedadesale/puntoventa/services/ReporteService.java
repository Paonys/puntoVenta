package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.ProductoMasVendido;
import com.novedadesale.puntoventa.models.ResumenJornada;
import com.novedadesale.puntoventa.models.Venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Reportes de ventas, más vendidos y corte de caja. */
public interface ReporteService {

    ResumenJornada resumenJornada(LocalDate fecha);

    List<ProductoMasVendido> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite);

    /**
     * Dinero cobrado en el rango (inclusive). Mide lo mismo que
     * {@link #resumenJornada(LocalDate)}: las ventas COMPLETADA del periodo.
     */
    BigDecimal ingresos(LocalDate desde, LocalDate hasta);

    /**
     * Todas las ventas del rango (inclusive), COMPLETADA y CANCELADA, para el
     * listado y su columna de estado. El dinero no se mide aquí: ver
     * {@link #ingresos(LocalDate, LocalDate)}.
     */
    List<Venta> ventas(LocalDate desde, LocalDate hasta);
}
