package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Categoria;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de categorías. */
public interface CategoriaRepository {

    List<Categoria> listar();

    Optional<Categoria> porId(int id);

    /** Inserta (id <= 0) o actualiza; devuelve la categoría con su id. */
    Categoria guardar(Categoria categoria);

    void eliminar(int id);
}
