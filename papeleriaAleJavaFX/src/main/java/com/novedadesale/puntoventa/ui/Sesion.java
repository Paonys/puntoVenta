package com.novedadesale.puntoventa.ui;

import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.persistence.BaseDatos;

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
        registrarAuditoria(u == null ? "Sistema" : u.nombre());
    }

    public static void cerrar() {
        usuario = null;
        registrarAuditoria("Sistema");
    }

    private static void registrarAuditoria(String nombre) {
        try {
            BaseDatos.registrarUsuarioAuditoria(nombre);
        } catch (RuntimeException ignored) {
            // La auditoría no debe impedir iniciar o cerrar sesión.
        }
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
