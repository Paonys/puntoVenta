package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Producto;

import java.util.List;
import java.util.Optional;

/** Registro, consulta y actualización de productos e inventario. */
public interface ProductoService {

    List<Producto> listar();

    Optional<Producto> porId(int id);

    Optional<Producto> porCodigo(String codigo);

    /** Busca por nombre o código (texto vacío = todos). */
    List<Producto> buscar(String texto);

    List<Producto> stockBajo();

    /** Crea (id == 0) o actualiza un producto. */
    Producto guardar(Producto producto);

    void eliminar(int id);

    /** Suma (o resta, con delta negativo) existencias, validando stock suficiente. */
    void ajustarStock(int productoId, int delta);
}
