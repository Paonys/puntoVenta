package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.repositories.UsuarioRepository;
import com.novedadesale.puntoventa.services.UsuarioService;
import com.novedadesale.puntoventa.shared.Seguridad;
import com.novedadesale.puntoventa.shared.Validadores;
import com.novedadesale.puntoventa.ui.Sesion;

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
        Autorizacion.exigirSesion("listar usuarios");
        return repo.listar();
    }

    @Override
    public Optional<Usuario> porId(int id) {
        Autorizacion.exigirSesion("consultar usuarios");
        return repo.porId(id);
    }

    @Override
    public Usuario crear(String nombre, String usuario, String password, Rol rol) {
        Autorizacion.exigirAdmin("crear usuarios");
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
        Autorizacion.exigirAdmin("actualizar usuarios");
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        Validadores.requerido(nombre, "Nombre");
        Validadores.requerido(usuario, "Usuario");

        // A8: no permitir que el sistema se quede sin ningún ADMIN activo.
        Rol nuevoRol = rol == null ? actual.rol() : rol;
        boolean eraAdminActivo = actual.rol() == Rol.ADMIN && actual.activo();
        boolean sigueAdminActivo = nuevoRol == Rol.ADMIN && activo;
        if (eraAdminActivo && !sigueAdminActivo) {
            exigirOtroAdminActivo(id);
        }

        String hash = (passwordOpcional == null || passwordOpcional.isBlank())
                ? actual.passwordHash() : Seguridad.hash(passwordOpcional);
        return repo.guardar(new Usuario(id, nombre.trim(), usuario.trim(), hash, nuevoRol, activo));
    }

    @Override
    public void cambiarPassword(int id, String password) {
        Autorizacion.exigirAdmin("cambiar contraseñas");
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        Validadores.requerido(password, "Contraseña");
        repo.guardar(new Usuario(id, actual.nombre(), actual.usuario(),
                Seguridad.hash(password), actual.rol(), actual.activo()));
    }

    @Override
    public void eliminar(int id) {
        Autorizacion.exigirAdmin("eliminar usuarios");
        if (Sesion.activa() && Sesion.id() == id) {
            throw new ServiceException("No puedes eliminar tu propia cuenta");
        }
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        if (actual.rol() == Rol.ADMIN && actual.activo()) {
            exigirOtroAdminActivo(id);
        }
        repo.eliminar(id);
    }

    /** Impide dejar el sistema sin al menos un ADMIN activo (A8). */
    private void exigirOtroAdminActivo(int excluirId) {
        boolean quedaOtro = repo.listar().stream()
                .anyMatch(u -> u.id() != excluirId && u.rol() == Rol.ADMIN && u.activo());
        if (!quedaOtro) {
            throw new ServiceException("No se puede quitar el acceso: es el único ADMIN activo del sistema");
        }
    }
}
