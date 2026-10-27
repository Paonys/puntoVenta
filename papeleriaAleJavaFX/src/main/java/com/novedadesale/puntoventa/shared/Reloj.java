package com.novedadesale.puntoventa.shared;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Hora actual del sistema, con un gancho para pruebas: si se fija un instante,
 * todos los lectores (bloqueo de cuenta, cierre por inactividad) usan ese valor
 * en lugar del reloj de la máquina, sin esperar minutos reales.
 */
public final class Reloj {

    private static LocalDateTime fijado;

    private Reloj() {
    }

    /** Devuelve el instante actual (o el fijado para pruebas). */
    public static LocalDateTime ahora() {
        return fijado != null ? fijado : LocalDateTime.now();
    }

    /** Fija un instante concreto; {@code null} devuelve al tiempo real. */
    public static void fijar(LocalDateTime momento) {
        fijado = momento;
    }

    /** Avanza el instante fijado (si no hay ninguno, parte del tiempo real). */
    public static void avanzar(Duration cuanto) {
        fijado = (fijado != null ? fijado : LocalDateTime.now()).plus(cuanto);
    }

    /** Libera la fijación y vuelve al tiempo real. */
    public static void liberar() {
        fijado = null;
    }
}
