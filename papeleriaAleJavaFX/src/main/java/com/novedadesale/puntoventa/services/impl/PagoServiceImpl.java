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
        // La venta normal registra su pago dentro de su propia transacción; este
        // método solo queda para operaciones administrativas.
        Autorizacion.exigirAdmin("registrar pagos directos");
        return repo.guardar(pago);
    }

    @Override
    public List<Pago> listar() {
        Autorizacion.exigirSesion("listar pagos");
        return repo.listar();
    }

    @Override
    public List<Pago> porVenta(int ventaId) {
        Autorizacion.exigirSesion("consultar pagos");
        return repo.porVenta(ventaId);
    }
}
