package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;

/** Producto con su cantidad e importe vendidos (para reportes). */
public record ProductoMasVendido(
        String codigo,
        String nombre,
        int cantidad,
        BigDecimal importe) {
}
