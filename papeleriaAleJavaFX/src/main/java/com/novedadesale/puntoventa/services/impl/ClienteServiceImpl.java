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
        Autorizacion.exigirSesion("listar clientes");
        return repo.listar();
    }

    @Override
    public Optional<Cliente> porId(int id) {
        Autorizacion.exigirSesion("consultar clientes");
        return repo.porId(id);
    }

    @Override
    public List<Cliente> buscar(String texto) {
        Autorizacion.exigirSesion("buscar clientes");
        return repo.buscar(texto);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        Autorizacion.exigirAdmin("guardar clientes");
        Validadores.requerido(cliente.nombre(), "Nombre");
        Validadores.opcionalEmail(cliente.email(), "Correo");
        Validadores.opcionalTelefono(cliente.telefono(), "Teléfono");
        Validadores.rangoPorcentaje(cliente.descuento(), "Descuento");
        return repo.guardar(cliente);
    }

    @Override
    public void eliminar(int id) {
        Autorizacion.exigirAdmin("eliminar clientes");
        repo.eliminar(id);
    }
}
