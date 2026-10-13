package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando de entrada para registrar una venta. El servicio se encarga de
 * calcular totales, cambio, folio y de descontar existencias.
 *
 * @param descuentoPorcentaje porcentaje de descuento (0-100).
 * @param montoRecibido       efectivo/importe entregado por el cliente (para el cambio).
 */
public record SolicitudVenta(
        List<ItemVenta> items,
        Integer clienteId,
        String clienteNombre,
        BigDecimal descuentoPorcentaje,
        MetodoPago metodoPago,
        BigDecimal montoRecibido,
        int usuarioId,
        String usuarioNombre) {
}
