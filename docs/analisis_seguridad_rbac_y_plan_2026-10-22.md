# Análisis de seguridad RBAC y plan de trabajo

**Fecha:** 22 de octubre de 2026  
**Estado:** análisis aprobado para **fase 1 + fase 2**. **No se ha modificado código.**  
**Objetivo:** decidir cómo restringir las acciones del cajero frente al administrador
y planear la implementación.

> El plan detallado y ejecutable de ambas fases está en
> `docs/plan_trabajo_seguridad_fases_1_2.md`.

## 1. Resumen ejecutivo

Las tres opciones planteadas son técnicamente viables, pero no son independientes:

- **Opción 1 (matriz RBAC):** implementable ya. No requiere cambios de esquema.
  Es la base para cualquier restricción real.
- **Opción 2 (solo documentar):** implementable de inmediato, sin código. No
  mejora la seguridad por sí sola; este mismo documento la cubre.
- **Opción 3 (endurecer credenciales):** implementable, pero depende de la
  opción 1 para tener una sesión y una identidad confiables. Sí requiere cambios
  de esquema y de la pantalla de login.

**Recomendación:** aprobar la opción 1 ahora y dejar la opción 3 como fase 2.
Hacerlas en ese orden evita duplicar trabajo y permite probar cada frontera de
seguridad antes de agregar bloqueo de cuentas y caducidad de sesión.

## 2. Estado actual

| Capa | Qué hace hoy | Archivo de referencia |
|---|---|---|
| UI | Oculta Configuración al cajero y oculta botones de escritura con `soloAdmin` | `controllers/MainLayoutController.java:68`, `ui/Componentes.java:93` |
| Servicios | Exige ADMIN para crear/editar/eliminar catálogo, clientes, proveedores, ofertas, usuarios, stock y cancelar ventas | `services/impl/Autorizacion.java:24` y los `*ServiceImpl` |
| Contraseñas | PBKDF2-HMAC-SHA256, salt aleatorio, comparación en tiempo constante; rechaza usuarios inactivos | `shared/Seguridad.java`, `services/impl/UsuarioServiceImpl.java:24` |
| Invariantes | No se puede eliminar la propia cuenta ni dejar el sistema sin un ADMIN activo | `services/impl/UsuarioServiceImpl.java:88` |
| Auditoría | Solo el historial de productos registra usuario, fecha y cambios | `persistence/Esquema.java`, `services/impl/ProductoServiceImpl.java:51` |
| Sesión | Estado estático en memoria; se cierra al salir | `ui/Sesion.java` |

### Hallazgos

| # | Severidad | Hallazgo | Evidencia |
|---|---|---|---|
| H1 | **Alta** | Registrar una venta no exige sesión y acepta `usuarioId`/`usuarioNombre` enviados por quien llama; se puede suplantar al usuario. | `services/impl/VentaServiceImpl.java:47`, `models/SolicitudVenta.java` |
| H2 | **Alta** | Exponer/registrar pagos no tiene guarda de rol ni de sesión. | `services/impl/PagoServiceImpl.java:18` |
| H3 | **Media-alta** | Varias lecturas (reportes, ventas, listados) no exigen sesión, aunque la UI solo las muestre tras el login. | `services/impl/ReporteServiceImpl.java`, `VentaServiceImpl.java:110`, `UsuarioServiceImpl.java:34` |
| H4 | **Media** | El servicio de venta acepta cualquier descuento entre 0 y 100 sin comprobar el rol; la UI no ofrece descuento manual, pero el servicio sí lo permite. | `VentaServiceImpl.java:79-83`, `SolicitudVenta.java` |
| H5 | **Media** | Credenciales sembradas débiles y conocidas (`admin/admin123`, `caja1/caja123`), sin cambio obligatorio, bloqueo por intentos ni cierre por inactividad. | `persistence/Seeder.java`, `controllers/LoginController.java` |
| H6 | **Baja-media** | La UI oculta acciones, pero no es una frontera de seguridad. Si se agrega una acción nueva sin guarda en el servicio, queda expuesta. | `ui/Componentes.java:93` |
| H7 | **Baja-media** | Cancelaciones de venta y cambios de usuarios no tienen auditoría; solo productos. | `services/impl/VentaServiceImpl.java:135`, `UsuarioServiceImpl.java` |
| H8 | **Informativa** | La base SQLite no está cifrada; quien tenga acceso al archivo puede leerla. | `persistence/BaseDatos.java` |

Lo positivo: las escrituras del catálogo y la gestión de usuarios ya están
protegidas en la capa de servicios, y las contraseñas nuevas usan un hash
adecuado. El problema principal no es el hash, sino **quién puede invocar cada
operación** y **qué identidad se registra en una venta**.

## 3. Viabilidad de las tres opciones

### Opción 1: matriz RBAC en servicios y UI

**Viable: sí. Complejidad: media. Cambios de esquema: ninguno.**

Qué incluye:

1. Agregar `Autorizacion.exigirSesion(String accion)` y aplicarla a todas las
   operaciones de servicio, excepto `autenticar`.
2. Mantener `exigirAdmin` para escrituras administrativas y `historial`.
3. En `VentaServiceImpl.registrar`, exigir sesión y tomar `usuarioId` y
   `usuarioNombre` de `Sesion`, no del comando. La venta siempre queda a nombre
   de quien realmente inició sesión.
4. Resolver el descuento del cliente en el servicio a partir de `clienteId`, y
   permitir un descuento adicional solo a ADMIN. Así el cajero conserva el
   descuento configurado del cliente, pero no puede inventar uno.
5. Guardar `PagoService.registrar` o marcarlo como operación interna; no debe
   ser una puerta abierta para insertar pagos.
6. Revisar que cada nueva acción de UI tenga su guarda equivalente en el
   servicio. La UI seguirá ocultando botones, pero como comodidad, no como
   seguridad.

Impacto:

- `Autorizacion`, los nueve `*ServiceImpl`, `Servicios` (si se inyecta el
  repositorio de clientes en ventas), `PointOfSaleController` y las pruebas.
- Las pruebas actuales que llaman servicios sin sesión deberán iniciar sesión
  en su preparación (`PersistenciaTest` es el caso principal).
- Riesgo bajo de regresión funcional; riesgo medio de tests desactualizados.

**Resultado:** un cajero no puede escribir catálogo, usuarios, stock, clientes,
ofertas, pagos ni cancelar ventas; solo vender y consultar. Un administrador
conserva todas las acciones.

### Opción 2: solo documentar

**Viable: sí. Complejidad: muy baja. Cambios de esquema: ninguno.**

Entrega un documento con la matriz, los huecos y el procedimiento operativo.
Este informe ya cubre el diagnóstico y la matriz. No corrige H1–H5, así que no
la considero una alternativa a la opción 1, sino un entregable complementario.

### Opción 3: endurecer credenciales y sesión

**Viable: sí. Complejidad: media-alta. Depende de la opción 1.**

Qué incluye:

1. **Cambio obligatorio de contraseña inicial.** Agregar
   `debe_cambiar_password` a `usuarios` (con migración), agregar el dato al
   modelo `Usuario` y al repositorio, y mostrar un diálogo obligatorio después
   del primer login. El usuario debe poder cambiar su propia contraseña; hoy
   `cambiarPassword` es solo para ADMIN.
2. **Bloqueo por intentos fallidos.** Recomendado persistirlo en la base
   (`intentos_fallidos`, `bloqueado_hasta`) para que no se reinicie al cerrar la
   aplicación. Alternativa mínima: mapa en memoria, válido para una sola
   instancia pero evadible reiniciando el proceso.
3. **Cierre por inactividad.** Temporizador en `MainLayoutController` (por
   ejemplo, 15 minutos configurable) que cierra la sesión y vuelve al login.
   Debe pausarse o manejarse con cuidado durante diálogos modales para no perder
   una venta a medio cobrar.
4. **Política de contraseñas.** Longitud mínima, al menos una letra y un número,
   rechazo de las credenciales sembradas y de la contraseña igual al usuario.
5. **Auditoría adicional (opcional).** Registrar cancelaciones de venta y
   cambios de usuarios en una tabla genérica de auditoría.

Impacto:

- Esquema `usuarios` y migración en `BaseDatos`.
- Modelo `Usuario`, `SqliteUsuarioRepository`, `UsuarioServiceImpl`,
  `Servicios`, `LoginController` y un diálogo nuevo de cambio de contraseña.
- Pruebas de autenticación, bloqueo, cambio obligatorio y política.
- La migración debe ejecutarse sobre una copia de la base real antes de
  desplegar; las bases existentes no deben perder usuarios.

**No incluye cifrado de la base.** Hacerlo requeriría SQLCipher u otra variante
del driver, con impacto alto en empaquetado y migración. Para una aplicación de
escritorio, primero conviene proteger el acceso al equipo y respaldar la base.

## 4. Comparación

| Criterio | Opción 1 | Opción 2 | Opción 3 |
|---|---|---|---|
| Restringe acciones del cajero | Sí, de verdad | No | Sí, y además protege el acceso |
| Cambios de esquema | No | No | Sí (`usuarios`) |
| Dependencia | Ninguna | Ninguna | Opción 1 |
| Riesgo de regresión | Bajo-medio | Nulo | Medio |
| Esfuerzo aproximado | 1 sesión de trabajo | Menos de 1 hora | 2–3 sesiones |
| Recomendación | Hacer primero | Entregar junto con este informe | Hacer después de la opción 1 |

## 5. Plan de trabajo propuesto

### Fase 0. Decisión y respaldo

- Aprobar la política de la sección 6.
- Respaldar `novedadesale.db` antes de cualquier migración.
- Confirmar que no hay cambios sin commitear antes de empezar.

**Criterio de salida:** política aprobada y respaldo verificado.

### Fase 1. Matriz RBAC (opción 1)

| ID | Tarea | Archivos principales | Criterio de aceptación |
|---|---|---|---|
| S1 | Documentar la matriz definitiva y las decisiones de negocio | este documento, `docs/` | Matriz aprobada por el usuario |
| S2 | Crear `exigirSesion` y aplicarla a lecturas y operaciones internas | `Autorizacion.java`, `*ServiceImpl.java` | Sin sesión, todo servicio (excepto `autenticar`) lanza `ServiceException` |
| S3 | Venta con identidad de sesión | `VentaServiceImpl.java`, `SolicitudVenta.java`, `PointOfSaleController.java` | Un llamador no puede falsificar `usuarioId`/`usuarioNombre`; la venta queda a nombre de `Sesion` |
| S4 | Política de descuentos | `VentaServiceImpl.java`, `Servicios.java`, pruebas | El cajero solo obtiene descuento de cliente/promoción; el descuento adicional exige ADMIN |
| S5 | Cerrar la puerta de pagos | `PagoServiceImpl.java` | Registrar pago exige sesión (o queda como operación interna no expuesta) |
| S6 | Revisar UI y acciones por rol | controladores, `Componentes.soloAdmin` | Cada acción visible tiene guarda equivalente en el servicio |
| S7 | Pruebas de denegación | `AutorizacionTest`, `PersistenciaTest` | Cajero y llamadas sin sesión no pueden escribir; ADMIN sí; venta no se puede suplantar |
| S8 | Regresión completa | `mvn -B test` | Todas las pruebas verdes y app abre correctamente |

**Criterio de salida:** matriz implementada, pruebas de denegación pasando y
sin cambios de esquema.

### Fase 2. Credenciales y sesión (opción 3)

| ID | Tarea | Archivos principales | Criterio de aceptación |
|---|---|---|---|
| S9 | Migración de `debe_cambiar_password` y modelo | `Esquema.java`, `BaseDatos.java`, `Usuario.java`, `SqliteUsuarioRepository.java` | Bases viejas migran sin perder usuarios |
| S10 | Cambio obligatorio en el primer login | `LoginController.java`, `UsuarioServiceImpl.java`, vista/diálogo nuevo | Las cuentas sembradas obligan a cambiar contraseña antes de entrar |
| S11 | Bloqueo por intentos fallidos | `UsuarioServiceImpl.java`, repositorio, migración | Tras N intentos, la cuenta se bloquea temporalmente; un login correcto limpia el contador |
| S12 | Política de contraseñas | `Validadores.java`, `UsuarioServiceImpl.java` | Se rechazan contraseñas débiles o iguales al usuario |
| S13 | Cierre por inactividad | `MainLayoutController.java`, `AppConstants.java` | Tras el tiempo configurado se cierra la sesión y se vuelve al login |
| S14 | Pruebas de credenciales y sesión | nuevas pruebas + regresión | Cambio obligatorio, bloqueo, política y último ADMIN siguen funcionando |

**Criterio de salida:** base migrada en copia, pruebas verdes y login probado con
las cuentas sembradas.

### Fase 3. Opcional, no incluida en la estimación

- Auditoría de cancelaciones y cambios de usuarios.
- Cifrado de la base o respaldos automáticos.
- Registro de intentos de acceso fallidos.

## 6. Decisiones que debes tomar antes de implementar

1. **Reportes y dashboard para cajeros:** ¿puede verlos en modo consulta o se
   ocultan por completo? Hoy los ve.
2. **Clientes:** ¿el cajero puede dar de alta clientes desde el punto de venta o
   sigue siendo exclusivo del administrador? Hoy es exclusivo del admin.
3. **Descuento de cliente:** ¿el cajero puede aplicar el descuento configurado
   del cliente? Recomendación: sí; el descuento manual adicional, no.
4. **Bloqueo de cuenta:** cuántos intentos (recomendado: 5) y cuánto tiempo
   (recomendado: 5–15 minutos).
5. **Inactividad:** minutos para cerrar sesión (recomendado: 15).
6. **Contraseñas iniciales:** ¿forzar cambio en el primer login? Recomendación:
   sí, al menos para las cuentas sembradas.

## 7. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Exigir sesión en lecturas rompe pruebas o pantallas cargadas antes del login | Actualizar pruebas y verificar que `MainLayout` solo se cargue tras autenticar |
| La venta pierde el carrito si se cierra la sesión por inactividad | Avisar antes de cerrar o pausar el temporizador durante el cobro |
| El bloqueo deja fuera al único administrador | Mantener el invariante de “al menos un ADMIN activo” y permitir recuperación local documentada |
| La migración de usuarios falla en la base real | Probar sobre copia y respaldar antes de abrir la app |
| Cambiar el modelo `Usuario` rompe constructores en pruebas | Actualizar todas las llamadas en el mismo cambio y ejecutar regresión |
| El cajero necesita una acción que la matriz le niega | Ajustar la matriz antes de implementar; no agregar excepciones sueltas en la UI |

## 8. Recomendación final

Implementar **opción 1 ahora** y tratar este documento como la entrega de la
**opción 2**. Programar la **opción 3** como fase 2, después de que la sesión y
la identidad de venta sean confiables.

Con eso se cumple la política “el cajero vende y consulta; el administrador
gestiona”, se cierran los huecos H1–H3 y se deja una base clara para endurecer
credenciales sin rehacer el trabajo.
