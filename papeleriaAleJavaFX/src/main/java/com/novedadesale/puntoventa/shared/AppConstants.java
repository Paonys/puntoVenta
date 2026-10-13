package com.novedadesale.puntoventa.shared;

import java.math.BigDecimal;
import java.util.Locale;

/** Constantes globales del sistema. Congeladas: cambiar aquí afecta a toda la app. */
public final class AppConstants {

    /** Tasa de IVA (16%). */
    public static final BigDecimal IVA_TASA = new BigDecimal("0.16");

    /** Umbral por debajo del cual un producto se considera con stock bajo. */
    public static final int UMBRAL_STOCK_BAJO = 10;

    /** Locale para formato de moneda/fecha (es-MX). */
    public static final Locale LOCALE = new Locale("es", "MX");

    private AppConstants() {
    }
}
