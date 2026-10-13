package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;

/**
 * Cliente de la tienda.
 *
 * @param descuento porcentaje de descuento permanente (0-100), para clientes frecuentes.
 */
public record Cliente(
        int id,
        String nombre,
        String telefono,
        String email,
        BigDecimal descuento) {
}
