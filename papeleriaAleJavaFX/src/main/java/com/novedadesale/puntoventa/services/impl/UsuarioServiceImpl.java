package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.repositories.UsuarioRepository;
import com.novedadesale.puntoventa.services.UsuarioService;
import com.novedadesale.puntoventa.shared.Seguridad;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.List;
import java.util.Optional;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioServiceImpl(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Usuario> autenticar(String usuario, String password) {
        if (usuario == null || password == null) {
            return Optional.empty();
        }
        return repo.porUsuario(usuario.trim())
                .filter(Usuario::activo)
                .filter(u -> Seguridad.verificar(password, u.passwordHash()));
    }

    @Override
    public List<Usuario> listar() {
        return repo.listar();
    }

    @Override
    public Optional<Usuario> porId(int id) {
        return repo.porId(id);
    }

    @Override
    public Usuario crear(String nombre, String usuario, String password, Rol rol) {
        Validadores.requerido(nombre, "Nombre");
        Validadores.requerido(usuario, "Usuario");
        Validadores.requerido(password, "Contraseña");
        repo.porUsuario(usuario.trim()).ifPresent(u -> {
            throw new ServiceException("Ya existe el usuario \"" + usuario + "\"");
        });
        return repo.guardar(new Usuario(0, nombre.trim(), usuario.trim(),
                Seguridad.hash(password), rol == null ? Rol.CAJERO : rol, true));
    }

    @Override
    public Usuario actualizar(int id, String nombre, String usuario, Rol rol, boolean activo, String passwordOpcional) {
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        Validadores.requerido(nombre, "Nombre");
        Validadores.requerido(usuario, "Usuario");
        String hash = (passwordOpcional == null || passwordOpcional.isBlank())
                ? actual.passwordHash() : Seguridad.hash(passwordOpcional);
        return repo.guardar(new Usuario(id, nombre.trim(), usuario.trim(), hash,
                rol == null ? actual.rol() : rol, activo));
    }

    @Override
    public void cambiarPassword(int id, String password) {
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        Validadores.requerido(password, "Contraseña");
        repo.guardar(new Usuario(id, actual.nombre(), actual.usuario(),
                Seguridad.hash(password), actual.rol(), actual.activo()));
    }

    @Override
    public void eliminar(int id) {
        repo.eliminar(id);
    }
}
