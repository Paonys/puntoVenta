package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.Producto;
import com.novedadesale.puntoventa.models.Promocion;
import com.novedadesale.puntoventa.models.TipoPromocion;
import com.novedadesale.puntoventa.services.PromocionService;
import com.novedadesale.puntoventa.shared.Validadores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryPromocionService implements PromocionService {

    private final Datos d = Datos.get();

    @Override
    public List<Promocion> listar() {
        return new ArrayList<>(d.promociones.values());
    }

    @Override
    public List<Promocion> activas(LocalDate fecha) {
        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        return d.promociones.values().stream().filter(p -> p.vigente(f)).toList();
    }

    @Override
    public Promocion guardar(Promocion promocion) {
        String nombre = Validadores.requerido(promocion.nombre(), "Nombre");
        int id = promocion.id() > 0 ? promocion.id() : d.promoSeq.incrementAndGet();
        Promocion nueva = new Promocion(id, nombre, promocion.tipo(),
                promocion.valor() == null ? BigDecimal.ZERO : promocion.valor(),
                promocion.fechaInicio(), promocion.fechaFin(), promocion.activa(), promocion.categoriaId());
        d.promociones.put(id, nueva);
        return nueva;
    }

    @Override
    public void eliminar(int id) {
        d.promociones.remove(id);
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

        List<Promocion> vigentes = activas(fecha);
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        for (ItemVenta item : items) {
            Producto producto = d.productos.get(item.productoId());
            int categoriaId = producto == null ? -1 : producto.categoriaId();

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
