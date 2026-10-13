package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.services.CategoriaService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryCategoriaService implements CategoriaService {

    private final Datos d = Datos.get();

    @Override
    public List<Categoria> listar() {
        return new ArrayList<>(d.categorias.values());
    }

    @Override
    public Optional<Categoria> porId(int id) {
        return Optional.ofNullable(d.categorias.get(id));
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        String nombre = Validadores.requerido(categoria.nombre(), "Nombre");
        int id = categoria.id() > 0 ? categoria.id() : d.catSeq.incrementAndGet();
        Categoria nueva = new Categoria(id, nombre);
        d.categorias.put(id, nueva);
        return nueva;
    }

    @Override
    public void eliminar(int id) {
        d.categorias.remove(id);
    }
}
