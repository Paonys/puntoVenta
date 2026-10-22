package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.exceptions.ValidacionException;
import com.novedadesale.puntoventa.models.HistorialProducto;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.repositories.HistorialProductoRepository;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.services.ProductoService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.List;
import java.util.Optional;

public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repo;
    private final HistorialProductoRepository historialRepo;

    public ProductoServiceImpl(ProductoRepository repo, HistorialProductoRepository historialRepo) {
        this.repo = repo;
        this.historialRepo = historialRepo;
    }

    @Override
    public List<Producto> listar() {
        return repo.listar();
    }

    @Override
    public Optional<Producto> porId(int id) {
        return repo.porId(id);
    }

    @Override
    public Optional<Producto> porCodigo(String codigo) {
        return repo.porCodigo(codigo);
    }

    @Override
    public List<Producto> buscar(String texto) {
        return repo.buscar(texto);
    }

    @Override
    public List<Producto> stockBajo() {
        return repo.stockBajo();
    }

    @Override
    public List<HistorialProducto> historial(int productoId) {
        Autorizacion.exigirAdmin("ver el historial de productos");
        return historialRepo.porProducto(productoId);
    }

    @Override
    public Producto guardar(Producto producto) {
        Autorizacion.exigirAdmin("guardar productos");
        Validadores.requerido(producto.codigo(), "Código");
        Validadores.requerido(producto.nombre(), "Nombre");
        Validadores.estrictamentePositivo(producto.precio(), "Precio");
        if (producto.stock() < 0) {
            throw new ValidacionException("El stock no puede ser negativo");
        }
        repo.porCodigo(producto.codigo()).ifPresent(existente -> {
            if (existente.id() != producto.id()) {
                throw new ServiceException("Ya existe un producto con el código \"" + producto.codigo() + "\"");
            }
        });
        return repo.guardar(producto);
    }

    @Override
    public void eliminar(int id) {
        Autorizacion.exigirAdmin("eliminar productos");
        repo.eliminar(id);
    }

    @Override
    public void ajustarStock(int productoId, int delta) {
        Autorizacion.exigirAdmin("ajustar stock");
        Producto p = repo.porId(productoId)
                .orElseThrow(() -> new ServiceException("Producto no encontrado: " + productoId));
        int nuevo = p.stock() + delta;
        if (nuevo < 0) {
            throw new ValidacionException("Stock insuficiente para \"" + p.nombre() + "\"");
        }
        repo.actualizarStock(productoId, nuevo);
    }
}
