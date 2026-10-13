package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Pago;
import com.novedadesale.puntoventa.services.PagoService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryPagoService implements PagoService {

    private final Datos d = Datos.get();

    @Override
    public Pago registrar(Pago pago) {
        int id = pago.id() > 0 ? pago.id() : d.pagoSeq.incrementAndGet();
        Pago nuevo = new Pago(id, pago.ventaId(), pago.metodo(), pago.monto(),
                pago.fecha() == null ? LocalDateTime.now() : pago.fecha());
        d.pagos.add(nuevo);
        return nuevo;
    }

    @Override
    public List<Pago> listar() {
        return new ArrayList<>(d.pagos);
    }

    @Override
    public List<Pago> porVenta(int ventaId) {
        return d.pagos.stream().filter(p -> p.ventaId() == ventaId).toList();
    }
}
