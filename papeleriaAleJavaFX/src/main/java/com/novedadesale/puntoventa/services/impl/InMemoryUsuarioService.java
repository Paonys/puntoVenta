package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.services.UsuarioService;
import com.novedadesale.puntoventa.shared.Seguridad;
import com.novedadesale.puntoventa.shared.Validadores;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUsuarioService implements UsuarioService {

    private final Datos d = Datos.get();

    @Override
    public Optional<Usuario> autenticar(String usuario, String password) {
        if (usuario == null || password == null) {
            return Optional.empty();
        }
        return d.usuarios.values().stream()
                .filter(u -> u.activo() && u.usuario().equalsIgnoreCase(usuario)
                        && Seguridad.verificar(password, u.passwordHash()))
                .findFirst();
    }

    @Override
    public List<Usuario> listar() {
        return new ArrayList<>(d.usuarios.values());
    }

    @Override
    public Optional<Usuario> porId(int id) {
        return Optional.ofNullable(d.usuarios.get(id));
    }

    @Override
    public Usuario crear(String nombre, String usuario, String password, Rol rol) {
        Validadores.requerido(nombre, "Nombre");
        Validadores.requerido(usuario, "Usuario");
        Validadores.requerido(password, "Contraseña");
        boolean existe = d.usuarios.values().stream()
                .anyMatch(u -> u.usuario().equalsIgnoreCase(usuario.trim()));
        if (existe) {
            throw new ServiceException("Ya existe un usuario con el nombre de acceso \"" + usuario + "\"");
        }
        int id = d.userSeq.incrementAndGet();
        Usuario nuevo = new Usuario(id, nombre.trim(), usuario.trim(), Seguridad.hash(password),
                rol == null ? Rol.CAJERO : rol, true);
        d.usuarios.put(id, nuevo);
        return nuevo;
    }

    @Override
    public Usuario actualizar(int id, String nombre, String usuario, Rol rol, boolean activo, String passwordOpcional) {
        Usuario actual = d.usuarios.get(id);
        if (actual == null) {
            throw new ServiceException("Usuario no encontrado: " + id);
        }
        Validadores.requerido(nombre, "Nombre");
        Validadores.requerido(usuario, "Usuario");
        String hash = (passwordOpcional == null || passwordOpcional.isBlank())
                ? actual.passwordHash() : Seguridad.hash(passwordOpcional);
        Usuario nuevo = new Usuario(id, nombre.trim(), usuario.trim(), hash,
                rol == null ? actual.rol() : rol, activo);
        d.usuarios.put(id, nuevo);
        return nuevo;
    }

    @Override
    public void cambiarPassword(int id, String password) {
        Usuario actual = d.usuarios.get(id);
        if (actual == null) {
            throw new ServiceException("Usuario no encontrado: " + id);
        }
        Validadores.requerido(password, "Contraseña");
        d.usuarios.put(id, new Usuario(id, actual.nombre(), actual.usuario(),
                Seguridad.hash(password), actual.rol(), actual.activo()));
    }

    @Override
    public void eliminar(int id) {
        d.usuarios.remove(id);
    }
}
