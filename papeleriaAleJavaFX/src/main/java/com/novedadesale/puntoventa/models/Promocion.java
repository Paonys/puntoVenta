package com.novedadesale.puntoventa.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Promoción vigente en un rango de fechas (p. ej. 10% útiles escolares,
 * 15% en regalos durante febrero).
 *
 * @param categoriaId categoría a la que aplica; {@code null} = todo el catálogo.
 */
public record Promocion(
        int id,
        String nombre,
        TipoPromocion tipo,
        BigDecimal valor,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activa,
        Integer categoriaId) {

    public boolean vigente(LocalDate fecha) {
        return activa
                && fechaInicio != null && fechaFin != null
                && !fecha.isBefore(fechaInicio)
                && !fecha.isAfter(fechaFin);
    }
}
