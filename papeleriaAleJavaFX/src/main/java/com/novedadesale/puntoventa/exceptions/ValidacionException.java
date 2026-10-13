package com.novedadesale.puntoventa.exceptions;

/** Datos de entrada inválidos (campo obligatorio, precio negativo, etc.). */
public class ValidacionException extends ServiceException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
