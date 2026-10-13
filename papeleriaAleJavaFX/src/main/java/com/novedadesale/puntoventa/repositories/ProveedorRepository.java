package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Proveedor;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de proveedores. */
public interface ProveedorRepository {

    List<Proveedor> listar();

    Optional<Proveedor> porId(int id);

    List<Proveedor> buscar(String texto);

    Proveedor guardar(Proveedor proveedor);

    void eliminar(int id);
}
