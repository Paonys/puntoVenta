package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Cliente;
import com.novedadesale.puntoventa.services.ClienteService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryClienteService implements ClienteService {

    private final Datos d = Datos.get();

    @Override
    public List<Cliente> listar() {
        return new ArrayList<>(d.clientes.values());
    }

    @Override
    public Optional<Cliente> porId(int id) {
        return Optional.ofNullable(d.clientes.get(id));
    }

    @Override
    public List<Cliente> buscar(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return d.clientes.values().stream()
                .filter(c -> q.isEmpty()
                        || c.nombre().toLowerCase(Locale.ROOT).contains(q)
                        || (c.telefono() != null && c.telefono().contains(q)))
                .toList();
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        String nombre = Validadores.requerido(cliente.nombre(), "Nombre");
        int id = cliente.id() > 0 ? cliente.id() : d.cliSeq.incrementAndGet();
        Cliente nuevo = new Cliente(id, nombre, cliente.telefono(), cliente.email(),
                cliente.descuento() == null ? BigDecimal.ZERO : cliente.descuento());
        d.clientes.put(id, nuevo);
        return nuevo;
    }

    @Override
    public void eliminar(int id) {
        d.clientes.remove(id);
    }
}
