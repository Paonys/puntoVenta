package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;

/** Renglón de una venta. El importe se deriva de precio unitario x cantidad. */
public record ItemVenta(
        int productoId,
        String codigo,
        String nombre,
        BigDecimal precioUnitario,
        int cantidad) {

    public BigDecimal importe() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public ItemVenta conCantidad(int nuevaCantidad) {
        return new ItemVenta(productoId, codigo, nombre, precioUnitario, nuevaCantidad);
    }
}
