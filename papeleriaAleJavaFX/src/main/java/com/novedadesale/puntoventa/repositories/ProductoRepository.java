package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Producto;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de productos. */
public interface ProductoRepository {

    List<Producto> listar();

    Optional<Producto> porId(int id);

    Optional<Producto> porCodigo(String codigo);

    List<Producto> buscar(String texto);

    List<Producto> stockBajo();

    Producto guardar(Producto producto);

    void eliminar(int id);

    void actualizarStock(int productoId, int nuevoStock);

    /**
     * Descuenta stock con aritmética en SQL ({@code stock = stock - ?}) y solo
     * si queda disponible. Devuelve {@code false} si no había stock suficiente
     * o el producto no existe.
     */
    boolean descontarStock(int productoId, int cantidad);
}
