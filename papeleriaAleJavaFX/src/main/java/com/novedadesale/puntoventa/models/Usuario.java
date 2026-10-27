package com.novedadesale.puntoventa.models;

/**
 * Usuario del sistema.
 *
 * @param passwordHash contraseña individual ya hasheada (ver {@code shared.Seguridad}).
 * @param debeCambiarPassword obliga a cambiarla antes de operar (S8).
 * @param intentosFallidos intentos de login fallidos consecutivos (S10).
 * @param bloqueadoHasta fecha ISO del fin del bloqueo, o {@code null} (S10).
 */
public record Usuario(
        int id,
        String nombre,
        String usuario,
        String passwordHash,
        Rol rol,
        boolean activo,
        boolean debeCambiarPassword,
        int intentosFallidos,
        String bloqueadoHasta) {
}
