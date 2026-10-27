package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.shared.Reloj;
import com.novedadesale.puntoventa.ui.TemporizadorInactividad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** S12: cierre por inactividad, probado contra {@link Reloj} sin esperar minutos reales. */
class InactividadTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 27, 8, 0);

    @AfterEach
    void liberar() {
        Reloj.liberar();
    }

    @Test
    void disparaAlVencerYNoVuelveADisparar() {
        Reloj.fijar(INICIO);
        AtomicInteger disparos = new AtomicInteger();
        TemporizadorInactividad t = new TemporizadorInactividad(15, disparos::incrementAndGet);
        t.activar();

        t.comprobar();
        assertEquals(0, disparos.get());

        Reloj.avanzar(Duration.ofMinutes(14));
        t.comprobar();
        assertEquals(0, disparos.get());

        Reloj.avanzar(Duration.ofMinutes(1));
        t.comprobar();
        assertEquals(1, disparos.get());

        t.comprobar();
        assertEquals(1, disparos.get(), "una vez disparado ya no vuelve a cerrar");
    }

    @Test
    void laActividadReiniciaLaCuenta() {
        Reloj.fijar(INICIO);
        AtomicInteger disparos = new AtomicInteger();
        TemporizadorInactividad t = new TemporizadorInactividad(15, disparos::incrementAndGet);
        t.activar();

        Reloj.avanzar(Duration.ofMinutes(10));
        t.registrarActividad();
        Reloj.avanzar(Duration.ofMinutes(10));
        t.comprobar();
        assertEquals(0, disparos.get());

        Reloj.avanzar(Duration.ofMinutes(5));
        t.comprobar();
        assertEquals(1, disparos.get());
    }

    @Test
    void pausadoPorDialogoNoDisparaYAlReanudarVuelveAContar() {
        Reloj.fijar(INICIO);
        AtomicInteger disparos = new AtomicInteger();
        TemporizadorInactividad t = new TemporizadorInactividad(15, disparos::incrementAndGet);
        t.activar();

        t.pausar();
        Reloj.avanzar(Duration.ofMinutes(30));
        t.comprobar();
        assertEquals(0, disparos.get(), "con un diálogo abierto no debe cerrar la sesión");

        t.reanudar();
        Reloj.avanzar(Duration.ofMinutes(10));
        t.comprobar();
        assertEquals(0, disparos.get());

        Reloj.avanzar(Duration.ofMinutes(5));
        t.comprobar();
        assertEquals(1, disparos.get());
    }

    @Test
    void detenidoNoDispara() {
        Reloj.fijar(INICIO);
        AtomicInteger disparos = new AtomicInteger();
        TemporizadorInactividad t = new TemporizadorInactividad(15, disparos::incrementAndGet);
        t.activar();
        t.detener();

        Reloj.avanzar(Duration.ofHours(2));
        t.comprobar();
        assertEquals(0, disparos.get());
    }
}
