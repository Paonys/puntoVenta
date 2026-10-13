package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Proveedor;
import com.novedadesale.puntoventa.services.ProveedorService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryProveedorService implements ProveedorService {

    private final Datos d = Datos.get();

    @Override
    public List<Proveedor> listar() {
        return new ArrayList<>(d.proveedores.values());
    }

    @Override
    public Optional<Proveedor> porId(int id) {
        return Optional.ofNullable(d.proveedores.get(id));
    }

    @Override
    public List<Proveedor> buscar(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return d.proveedores.values().stream()
                .filter(p -> q.isEmpty() || p.nombre().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        String nombre = Validadores.requerido(proveedor.nombre(), "Nombre");
        int id = proveedor.id() > 0 ? proveedor.id() : d.provSeq.incrementAndGet();
        Proveedor nuevo = new Proveedor(id, nombre, proveedor.contacto(),
                proveedor.telefono(), proveedor.email());
        d.proveedores.put(id, nuevo);
        return nuevo;
    }

    @Override
    public void eliminar(int id) {
        d.proveedores.remove(id);
    }
}
