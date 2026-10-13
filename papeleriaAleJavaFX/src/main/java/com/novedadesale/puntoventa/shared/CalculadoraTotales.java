package com.novedadesale.puntoventa.shared;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.Totales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cálculo de totales de una venta. Única fuente de verdad para
 * subtotal, descuento, IVA y total: la usan tanto el POS como los servicios.
 */
public final class CalculadoraTotales {

    private CalculadoraTotales() {
    }

    /**
     * @param items               renglones de la venta.
     * @param descuentoPorcentaje descuento en porcentaje (0-100), puede ser {@code null}.
     */
    public static Totales calcular(List<ItemVenta> items, BigDecimal descuentoPorcentaje) {
        List<ItemVenta> renglones = items == null ? List.of() : items;

        BigDecimal subtotal = renglones.stream()
                .map(ItemVenta::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pct = descuentoPorcentaje == null ? BigDecimal.ZERO : descuentoPorcentaje;
        BigDecimal descuento = subtotal.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal base = subtotal.subtract(descuento);
        BigDecimal iva = base.multiply(AppConstants.IVA_TASA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(iva);

        return new Totales(subtotal, descuento, base, iva, total);
    }
}
