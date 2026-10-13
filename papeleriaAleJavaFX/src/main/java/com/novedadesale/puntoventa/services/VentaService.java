package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.SolicitudVenta;
import com.novedadesale.puntoventa.models.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Registro y consulta de ventas. */
public interface VentaService {

    /**
     * Registra una venta: calcula totales y cambio, asigna folio y
     * descuenta existencias. Lanza ValidacionException si falta stock.
     */
    Venta registrar(SolicitudVenta solicitud);

    List<Venta> listar();

    Optional<Venta> porId(int id);

    Optional<Venta> porFolio(String folio);

    List<Venta> porFecha(LocalDate fecha);

    /** Siguiente folio disponible (p. ej. V-00001). */
    String siguienteFolio();

    void cancelar(int ventaId);
}
