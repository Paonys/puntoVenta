package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.persistence.BaseDatos;
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.shared.AppConstants;
import com.novedadesale.puntoventa.shared.Reloj;
import com.novedadesale.puntoventa.ui.Sesion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fase 2 (S9–S11): cambio obligatorio, bloqueo por intentos y política de contraseñas. */
class CredencialesTest {

    private Path db;

    @BeforeEach
    void preparar() throws Exception {
        db = Files.createTempFile("novedadesale-cred-", ".db");
        Files.deleteIfExists(db);
        BaseDatos.usarRuta(db.toString());
        Reloj.liberar();
    }

    @AfterEach
    void limpiar() throws Exception {
        Sesion.cerrar();
        Reloj.liberar();
        BaseDatos.cerrar();
        Files.deleteIfExists(db);
    }

    // ------------------------------------------------------------------
    // S10: bloqueo por intentos fallidos
    // ------------------------------------------------------------------

    @Test
    void cincoFallosBloqueanYAlVencerElBloqueoEntra() {
        for (int i = 1; i <= AppConstants.INTENTOS_BLOQUEO; i++) {
            assertFalse(Servicios.usuarios().autenticar("caja1", "mala" + i).isPresent());
        }

        ServiceException bloqueado = assertThrows(ServiceException.class,
                () -> Servicios.usuarios().autenticar("caja1", "caja123"));
        assertTrue(bloqueado.getMessage().toLowerCase().contains("bloqueada"));

        Reloj.avanzar(Duration.ofMinutes(AppConstants.MINUTOS_BLOQUEO + 1));
        assertTrue(Servicios.usuarios().autenticar("caja1", "caja123").isPresent());

        // Tras entrar, los fallos y el bloqueo quedan limpios.
        Sesion.iniciar(Servicios.usuarios().autenticar("caja1", "caja123").orElseThrow());
        var caja = Servicios.usuarios().porId(Sesion.id()).orElseThrow();
        assertNull(caja.bloqueadoHasta());
        assertEquals(0, caja.intentosFallidos());
    }

    @Test
    void loginCorrectoAntesDelLimiteLimpiaLosFallos() {
        assertFalse(Servicios.usuarios().autenticar("caja1", "mala1").isPresent());
        assertFalse(Servicios.usuarios().autenticar("caja1", "mala2").isPresent());

        Sesion.iniciar(Servicios.usuarios().autenticar("caja1", "caja123").orElseThrow());
        var caja = Servicios.usuarios().porId(Sesion.id()).orElseThrow();
        assertEquals(0, caja.intentosFallidos());
        assertNull(caja.bloqueadoHasta());
    }

    // ------------------------------------------------------------------
    // S9: cambio de la contraseña propia
    // ------------------------------------------------------------------

    @Test
    void cambioPropioValidaLaActualYLimpiaLaBandera() {
        Sesion.iniciar(Servicios.usuarios().autenticar("caja1", "caja123").orElseThrow());

        assertThrows(ServiceException.class,
                () -> Servicios.usuarios().cambiarPasswordPropia("otra", "CajaNueva1"));
        assertThrows(ServiceException.class,
                () -> Servicios.usuarios().cambiarPasswordPropia("caja123", "corta"));

        Servicios.usuarios().cambiarPasswordPropia("caja123", "CajaNueva1");
        assertFalse(Servicios.usuarios().porId(Sesion.id()).orElseThrow().debeCambiarPassword());

        assertThrows(ServiceException.class,
                () -> Servicios.usuarios().cambiarPasswordPropia("CajaNueva1", "CajaNueva1"));

        assertFalse(Servicios.usuarios().autenticar("caja1", "caja123").isPresent());
        assertTrue(Servicios.usuarios().autenticar("caja1", "CajaNueva1").isPresent());
    }

    @Test
    void sinSesionNoSePuedeCambiarLaPropia() {
        assertThrows(ServiceException.class,
                () -> Servicios.usuarios().cambiarPasswordPropia("caja123", "CajaNueva1"));
    }

    // ------------------------------------------------------------------
    // S11: política de contraseñas
    // ------------------------------------------------------------------

    @Test
    void politicaRechazaClavesDebilesYSembradas() {
        Sesion.iniciar(Servicios.usuarios().autenticar("admin", "admin123").orElseThrow());

        assertThrows(ValidacionException.class,
                () -> Servicios.usuarios().crear("Corta", "corta1", "pass", Rol.CAJERO));
        assertThrows(ValidacionException.class,
                () -> Servicios.usuarios().crear("Sin Digito", "sinnum1", "soloLetras", Rol.CAJERO));
        assertThrows(ValidacionException.class,
                () -> Servicios.usuarios().crear("Sembrada", "sembr1", "admin123", Rol.CAJERO));
        assertThrows(ValidacionException.class,
                () -> Servicios.usuarios().crear("Igual", "usuario1", "usuario1", Rol.CAJERO));

        assertTrue(Servicios.usuarios().crear("Válida", "valida1", "Clave123", Rol.CAJERO)
                .debeCambiarPassword());
    }
}
