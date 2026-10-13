package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.EstadoVenta;
import com.novedadesale.puntoventa.models.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de ventas (cabecera + renglones + pagos asociados). */
public interface VentaRepository {

    List<Venta> listar();

    List<Venta> porFecha(LocalDate fecha);

    List<Venta> entre(LocalDate desde, LocalDate hasta);

    Optional<Venta> porId(int id);

    Optional<Venta> porFolio(String folio);

    /** Inserta la venta y sus renglones; devuelve la venta con id y items persistidos. */
    Venta guardar(Venta venta);

    void actualizarEstado(int ventaId, EstadoVenta estado);

    /** Cantidad de ventas registradas (para el folio). */
    int contar();
}
