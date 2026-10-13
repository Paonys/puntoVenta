package com.novedadesale.puntoventa.exceptions;

/** Error de negocio o de infraestructura de la capa de servicios. */
public class ServiceException extends RuntimeException {

    public ServiceException(String mensaje) {
        super(mensaje);
    }

    public ServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
