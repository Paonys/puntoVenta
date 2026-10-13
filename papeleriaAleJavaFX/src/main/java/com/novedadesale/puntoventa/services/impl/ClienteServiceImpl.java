package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.repositories.ClienteRepository;
import com.novedadesale.puntoventa.services.ClienteService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.List;
import java.util.Optional;

public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repo;

    public ClienteServiceImpl(ClienteRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Cliente> listar() {
        return repo.listar();
    }

    @Override
    public Optional<Cliente> porId(int id) {
        return repo.porId(id);
    }

    @Override
    public List<Cliente> buscar(String texto) {
        return repo.buscar(texto);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        Validadores.requerido(cliente.nombre(), "Nombre");
        return repo.guardar(cliente);
    }

    @Override
    public void eliminar(int id) {
        repo.eliminar(id);
    }
}
