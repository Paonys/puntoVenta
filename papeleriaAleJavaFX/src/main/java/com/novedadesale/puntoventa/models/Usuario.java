package com.novedadesale.puntoventa.models;

/**
 * Usuario del sistema.
 *
 * @param passwordHash contraseña individual ya hasheada (ver {@code shared.Seguridad}).
 */
public record Usuario(
        int id,
        String nombre,
        String usuario,
        String passwordHash,
        Rol rol,
        boolean activo) {
}
