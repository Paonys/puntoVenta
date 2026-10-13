package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;

/**
 * Producto del catálogo. El precio se maneja con {@link BigDecimal}
 * (nunca double) según la recomendación de la documentación del proyecto.
 */
public record Producto(
        int id,
        String codigo,
        String nombre,
        int categoriaId,
        String categoria,
        BigDecimal precio,
        int stock,
        int stockMinimo) {

    public boolean agotado() {
        return stock <= 0;
    }

    public boolean stockBajo() {
        return stock > 0 && stock <= stockMinimo;
    }

    public Producto conStock(int nuevoStock) {
        return new Producto(id, codigo, nombre, categoriaId, categoria, precio, nuevoStock, stockMinimo);
    }
}
