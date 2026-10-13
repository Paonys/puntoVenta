package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resumen de ventas de una jornada (corte de caja). */
public record ResumenJornada(
        LocalDate fecha,
        BigDecimal totalVentas,
        int numeroVentas,
        BigDecimal ticketPromedio,
        BigDecimal totalEfectivo,
        BigDecimal totalTarjeta,
        BigDecimal totalTransferencia) {
}
