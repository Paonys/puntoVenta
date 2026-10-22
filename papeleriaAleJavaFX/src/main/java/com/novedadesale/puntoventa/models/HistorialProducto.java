package com.novedadesale.puntoventa.models;

/** Un cambio registrado en el historial de un producto. */
public record HistorialProducto(
        int id,
        int productoId,
        String accion,
        String usuario,
        String fecha,
        String detalle) {
}
