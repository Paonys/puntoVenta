package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Pago;

import java.time.LocalDate;
import java.util.List;

/** Acceso a datos de pagos. */
public interface PagoRepository {

    List<Pago> listar();

    List<Pago> porVenta(int ventaId);

    /**
     * Pagos de ventas COMPLETADA en el rango de fechas de la venta (inclusive).
     * Es la fuente única para calcular ingresos (A9).
     */
    List<Pago> entre(LocalDate desde, LocalDate hasta);

    Pago guardar(Pago pago);
}
