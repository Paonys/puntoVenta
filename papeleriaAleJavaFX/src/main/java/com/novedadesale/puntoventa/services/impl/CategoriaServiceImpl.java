package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.repositories.CategoriaRepository;
import com.novedadesale.puntoventa.services.CategoriaService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.List;
import java.util.Optional;

public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repo;

    public CategoriaServiceImpl(CategoriaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Categoria> listar() {
        return repo.listar();
    }

    @Override
    public Optional<Categoria> porId(int id) {
        return repo.porId(id);
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        String nombre = Validadores.requerido(categoria.nombre(), "Nombre");
        return repo.guardar(new Categoria(categoria.id(), nombre));
    }

    @Override
    public void eliminar(int id) {
        repo.eliminar(id);
    }
}
