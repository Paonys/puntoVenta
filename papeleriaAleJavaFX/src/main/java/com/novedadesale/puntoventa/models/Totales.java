package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;

/**
 * Totales calculados de una venta.
 *
 * @param subtotal             suma de los importes de los renglones.
 * @param descuento            monto de descuento aplicado.
 * @param subtotalConDescuento subtotal - descuento.
 * @param iva                  impuesto calculado sobre el subtotal con descuento.
 * @param total                subtotalConDescuento + iva.
 */
public record Totales(
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal subtotalConDescuento,
        BigDecimal iva,
        BigDecimal total) {
}
