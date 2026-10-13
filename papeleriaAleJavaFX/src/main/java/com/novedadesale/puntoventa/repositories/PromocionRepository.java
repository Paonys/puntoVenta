package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Promocion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de promociones. */
public interface PromocionRepository {

    List<Promocion> listar();

    Optional<Promocion> porId(int id);

    List<Promocion> activas(LocalDate fecha);

    Promocion guardar(Promocion promocion);

    void eliminar(int id);
}
