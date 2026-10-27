package com.novedadesale.puntoventa.shared;

import java.math.BigDecimal;
import java.util.Locale;

/** Constantes globales del sistema. Congeladas: cambiar aquí afecta a toda la app. */
public final class AppConstants {

    /** Tasa de IVA (16%). */
    public static final BigDecimal IVA_TASA = new BigDecimal("0.16");

    /** Umbral por debajo del cual un producto se considera con stock bajo. */
    public static final int UMBRAL_STOCK_BAJO = 10;

    /** Intentos de login fallidos antes de bloquear la cuenta (S10). */
    public static final int INTENTOS_BLOQUEO = 5;

    /** Minutos que dura el bloqueo tras agotar los intentos (S10). */
    public static final int MINUTOS_BLOQUEO = 15;

    /** Minutos de inactividad antes de cerrar la sesión (S12). */
    public static final int MINUTOS_INACTIVIDAD = 15;

    /** Locale para formato de moneda/fecha (es-MX). */
    public static final Locale LOCALE = new Locale("es", "MX");

    private AppConstants() {
    }
}
