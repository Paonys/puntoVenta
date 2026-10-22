package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.HistorialProducto;

import java.util.List;

/** Acceso al historial de cambios de los productos. */
public interface HistorialProductoRepository {

    List<HistorialProducto> porProducto(int productoId);
}
