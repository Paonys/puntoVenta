package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Proveedor;

import java.util.List;
import java.util.Optional;

/** Administración de proveedores. */
public interface ProveedorService {

    List<Proveedor> listar();

    Optional<Proveedor> porId(int id);

    List<Proveedor> buscar(String texto);

    Proveedor guardar(Proveedor proveedor);

    void eliminar(int id);
}
