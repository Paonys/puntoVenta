# Reporte de ejecución - Seguridad RBAC Fase 2

**Fecha:** 2026-10-27
**Estado:** Completado (falta solo la verificación manual de S13)
**Tests:** `mvn -B test` → **BUILD SUCCESS** (**61/61**)

Fase 2 del plan `docs/plan_trabajo_seguridad_fases_1_2.md` (S8–S13): credenciales y sesión.
La S8 (esquema y modelo) ya estaba commiteada el mismo día en
`persistencia: endurecer Esquema/Seeder y repos de usuario + pruebas`; esta sesión completó S9–S12.

## Cambios realizados

### S8 (confirmado, hecho antes)
- `persistence/Esquema.java`, `persistence/BaseDatos.java` (migración idempotente con
  `agregarColumnaSiFalta`), `persistence/Seeder.java`, `models/Usuario.java`,
  `repositories/sqlite/SqliteUsuarioRepository.java`: columnas `debe_cambiar_password`,
  `intentos_fallidos`, `bloqueado_hasta`; usuarios sembrados y creados con la bandera activa.

### S9 - Cambio obligatorio en el primer login
- `services/UsuarioService.java` + `services/impl/UsuarioServiceImpl.java`:
  **`cambiarPasswordPropia(actual, nueva)`** — exige sesión, valida la contraseña actual,
  aplica la política y limpia `debe_cambiar_password`.
- `controllers/LoginController.java`: tras autenticar, si la bandera está activa abre un
  **diálogo modal no cancelable sin cambiar** (actual / nueva / confirmación); si se cancela
  se cierra la sesión y se regresa al login.
- `ui/Dialogos.java`: `mostrar(Dialog)` propio con contador de diálogos abiertos.

### S10 - Bloqueo por intentos fallidos
- `UsuarioServiceImpl.autenticar`: rechaza con mensaje de cuenta bloqueada mientras
  `bloqueado_hasta` esté vigente; cada fallo suma `intentos_fallidos` y a los
  **5 intentos** fija el bloqueo a **15 minutos**; un login correcto limpia contadores.
- Bloqueo vencido: se ignoran los intentos antiguos y el primer login exitoso los borra.
- `shared/AppConstants`: `INTENTOS_BLOQUEO`, `MINUTOS_BLOQUEO`.

### S11 - Política de contraseñas
- `shared/Validadores.passwordSegura`: mínimo 8 caracteres, al menos una letra y un número,
  distinta del usuario y de las claves sembradas (`admin123`, `caja123`).
- Aplicada en `crear`, `actualizar` (si informa contraseña), `cambiarPassword` y
  `cambiarPasswordPropia`. No se exige al iniciar sesión.

### S12 - Cierre por inactividad
- `shared/AppConstants.MINUTOS_INACTIVIDAD = 15`.
- **`ui/TemporizadorInactividad`** (clase comprobable, sin reloj real): activar/detener,
  `registrarActividad`, `pausar`/`reanudar`, `comprobar`.
- `controllers/MainLayoutController`: lo activa solo en la pantalla principal (no en el login),
  se reinicia con teclado/ratón y **se pausa mientras haya un diálogo modal abierto**
  (`Dialogos.abierto()`); al vencer cierra la sesión y vuelve al login. Se detiene también
  al cerrar sesión manualmente.
- `shared/Reloj`: gancho de tiempo para pruebas (bloqueo e inactividad se prueban sin esperar).

### S13 - Pruebas automatizadas (parcial)
- **`CredencialesTest`** (5): bloqueo a los 5 fallos y liberación al vencer; login correcto
  limpia fallos; cambio propio valida la actual, la política y limpia la bandera; sin sesión no
  se cambia; política rechaza claves cortas, sin número, sembradas e iguales al usuario.
- **`InactividadTest`** (4): dispara al vencer una sola vez; la actividad reinicia la cuenta;
  pausado por diálogo no dispara y al reanudar vuelve a contar; detenido no dispara.
- `AutorizacionTest`: password de prueba `pass2` → `Admin2026x` (ahora sujeta a política).

## Validación
- **61/61 pruebas verdes** al ejecutar la fase 2 (`mvn -B test`); **65/65 al cierre del día**, tras las correcciones de la revisión ponytail (#1/#5/#6/#7).

## Pendiente (S13 manual)
- Probar la migración sobre una **copia** de `novedadesale.db` real.
- Verificar a mano: cambio obligatorio, bloqueo tras 5 fallos, cierre por inactividad,
  cajero sin escritura, venta con descuento de cliente y sin descuento manual.
