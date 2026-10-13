package com.novedadesale.puntoventa.services;

import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;

import java.util.List;
import java.util.Optional;

/** Inicio de sesión y control de usuarios (contraseñas individuales). */
public interface UsuarioService {

    /** Devuelve el usuario si las credenciales son válidas y está activo. */
    Optional<Usuario> autenticar(String usuario, String password);

    List<Usuario> listar();

    Optional<Usuario> porId(int id);

    Usuario crear(String nombre, String usuario, String password, Rol rol);

    /** Actualiza datos; si {@code passwordOpcional} es nulo o vacío, conserva la contraseña. */
    Usuario actualizar(int id, String nombre, String usuario, Rol rol, boolean activo, String passwordOpcional);

    void cambiarPassword(int id, String password);

    void eliminar(int id);
}
