package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comando de entrada para registrar una venta. El servicio se encarga de
 * calcular totales, cambio, folio y de descontar existencias.
 *
 * <p>La identidad de la venta sale de la sesión activa, no de este comando.</p>
 *
 * @param descuentoPorcentaje descuento manual adicional (0-100), exclusivo de
 *                            ADMIN. El descuento configurado del cliente y las
 *                            promociones vigentes los resuelve el propio
 *                            servicio a partir de {@code clienteId}.
 * @param montoRecibido       efectivo/importe entregado por el cliente (para el cambio).
 */
public record SolicitudVenta(
        List<ItemVenta> items,
        Integer clienteId,
        String clienteNombre,
        BigDecimal descuentoPorcentaje,
        MetodoPago metodoPago,
        BigDecimal montoRecibido) {
}
