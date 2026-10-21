package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Pago;

import java.util.List;

/** Acceso a datos de pagos. */
public interface PagoRepository {

    List<Pago> listar();

    List<Pago> porVenta(int ventaId);

    Pago guardar(Pago pago);
}
