package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Categoria;

import java.util.List;
import java.util.Optional;

/** Catálogo de categorías / secciones. */
public interface CategoriaService {

    List<Categoria> listar();

    Optional<Categoria> porId(int id);

    /** Crea (id == 0) o actualiza una categoría. */
    Categoria guardar(Categoria categoria);

    void eliminar(int id);
}
