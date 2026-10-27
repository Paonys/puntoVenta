package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.shared.Reloj;

import java.time.LocalDateTime;

/**
 * Cierre de sesión por inactividad (S12): cuenta desde la última actividad y
 * dispara la acción al vencer el plazo. Es pausable mientras haya un diálogo
 * modal abierto y se comprueba con {@link Reloj}, de modo que las pruebas no
 * esperan el tiempo real.
 */
public final class TemporizadorInactividad {

    private final long minutos;
    private final Runnable alVencer;

    private LocalDateTime ultimoActividad;
    private boolean activo;
    private boolean pausado;

    public TemporizadorInactividad(long minutos, Runnable alVencer) {
        this.minutos = minutos;
        this.alVencer = alVencer;
    }

    /** Empieza a contar desde ahora. */
    public void activar() {
        activo = true;
        pausado = false;
        registrarActividad();
    }

    public void detener() {
        activo = false;
        pausado = false;
    }

    public boolean activo() {
        return activo;
    }

    /** Reinicia la cuenta ante cada evento de teclado o ratón. */
    public void registrarActividad() {
        if (activo && !pausado) {
            ultimoActividad = Reloj.ahora();
        }
    }

    /** Pausa la cuenta (diálogo modal en pantalla); no la detiene. */
    public void pausar() {
        pausado = activo;
    }

    /** Reanuda la cuenta y vuelve a contar desde ahora. */
    public void reanudar() {
        if (pausado) {
            pausado = false;
            registrarActividad();
        }
    }

    public boolean expirado() {
        return activo && !pausado && ultimoActividad != null
                && !Reloj.ahora().isBefore(ultimoActividad.plusMinutes(minutos));
    }

    /** Comprueba el vencimiento; al dispararse deja de estar activo. */
    public void comprobar() {
        if (expirado()) {
            activo = false;
            alVencer.run();
        }
    }
}
