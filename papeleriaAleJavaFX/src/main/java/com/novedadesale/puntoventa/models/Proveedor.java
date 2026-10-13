package com.novedadesale.puntoventa.models;

/** Proveedor de mercancía. */
public record Proveedor(
        int id,
        String nombre,
        String contacto,
        String telefono,
        String email) {
}
