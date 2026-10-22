# Plan de trabajo: seguridad RBAC (fases 1 y 2)

**Fecha:** 22 de octubre de 2026  
**Alcance aprobado:** fase 1 + fase 2.  
**Estado:** planificado. **No se ha modificado código.**  
**Documento de análisis:** `docs/analisis_seguridad_rbac_y_plan_2026-10-22.md`

## 1. Resultado esperado

Al terminar las dos fases:

- Ningún servicio, salvo el login, funciona sin una sesión activa.
- Un cajero solo puede vender y consultar. No puede crear, editar, eliminar,
  ajustar stock, cancelar ventas, gestionar usuarios, registrar pagos ni aplicar
  descuentos manuales.
- Una venta queda siempre a nombre del usuario que realmente inició sesión; el
  llamador no puede suplantar la identidad.
- El cajero conserva el descuento configurado del cliente y las promociones.
- Las cuentas con contraseña inicial deben cambiarla antes de operar.
- Una cuenta se bloquea temporalmente tras varios intentos fallidos.
- La sesión se cierra tras un periodo de inactividad.
- Se mantienen los invariantes actuales: al menos un ADMIN activo y no poder
  eliminar la propia cuenta.

**Fuera de alcance:** auditoría de cancelaciones, cifrado de la base y respaldos
automáticos. Se pueden agregar después como fase 3.

## 2. Supuestos de política

Estos valores se usarán salvo que se indique lo contrario antes de empezar:

| Tema | Supuesto |
|---|---|
| Reportes y dashboard | El cajero puede consultarlos en modo lectura; no puede cancelar ventas |
| Clientes | Solo el administrador crea, edita o elimina clientes |
| Descuento de cliente | El cajero sí puede aplicar el descuento ya configurado del cliente |
| Descuento manual adicional | Solo el administrador |
| Intentos fallidos | 5 intentos; bloqueo de 15 minutos |
| Inactividad | Cierre de sesión a los 15 minutos |
| Contraseña inicial | Obligatorio cambiarla en el primer inicio de sesión |
| Política de contraseña | Mínimo 8 caracteres, al menos una letra y un número, distinta del usuario y de las claves sembradas |
| Pagos directos | Solo el administrador; la venta normal los registra internamente |

Si alguno cambia, debe ajustarse **antes** de la tarea S1 para no rehacer
pruebas.

## 3. Fase 1 — Matriz RBAC

### S1. Cerrar la matriz y las decisiones

- Confirmar los supuestos de la sección 2.
- Dejar escrita la matriz definitiva en `docs/`.
- Identificar cualquier acción nueva de UI que no tenga guarda de servicio.

**Salida:** matriz aprobada, sin cambios de código.

### S2. Guarda de sesión en la capa de servicios

**Objetivo:** la sesión sea obligatoria para toda operación distinta del login.

Pasos:

1. Agregar `Autorizacion.exigirSesion(String accion)` junto a `exigirAdmin`.
2. Aplicarla en las lecturas y escrituras de:
   - `CategoriaServiceImpl`
   - `ClienteServiceImpl`
   - `PagoServiceImpl`
   - `ProductoServiceImpl`
   - `PromocionServiceImpl`
   - `ProveedorServiceImpl`
   - `ReporteServiceImpl`
   - `UsuarioServiceImpl` (excepto `autenticar`)
   - `VentaServiceImpl`
3. Mantener `exigirAdmin` en las escrituras administrativas ya protegidas.
4. Excepción deliberada: `UsuarioServiceImpl.autenticar` debe seguir funcionando
   sin sesión.

**Pruebas:** cada servicio lanza `ServiceException` sin sesión; con sesión de
cajero funciona la lectura; con sesión de admin funciona la escritura.

**Archivos:** `services/impl/Autorizacion.java`, los nueve `*ServiceImpl.java`,
pruebas.

### S3. Identidad de venta desde la sesión

**Objetivo:** eliminar la suplantación de usuario en ventas.

Pasos:

1. `VentaServiceImpl.registrar` exige sesión.
2. La venta toma `usuarioId` y `usuarioNombre` de `Sesion`, no del comando.
3. Quitar `usuarioId` y `usuarioNombre` de `SolicitudVenta` y actualizar todas
   las llamadas (controlador de ventas y pruebas).
4. Verificar que el ticket y el reporte muestran el usuario de la sesión.

**Pruebas:** una solicitud manipulada no puede registrar la venta a nombre de
otro usuario; sin sesión se rechaza; el cajero sí puede vender.

**Archivos:** `models/SolicitudVenta.java`, `services/impl/VentaServiceImpl.java`,
`controllers/PointOfSaleController.java`, pruebas.

### S4. Descuentos por rol

**Objetivo:** el cajero no puede inventar descuentos.

Pasos:

1. Inyectar el repositorio de clientes en `VentaServiceImpl` a través de
   `Servicios`.
2. Calcular el descuento del cliente en el servicio usando `clienteId`; no
   confiar en el porcentaje que envía la UI.
3. Tratar `SolicitudVenta.descuentoPorcentaje` como **descuento manual
   adicional**.
4. Si el descuento manual es mayor a cero y la sesión no es ADMIN, rechazar con
   `ServiceException`.
5. Mantener las promociones calculadas por el servicio y el tope del 100%.
6. Ajustar el punto de venta para enviar cero como descuento manual; el
   descuento del cliente lo resuelve el servicio.

**Pruebas:** cajero con cliente al 5% obtiene 5%; cajero con 10% manual falla;
admin con 10% manual funciona; promociones siguen aplicando.

**Archivos:** `services/impl/VentaServiceImpl.java`, `services/Servicios.java`,
`controllers/PointOfSaleController.java`, pruebas.

### S5. Cerrar el registro directo de pagos

**Objetivo:** no permitir insertar pagos sueltos.

Pasos:

1. `PagoServiceImpl.registrar` exige sesión de ADMIN.
2. Verificar que la venta normal sigue registrando su pago mediante el
   repositorio dentro de la transacción de `VentaServiceImpl`.
3. Si no se necesita exponer el método, marcarlo como operación interna y
   evitar su uso desde la UI.

**Pruebas:** cajero y llamadas sin sesión no pueden registrar pagos; admin sí;
la venta normal sigue generando su pago.

**Archivos:** `services/impl/PagoServiceImpl.java`, pruebas.

### S6. Revisión de UI y acciones

**Objetivo:** que no quede una acción visible sin guarda equivalente.

Pasos:

1. Revisar cada controlador con botones de escritura y confirmar su guarda en
   el servicio.
2. Mantener `Componentes.soloAdmin` como comodidad visual.
3. Confirmar que Configuración sigue oculta al cajero.
4. Decidir si se agrega un indicador de “modo consulta” para el cajero; si no se
   agrega, no hay cambio visual.
5. No agregar controles nuevos de descuento manual en esta fase.

**Salida:** lista de acciones UI ↔ guarda de servicio, sin acciones huérfanas.

### S7. Pruebas de denegación y regresión

- Ampliar `AutorizacionTest` con:
  - Lecturas sin sesión rechazadas.
  - Ventas sin sesión rechazadas.
  - Suplantación de usuario rechazada.
  - Descuento manual de cajero rechazado.
  - Pago directo de cajero rechazado.
- Actualizar `PersistenciaTest` para iniciar sesión antes de las operaciones que
  ahora la requieren.
- Ejecutar `mvn -B test`.

**Criterio de salida de la fase 1:** todas las pruebas verdes, app abre y se
puede vender como cajero y administrar como admin.

## 4. Fase 2 — Credenciales y sesión

### S8. Esquema y modelo de credenciales

**Objetivo:** soportar cambio obligatorio y bloqueo sin perder datos.

Pasos:

1. Agregar a `usuarios`:
   - `debe_cambiar_password INTEGER NOT NULL DEFAULT 0`
   - `intentos_fallidos INTEGER NOT NULL DEFAULT 0`
   - `bloqueado_hasta TEXT`
2. Agregar migraciones idempotentes en `BaseDatos` con
   `agregarColumnaSiFalta`.
3. Actualizar el record `Usuario` y `SqliteUsuarioRepository` (mapeo, INSERT y
   UPDATE).
4. Actualizar todas las construcciones de `Usuario` en el código y las pruebas.
5. Marcar con `debe_cambiar_password = 1` a los usuarios sembrados y a los
   usuarios nuevos creados por un administrador.

**Pruebas:** una base existente migra sin perder usuarios; una base nueva crea
las columnas; el login sigue funcionando.

**Archivos:** `persistence/Esquema.java`, `persistence/BaseDatos.java`,
`persistence/Seeder.java`, `models/Usuario.java`,
`repositories/sqlite/SqliteUsuarioRepository.java`, pruebas.

### S9. Cambio obligatorio en el primer login

**Objetivo:** no operar con la contraseña inicial.

Pasos:

1. Agregar al servicio un cambio de contraseña propia que valide la contraseña
   actual (`cambiarPasswordPropia`).
2. En `LoginController`, después de autenticar:
   - Si `debe_cambiar_password` es verdadero, abrir un diálogo modal obligatorio.
   - Pedir contraseña actual, nueva y confirmación.
   - No permitir cerrar el diálogo sin cambiar; si se cancela, volver al login.
   - Al guardar, limpiar la bandera y continuar al sistema.
3. No permitir que la nueva contraseña sea igual a la inicial.
4. Mantener el cambio de contraseña de administrador para el resto de cuentas.

**Pruebas:** usuario sembrado no entra sin cambiar; cambio correcto limpia la
bandera; contraseña actual incorrecta falla; cancelar no inicia sesión.

**Archivos:** `controllers/LoginController.java`,
`services/UsuarioService.java`, `services/impl/UsuarioServiceImpl.java`,
repositorio, diálogo/vista nueva, pruebas.

### S10. Bloqueo por intentos fallidos

**Objetivo:** frenar ataques de fuerza bruta.

Pasos:

1. En `autenticar`, comprobar `bloqueado_hasta`; si está vigente, rechazar con
   un mensaje de cuenta bloqueada.
2. En cada fallo, incrementar `intentos_fallidos`; al llegar a 5, fijar
   `bloqueado_hasta` a ahora + 15 minutos.
3. En un login correcto, limpiar intentos y bloqueo.
4. Agregar métodos al repositorio para actualizar contadores y bloqueo.
5. Usar un reloj inyectable o una fuente de tiempo controlable para probar sin
   esperar 15 minutos.

**Pruebas:** 5 fallos bloquean; la contraseña correcta durante el bloqueo no
entra; al vencer el bloqueo entra y se limpia el contador; un login correcto
antes del límite limpia los fallos.

**Archivos:** `UsuarioServiceImpl.java`, `UsuarioRepository.java`,
`SqliteUsuarioRepository.java`, pruebas.

### S11. Política de contraseñas

**Objetivo:** evitar contraseñas débiles.

Pasos:

1. Agregar una validación reutilizable en `Validadores`:
   - Mínimo 8 caracteres.
   - Al menos una letra y un número.
   - No igual al nombre de usuario.
   - No estar en la lista de claves sembradas conocidas.
2. Aplicarla al crear usuarios, al cambio de administrador y al cambio propio.
3. No bloquear el login de cuentas antiguas: solo se exige al establecer una
   contraseña nueva.

**Pruebas:** se rechazan claves cortas, sin número, iguales al usuario y las
sembradas; se acepta una clave válida.

**Archivos:** `shared/Validadores.java`, `UsuarioServiceImpl.java`, pruebas.

### S12. Cierre por inactividad

**Objetivo:** reducir el riesgo de dejar una sesión abierta.

Pasos:

1. Definir `MINUTOS_INACTIVIDAD = 15` en `AppConstants`.
2. Crear un temporizador reutilizable que reinicie la cuenta atrás con eventos
   de teclado y ratón.
3. Activarlo solo en `MainLayout`, no en el login.
4. Al vencer, cerrar sesión y volver al login.
5. Evitar que el cierre ocurra en medio de un cobro: pausar el temporizador
   mientras haya un diálogo modal o advertir antes de cerrar.
6. Extraer la lógica a una clase comprobable, sin depender del reloj real.

**Pruebas:** el temporizador dispara el cierre; la actividad lo reinicia; el
login no lo activa.

**Archivos:** `shared/AppConstants.java`, `controllers/MainLayoutController.java`,
clase nueva de temporizador, pruebas.

### S13. Pruebas, migración y verificación manual

- Ejecutar `mvn -B test`.
- Probar la migración sobre una **copia** de `novedadesale.db`:
  - Abrir la app.
  - Verificar usuarios existentes.
  - Iniciar sesión con admin y cajero.
- Verificar manualmente:
  - Cambio obligatorio.
  - Bloqueo tras 5 fallos.
  - Cierre por inactividad.
  - Cajero sin acceso a escritura.
  - Venta con descuento de cliente y sin descuento manual.

**Criterio de salida de la fase 2:** base migrada sin pérdida de datos, pruebas
verdes y los cuatro escenarios manuales funcionando.

## 5. Orden de ejecución

```text
S1
 └─ S2 ─ S3 ─ S4 ─ S5 ─ S6 ─ S7   (fase 1)
                              └─ S8 ─ S9 ─ S10 ─ S11 ─ S12 ─ S13 (fase 2)
```

No empezar S8 hasta que S7 esté verde: el cambio obligatorio y el bloqueo se
apoyan en una sesión y una identidad confiables.

## 6. Matriz de pruebas

| Escenario | Sin sesión | Cajero | Admin |
|---|---|---|---|
| Iniciar sesión | Permitido | Permitido | Permitido |
| Consultar catálogo, clientes, inventario, reportes | Rechazado | Permitido | Permitido |
| Registrar venta | Rechazado | Permitido | Permitido |
| Suplantar usuario en venta | Rechazado | Rechazado | Rechazado (se usa la sesión) |
| Descuento de cliente | — | Permitido | Permitido |
| Descuento manual adicional | Rechazado | Rechazado | Permitido |
| Crear/editar/eliminar catálogo, clientes, ofertas, proveedores | Rechazado | Rechazado | Permitido |
| Ajustar stock | Rechazado | Rechazado | Permitido |
| Cancelar venta | Rechazado | Rechazado | Permitido |
| Registrar pago directo | Rechazado | Rechazado | Permitido |
| Ver historial de productos | Rechazado | Rechazado | Permitido |
| Gestionar usuarios y contraseñas | Rechazado | Rechazado | Permitido |
| Entrar con contraseña inicial | — | Bloqueado hasta cambiarla | Bloqueado hasta cambiarla |
| 5 intentos fallidos | — | Bloqueo temporal | Bloqueo temporal |
| Inactividad de 15 minutos | — | Cierra sesión | Cierra sesión |

## 7. Archivos afectados (estimado)

### Fase 1

- `services/impl/Autorizacion.java`
- `services/impl/*ServiceImpl.java` (9 archivos)
- `models/SolicitudVenta.java`
- `services/Servicios.java`
- `controllers/PointOfSaleController.java`
- `src/test/.../AutorizacionTest.java`
- `src/test/.../PersistenciaTest.java`

### Fase 2

- `persistence/Esquema.java`
- `persistence/BaseDatos.java`
- `persistence/Seeder.java`
- `models/Usuario.java`
- `repositories/UsuarioRepository.java`
- `repositories/sqlite/SqliteUsuarioRepository.java`
- `services/UsuarioService.java`
- `services/impl/UsuarioServiceImpl.java`
- `shared/Validadores.java`
- `shared/AppConstants.java`
- `controllers/LoginController.java`
- `controllers/MainLayoutController.java`
- vista/diálogo nuevo de cambio de contraseña
- pruebas nuevas de credenciales e inactividad

## 8. Estimación

| Fase | Esfuerzo | Riesgo |
|---|---|---|
| Fase 1 | 1–2 sesiones de trabajo | Bajo-medio |
| Fase 2 | 2–3 sesiones de trabajo | Medio |
| Total | 3–5 sesiones | Medio |

## 9. Definición de terminado

- Matriz RBAC implementada y probada en servicios, no solo en la UI.
- Venta asociada a la sesión real.
- Descuento manual reservado al administrador.
- Pagos directos cerrados.
- Cambio obligatorio, bloqueo por intentos y política de contraseñas activos.
- Cierre por inactividad funcionando.
- Migración probada sobre copia de la base real.
- `mvn -B test` verde.
- Sin cambios en la política fuera de los supuestos aprobados.
