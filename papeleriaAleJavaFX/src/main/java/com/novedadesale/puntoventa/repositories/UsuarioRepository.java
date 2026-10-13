package com.novedadesale.puntoventa.repositories;

import com.novedadesale.puntoventa.models.Usuario;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de usuarios. */
public interface UsuarioRepository {

    List<Usuario> listar();

    Optional<Usuario> porId(int id);

    Optional<Usuario> porUsuario(String usuario);

    Usuario guardar(Usuario usuario);

    void eliminar(int id);
}
