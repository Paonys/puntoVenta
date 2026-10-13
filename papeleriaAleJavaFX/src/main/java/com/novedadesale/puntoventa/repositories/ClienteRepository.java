package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Cliente;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de clientes. */
public interface ClienteRepository {

    List<Cliente> listar();

    Optional<Cliente> porId(int id);

    List<Cliente> buscar(String texto);

    Cliente guardar(Cliente cliente);

    void eliminar(int id);
}
