package com.novedadesale.puntoventa.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

/** Formato y parseo de valores monetarios. */
public final class Formato {

    private Formato() {
    }

    /** Formatea un monto como moneda es-MX. */
    public static String money(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        return NumberFormat.getCurrencyInstance(AppConstants.LOCALE).format(v);
    }

    /** Parsea texto a BigDecimal (2 decimales). Devuelve 0 ante texto inválido. */
    public static BigDecimal decimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
