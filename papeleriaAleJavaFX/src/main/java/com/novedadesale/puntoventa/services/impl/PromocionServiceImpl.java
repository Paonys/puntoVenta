package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.repositories.ProductoRepository;
import com.novedadesale.puntoventa.repositories.PromocionRepository;
import com.novedadesale.puntoventa.services.PromocionService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository repo;
    private final ProductoRepository productoRepo;

    public PromocionServiceImpl(PromocionRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    @Override
    public List<Promocion> listar() {
        return repo.listar();
    }

    @Override
    public List<Promocion> activas(LocalDate fecha) {
        return repo.activas(fecha);
    }

    @Override
    public Promocion guardar(Promocion promocion) {
        Validadores.requerido(promocion.nombre(), "Nombre");
        return repo.guardar(promocion);
    }

    @Override
    public void eliminar(int id) {
        repo.eliminar(id);
    }

    @Override
    public BigDecimal calcularDescuento(List<ItemVenta> items, LocalDate fecha) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = items.stream()
                .map(ItemVenta::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotal.signum() == 0) {
            return BigDecimal.ZERO;
        }

        List<Promocion> vigentes = repo.activas(fecha);
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        for (ItemVenta item : items) {
            int categoriaId = productoRepo.porId(item.productoId())
                    .map(Producto::categoriaId).orElse(-1);

            BigDecimal mejor = BigDecimal.ZERO;
            for (Promocion promo : vigentes) {
                if (promo.categoriaId() != null && promo.categoriaId() != categoriaId) {
                    continue;
                }
                BigDecimal descuento = promo.tipo() == TipoPromocion.PORCENTAJE
                        ? item.importe().multiply(promo.valor()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : promo.valor();
                if (descuento.compareTo(mejor) > 0) {
                    mejor = descuento;
                }
            }
            if (mejor.compareTo(item.importe()) > 0) {
                mejor = item.importe();
            }
            descuentoTotal = descuentoTotal.add(mejor);
        }

        return descuentoTotal.multiply(BigDecimal.valueOf(100))
                .divide(subtotal, 2, RoundingMode.HALF_UP);
    }
}
