package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Pago;
import com.novedadesale.puntoventa.repositories.PagoRepository;
import com.novedadesale.puntoventa.services.PagoService;

import java.util.List;

public class PagoServiceImpl implements PagoService {

    private final PagoRepository repo;

    public PagoServiceImpl(PagoRepository repo) {
        this.repo = repo;
    }

    @Override
    public Pago registrar(Pago pago) {
        return repo.guardar(pago);
    }

    @Override
    public List<Pago> listar() {
        return repo.listar();
    }

    @Override
    public List<Pago> porVenta(int ventaId) {
        return repo.porVenta(ventaId);
    }
}
