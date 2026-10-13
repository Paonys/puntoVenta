package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Pago;

import java.util.List;

/** Registro de pagos. */
public interface PagoService {

    Pago registrar(Pago pago);

    List<Pago> listar();

    List<Pago> porVenta(int ventaId);
}
