package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando de entrada para registrar una venta. El servicio se encarga de
 * calcular totales, cambio, folio y de descontar existencias.
 *
 * @param descuentoPorcentaje descuento adicional del cliente (0-100). Las
 *                            promociones vigentes las resuelve el propio
 *                            servicio y se suman a este porcentaje.
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
