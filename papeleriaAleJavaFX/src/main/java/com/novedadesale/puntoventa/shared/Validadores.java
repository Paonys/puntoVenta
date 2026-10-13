package com.novedadesale.puntoventa.shared;

import com.novedadesale.puntoventa.exceptions.ValidacionException;

import java.math.BigDecimal;

/** Validaciones reutilizables. Lanzan {@link ValidacionException}. */
public final class Validadores {

    private Validadores() {
    }

    public static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio");
        }
        return valor.trim();
    }

    public static void positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede ser negativo");
        }
    }
}
