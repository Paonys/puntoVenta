package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.repositories.ProveedorRepository;
import com.novedadesale.puntoventa.services.ProveedorService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.List;
import java.util.Optional;

public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository repo;

    public ProveedorServiceImpl(ProveedorRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Proveedor> listar() {
        return repo.listar();
    }

    @Override
    public Optional<Proveedor> porId(int id) {
        return repo.porId(id);
    }

    @Override
    public List<Proveedor> buscar(String texto) {
        return repo.buscar(texto);
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        Validadores.requerido(proveedor.nombre(), "Nombre");
        Validadores.opcionalEmail(proveedor.email(), "Correo");
        Validadores.opcionalTelefono(proveedor.telefono(), "Teléfono");
        return repo.guardar(proveedor);
    }

    @Override
    public void eliminar(int id) {
        repo.eliminar(id);
    }
}
