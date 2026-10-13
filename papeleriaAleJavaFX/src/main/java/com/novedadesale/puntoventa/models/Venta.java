package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Venta registrada. Inmutable; los totales los calcula el servicio. */
public record Venta(
        int id,
        String folio,
        LocalDateTime fecha,
        int usuarioId,
        String usuarioNombre,
        Integer clienteId,
        String clienteNombre,
        List<ItemVenta> items,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal iva,
        BigDecimal total,
        MetodoPago metodoPago,
        BigDecimal montoRecibido,
        BigDecimal cambio,
        EstadoVenta estado) {
}
