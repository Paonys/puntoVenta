package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.models.Categoria;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.services.ProductoService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryProductoService implements ProductoService {

    private final Datos d = Datos.get();

    @Override
    public List<Producto> listar() {
        return new ArrayList<>(d.productos.values());
    }

    @Override
    public Optional<Producto> porId(int id) {
        return Optional.ofNullable(d.productos.get(id));
    }

    @Override
    public Optional<Producto> porCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return d.productos.values().stream()
                .filter(p -> codigo.equalsIgnoreCase(p.codigo()))
                .findFirst();
    }

    @Override
    public List<Producto> buscar(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return d.productos.values().stream()
                .filter(p -> q.isEmpty()
                        || p.nombre().toLowerCase(Locale.ROOT).contains(q)
                        || p.codigo().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    @Override
    public List<Producto> stockBajo() {
        return d.productos.values().stream().filter(Producto::stockBajo).toList();
    }

    @Override
    public Producto guardar(Producto producto) {
        String codigo = Validadores.requerido(producto.codigo(), "Código");
        String nombre = Validadores.requerido(producto.nombre(), "Nombre");
        Validadores.positivo(producto.precio(), "Precio");
        if (producto.stock() < 0) {
            throw new ValidacionException("El stock no puede ser negativo");
        }
        int id = producto.id() > 0 ? producto.id() : d.prodSeq.incrementAndGet();
        Categoria cat = d.categorias.get(producto.categoriaId());
        String catNombre = cat != null ? cat.nombre() : producto.categoria();
        Producto nueva = new Producto(id, codigo, nombre, producto.categoriaId(), catNombre,
                producto.precio() == null ? BigDecimal.ZERO : producto.precio(),
                producto.stock(), producto.stockMinimo());
        d.productos.put(id, nueva);
        return nueva;
    }

    @Override
    public void eliminar(int id) {
        d.productos.remove(id);
    }

    @Override
    public void ajustarStock(int productoId, int delta) {
        Producto p = d.productos.get(productoId);
        if (p == null) {
            throw new ServiceException("Producto no encontrado: " + productoId);
        }
        int nuevo = p.stock() + delta;
        if (nuevo < 0) {
            throw new ValidacionException("Stock insuficiente para \"" + p.nombre() + "\"");
        }
        d.productos.put(productoId, p.conStock(nuevo));
    }
}
