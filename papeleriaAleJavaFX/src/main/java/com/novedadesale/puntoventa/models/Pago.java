package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago asociado a una venta. */
public record Pago(
        int id,
        int ventaId,
        MetodoPago metodo,
        BigDecimal monto,
        LocalDateTime fecha) {
}
