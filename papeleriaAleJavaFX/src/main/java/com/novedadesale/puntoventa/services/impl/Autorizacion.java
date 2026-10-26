package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.ui.Sesion;

/**
 * Guardas de autorización para la capa de servicios (A8).
 *
 * <p>La UI ya oculta botones según el rol ({@code Componentes.soloAdmin}), pero
 * eso no es una frontera de seguridad: cualquier código podría invocar los
 * servicios directamente. Estas verificaciones se ejecutan SIEMPRE dentro de
 * los {@code *ServiceImpl} y lanzan {@link ServiceException} si el rol actual
 * no tiene permiso.</p>
 *
 * <p>Política: {@code ADMIN} crea/edita catálogo, gestiona usuarios y cancela
 * ventas; {@code CAJERO} solo vende y consulta.</p>
 */
final class Autorizacion {

    private Autorizacion() {
    }

    /** Exige una sesión activa, de cualquier rol. */
    static void exigirSesion(String accion) {
        if (!Sesion.activa()) {
            throw new ServiceException("Requiere sesión activa: " + accion);
        }
    }

    /** Exige una sesión activa con rol {@code ADMIN}. */
    static void exigirAdmin(String accion) {
        exigirSesion(accion);
        if (!Sesion.esAdmin()) {
            throw new ServiceException("Requiere sesión de administrador: " + accion);
        }
    }
}
