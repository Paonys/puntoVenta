package com.novedadesale.puntoventa;

import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.shared.Seguridad;
import com.novedadesale.puntoventa.shared.Validadores;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de utilidades compartidas: hashing y validaciones. */
class SharedTest {

    @Test
    void hashUsaSaltYVerifica() {
        String h1 = Seguridad.hash("secreta");
        String h2 = Seguridad.hash("secreta");

        assertTrue(h1.startsWith("pbkdf2sha256$"));
        assertNotEquals(h1, h2, "El salt debe hacer distintos los hashes");
        assertTrue(Seguridad.verificar("secreta", h1));
        assertFalse(Seguridad.verificar("otra", h1));
        assertFalse(Seguridad.verificar(null, h1));
        assertFalse(Seguridad.verificar("secreta", null));
    }

    @Test
    void verificaHashLegadoSha256() throws Exception {
        String hex = String.format("%064x", new java.math.BigInteger(1,
                MessageDigest.getInstance("SHA-256").digest("admin123".getBytes(StandardCharsets.UTF_8))));
        assertTrue(Seguridad.verificar("admin123", hex));
        assertFalse(Seguridad.verificar("incorrecta", hex));
    }

    @Test
    void validaCorreoYTelefono() {
        assertDoesNotThrow(() -> Validadores.opcionalEmail("a@b.com", "Correo"));
        assertDoesNotThrow(() -> Validadores.opcionalEmail("", "Correo"));
        assertThrows(ValidacionException.class, () -> Validadores.opcionalEmail("no-es-correo", "Correo"));

        assertDoesNotThrow(() -> Validadores.opcionalTelefono("555-100-2000", "Teléfono"));
        assertThrows(ValidacionException.class, () -> Validadores.opcionalTelefono("abc", "Teléfono"));
    }

    @Test
    void validaNumericos() {
        assertThrows(ValidacionException.class,
                () -> Validadores.estrictamentePositivo(BigDecimal.ZERO, "Precio"));
        assertThrows(ValidacionException.class,
                () -> Validadores.rangoPorcentaje(new BigDecimal("101"), "Descuento"));
        assertDoesNotThrow(() -> Validadores.rangoPorcentaje(new BigDecimal("100"), "Descuento"));
    }
}
