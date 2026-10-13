package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Cliente;

import java.util.List;
import java.util.Optional;

/** Cartera de clientes y descuentos de cliente frecuente. */
public interface ClienteService {

    List<Cliente> listar();

    Optional<Cliente> porId(int id);

    List<Cliente> buscar(String texto);

    Cliente guardar(Cliente cliente);

    void eliminar(int id);
}
