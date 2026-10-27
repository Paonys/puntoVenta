package com.novedadesale.puntoventa.services.impl;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.models.Rol;
import com.novedadesale.puntoventa.models.Usuario;
import com.novedadesale.puntoventa.repositories.UsuarioRepository;
import com.novedadesale.puntoventa.services.UsuarioService;
import com.novedadesale.puntoventa.shared.AppConstants;
import com.novedadesale.puntoventa.shared.Reloj;
import com.novedadesale.puntoventa.shared.Seguridad;
import com.novedadesale.puntoventa.shared.Validadores;
import com.novedadesale.puntoventa.ui.Sesion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UsuarioServiceImpl implements UsuarioService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final UsuarioRepository repo;

    public UsuarioServiceImpl(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Usuario> autenticar(String usuario, String password) {
        if (usuario == null || password == null) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = repo.porUsuario(usuario.trim());
        if (encontrado.isEmpty() || !encontrado.get().activo()) {
            return Optional.empty();
        }
        LocalDateTime ahora = Reloj.ahora();
        Usuario u = encontrado.get();

        if (bloqueado(u, ahora)) {
            throw new ServiceException("Cuenta bloqueada hasta las "
                    + LocalDateTime.parse(u.bloqueadoHasta()).format(HORA));
        }
        boolean sucio = u.intentosFallidos() != 0 || u.bloqueadoHasta() != null;
        if (u.bloqueadoHasta() != null) {
            // Bloqueo vencido: se ignoran los intentos antiguos sin escribir.
            u = con(u, 0, null);
        }

        if (!Seguridad.verificar(password, u.passwordHash())) {
            registrarFallo(u, ahora);
            return Optional.empty();
        }
        if (sucio) {
            repo.guardar(con(u, 0, null));
        }
        return Optional.of(u);
    }

    private boolean bloqueado(Usuario u, LocalDateTime ahora) {
        return u.bloqueadoHasta() != null && LocalDateTime.parse(u.bloqueadoHasta()).isAfter(ahora);
    }

    /** S10: suma un fallo y bloquea la cuenta al agotar los intentos. */
    private void registrarFallo(Usuario u, LocalDateTime ahora) {
        int intentos = u.intentosFallidos() + 1;
        String bloqueadoHasta = u.bloqueadoHasta();
        if (intentos >= AppConstants.INTENTOS_BLOQUEO) {
            bloqueadoHasta = ahora.plusMinutes(AppConstants.MINUTOS_BLOQUEO).toString();
        }
        repo.guardar(con(u, intentos, bloqueadoHasta));
    }

    private Usuario con(Usuario u, int intentos, String bloqueadoHasta) {
        return new Usuario(u.id(), u.nombre(), u.usuario(), u.passwordHash(), u.rol(), u.activo(),
                u.debeCambiarPassword(), intentos, bloqueadoHasta);
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
        Validadores.passwordSegura(password, usuario);
        repo.porUsuario(usuario.trim()).ifPresent(u -> {
            throw new ServiceException("Ya existe el usuario \"" + usuario + "\"");
        });
        return repo.guardar(new Usuario(0, nombre.trim(), usuario.trim(),
                Seguridad.hash(password), rol == null ? Rol.CAJERO : rol, true, true, 0, null));
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

        boolean cambiaPassword = passwordOpcional != null && !passwordOpcional.isBlank();
        if (cambiaPassword) {
            Validadores.passwordSegura(passwordOpcional, usuario);
        }
        String hash = cambiaPassword ? Seguridad.hash(passwordOpcional) : actual.passwordHash();
        return repo.guardar(new Usuario(id, nombre.trim(), usuario.trim(), hash, nuevoRol, activo,
                actual.debeCambiarPassword(), actual.intentosFallidos(), actual.bloqueadoHasta()));
    }

    @Override
    public void cambiarPassword(int id, String password) {
        Autorizacion.exigirAdmin("cambiar contraseñas");
        Usuario actual = repo.porId(id)
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + id));
        Validadores.requerido(password, "Contraseña");
        Validadores.passwordSegura(password, actual.usuario());
        repo.guardar(new Usuario(id, actual.nombre(), actual.usuario(),
                Seguridad.hash(password), actual.rol(), actual.activo(),
                actual.debeCambiarPassword(), actual.intentosFallidos(), actual.bloqueadoHasta()));
    }

    @Override
    public void cambiarPasswordPropia(String passwordActual, String passwordNueva) {
        Autorizacion.exigirSesion("cambiar la contraseña propia");
        Usuario actual = repo.porId(Sesion.id())
                .orElseThrow(() -> new ServiceException("Usuario no encontrado: " + Sesion.id()));
        if (!Seguridad.verificar(passwordActual, actual.passwordHash())) {
            throw new ServiceException("La contraseña actual no coincide");
        }
        Validadores.passwordSegura(passwordNueva, actual.usuario());
        if (Seguridad.verificar(passwordNueva, actual.passwordHash())) {
            throw new ServiceException("La contraseña nueva debe ser distinta a la actual");
        }
        repo.guardar(new Usuario(actual.id(), actual.nombre(), actual.usuario(),
                Seguridad.hash(passwordNueva), actual.rol(), actual.activo(),
                false, 0, null));
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
