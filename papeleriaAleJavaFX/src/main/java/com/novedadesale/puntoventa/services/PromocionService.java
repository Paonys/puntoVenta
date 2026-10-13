package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.ItemVenta;
import com.novedadesale.puntoventa.models.Promocion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Ofertas y reglas de descuento por temporada. */
public interface PromocionService {

    List<Promocion> listar();

    List<Promocion> activas(LocalDate fecha);

    Promocion guardar(Promocion promocion);

    void eliminar(int id);

    /** Descuento (porcentaje) que aplica a un conjunto de renglones según las promociones vigentes. */
    BigDecimal calcularDescuento(List<ItemVenta> items, LocalDate fecha);
}
