package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;

/** Sesión del usuario que inició sesión (estado en memoria de la UI). */
public final class Sesion {

    private static Usuario usuario;

    private Sesion() {
    }

    public static Usuario usuario() {
        return usuario;
    }

    public static void iniciar(Usuario u) {
        usuario = u;
    }

    public static void cerrar() {
        usuario = null;
    }

    public static boolean activa() {
        return usuario != null;
    }

    public static int id() {
        return usuario == null ? 0 : usuario.id();
    }

    public static String nombre() {
        return usuario == null ? "Invitado" : usuario.nombre();
    }

    public static Rol rol() {
        return usuario == null ? Rol.CAJERO : usuario.rol();
    }

    public static boolean esAdmin() {
        return usuario != null && usuario.rol() == Rol.ADMIN;
    }
}
