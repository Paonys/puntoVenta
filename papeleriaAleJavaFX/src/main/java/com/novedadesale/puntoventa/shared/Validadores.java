package com.novedadesale.puntoventa.shared;

import com.novedadesale.puntoventa.exceptions.ValidacionException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Validaciones reutilizables. Lanzan {@link ValidacionException}. */
public final class Validadores {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO =
            Pattern.compile("^[0-9+()\\-\\s]{7,25}$");

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

    public static void estrictamentePositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser mayor que cero");
        }
    }

    /** Valida un porcentaje (0-100). {@code null} se considera válido (sin descuento). */
    public static void rangoPorcentaje(BigDecimal valor, String campo) {
        if (valor == null) {
            return;
        }
        if (valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidacionException("El campo \"" + campo + "\" debe estar entre 0 y 100");
        }
    }

    /** Valida un correo si viene informado (vacío = válido). */
    public static void opcionalEmail(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El campo \"" + campo + "\" no es un correo válido");
        }
    }

    /** Valida un teléfono si viene informado (vacío = válido). */
    public static void opcionalTelefono(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        long digitos = valor.chars().filter(Character::isDigit).count();
        if (!TELEFONO.matcher(valor.trim()).matches() || digitos < 7) {
            throw new ValidacionException("El campo \"" + campo + "\" no es un teléfono válido");
        }
    }
}
