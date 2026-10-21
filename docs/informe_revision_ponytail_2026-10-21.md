# Revisión de código — Punto de Venta "Novedades Ale"

> **revision por ponytail**

**Fecha:** 21 de octubre de 2026
**Alcance:** el repo git es `puntoVenta/` (no la carpeta de trabajo, que es su padre). Los cambios revisados eran el commit `b5ca1ed` (A8+A9) con el working tree limpio; HEAD era `594139d`.
**Cambios revisados:** A8 (autorización por rol en la capa de servicios), A9 (FK `pagos`→`ventas`, `pagos` como fuente única de ingresos y migración automática), reescritura de la página de Reportes y sus pruebas.
**Verificación:** `mvn -B test` → **39/39 OK** (ejecutado durante la revisión). No se modificó código.

---

## Qué hace este cambio

**Carga asumida:** un proceso JavaFX de escritorio, un cajero a la vez, un archivo SQLite local, decenas de ventas por día, sin servidor. La base actual `novedadesale.db` ya está migrada y consistente (15 ventas / 15 pagos / FK presente).

A8 lleva las guardas de rol a la capa de servicios (`Autorizacion` + `ui.Sesion`). A9 hace que `pagos` tenga FK contra `ventas`, que `pagos` sea la fuente única de "ingresos" y agrega una migración automática para bases creadas antes. La página de Reportes se reescribió para dejar de recalcular ingresos en la UI.

---

## Must fix (bloqueantes)

### 1. La migración de `pagos` puede dejar la app inservible para siempre — `persistence/BaseDatos.java:71-103`

- **Qué es:** una migración de una sola vez que reconstruye `pagos` para agregar la FK; se ejecuta en cada arranque (`BaseDatos.java:46-50`).
- **Problema:** no es idempotente ante un fallo ni atómica. Reproduje el SQL exacto: si sobrevive un `pagos_tmp` de un intento fallido anterior → `table pagos_tmp already exists`; si la tabla vieja la creó a mano con otras columnas → `no such column: fecha` (imposible desde el historial de este repo: la tabla siempre tuvo `fecha`). En ambos casos `crearEsquema` lanza → `conexion()` lanza `ServiceException` → **la app ya no puede abrir la base y falla igual en cada reinicio**; la única salida es borrar el .db (y con él todas las ventas). Y si el proceso muere entre `DROP TABLE pagos` y el `RENAME`, el siguiente arranque crea un `pagos` vacío → **se pierde el historial de pagos en silencio** (los datos quedan huérfanos en `pagos_tmp`). El .db vive en una carpeta sincronizada por OneDrive, que bloquea archivos: un SQLITE_BUSY pasajero basta para disparar esto.
- **Fix:** `DROP TABLE IF EXISTS pagos_tmp` antes de crearla; hacer primero la copia y el `DROP` + `RENAME` dentro de una transacción (SQLite tiene DDL transaccional) para que el intercambio sea atómico; no dejar `pagos_tmp` huérfano si falla la copia. El `PRAGMA foreign_keys` debe quedar fuera de esa transacción (dentro no hace nada).
- **Si lo saltamos:** un error transitorio de la base convierte la app en un ladrillo que solo se arregla borrando el archivo de datos.

### 2. Cualquiera puede escribir pagos, y los pagos son ahora "el ingreso" — `services/impl/PagoServiceImpl.java:17-20` + `services/impl/ReporteServiceImpl.java:83-89`

- **Qué es:** A9 volvió a `pagos` la fuente única de ingresos, pero `PagoService.registrar` es una vía pública de escritura a esa tabla **sin** `Autorizacion.exigirAdmin` — a diferencia de toda mutación en la capa.
- **Problema:** cualquier llamador (una sesión de CAJERO, un script, un controlador futuro) puede insertar un pago de una venta existente → `ingresos` crece mientras `resumenJornada` (que suma ventas) no → la página de Reportes y el Dashboard muestran dinero distinto para el mismo día, y el ingreso reportado se puede inflar sin venta. Un segundo pago de la misma venta la cuenta dos veces: nada garantiza un pago por venta.
- **Fix:** agregar `Autorizacion.exigirAdmin("registrar pagos")` en `PagoServiceImpl.registrar` — el flujo de venta escribe directo por `pagoRepo.guardar` (`VentaServiceImpl.java:104`), así que cobrar no se rompe. Si de verdad se requieren pagos manuales, mete un pago por venta y que `ingresos` sume ventas.
- **Si lo saltamos:** "fuente única" solo se cumple mientras nadie más escriba en `pagos`.

---

## Should fix (deberían corregirse)

### 3. El dinero del día se calcula desde dos tablas distintas — `services/impl/ReporteServiceImpl.java:39-53` vs `:83-89`, `controllers/ReportesController.java:178-187`

- **Qué es:** el resumen del día suma ventas; el "Total vendido" de Reportes suma pagos; el promedio divide el total de pagos entre el número de ventas.
- **Problema:** solo coinciden mientras toda venta tenga exactamente un pago. Una venta escrita por la versión previa a A9 no tiene fila en `pagos` → Reportes muestra "Total vendido $0.00 / 1 ticket" → promedio $0.00, mientras el Dashboard sí muestra esa venta. Dos pantallas, mismo día, respuestas distintas.
- **Fix:** elige una fuente — que `resumenJornada` también use pagos (o que `ingresos` sume ventas). Son 2-3 líneas en `ReporteServiceImpl`, sin tocar el contrato; o inserta los pagos que falten.
- **Si lo saltamos:** reporte y dashboard pueden discrepar sobre el dinero del día sin forma de saber cuál tiene razón.

### 4. `pagoRepo.entre` truena con fechas nulas y su DAO hermano las maneja — `repositories/sqlite/SqlitePagoRepository.java:61-79` vs `repositories/sqlite/SqliteVentaRepository.java:83-110`

- **Qué es:** `entre` hace `desde.toString()` sin revisar null; el DAO de ventas arma el WHERE condicionalmente.
- **Problema:** `Servicios.reportes().ingresos(null, null)` lanza NullPointerException mientras `Servicios.reportes().ventas(null, null)` funciona. Hoy ninguna llamada de la UI pasa null (la página sale temprano), pero la interfaz nunca dijo que las fechas sean obligatorias.
- **Fix:** copia el patrón de WHERE condicional de `SqliteVentaRepository.entre` (~6 líneas), o rechaza fechas nulas al inicio de `ReporteServiceImpl.ingresos`.
- **Si lo saltamos:** el siguiente que pase null recibe un NPE en vez de "todo el periodo".

### 5. Folio y validación de stock ocurren fuera de la transacción — `services/impl/VentaServiceImpl.java:66-74, 90, 98-106`

- **Qué es:** el servicio lee el catálogo y calcula el folio (`MAX(id)+1`) *antes* de abrir la transacción, y escribe el stock con un valor absoluto desde esa lectura vieja (`producto.stock() - cantidad`).
- **Problema:** con dos terminales sobre el mismo .db (o un futuro hilo en segundo plano), las dos ventas pasan la validación de stock y el segundo INSERT muere por `UNIQUE constraint failed: ventas.folio` → se pierde la venta y el cajero tiene que reintentar; la escritura absoluta de stock además pisa un cambio si alguien movió el stock en medio.
- **Fix:** mueve la relectura y el cálculo del folio dentro de `BaseDatos.enTransaccion`, y escribe el stock con aritmética SQL (`SET stock = stock - ? WHERE id = ? AND stock >= ?`).
- **Si lo saltamos:** con un solo proceso y el hilo de JavaFX haciendo todo, esto no puede pasar — solo muerde cuando aparece un segundo proceso o hilo.

### 6. La migración no tiene prueba — el código más riesgoso del cambio

- **Qué es:** `migrarPagosSiFaltaForeignKeys` es lo único que puede dañar la base de un usuario, y nada lo ejercita.
- **Problema:** todas las pruebas crean una base nueva cuyo DDL ya trae la FK, así que la ruta de migración nunca corre. Una regresión se envía sin que nadie la note — y falla al arrancar, en la máquina del usuario, con sus datos.
- **Fix:** una prueba en `PersistenciaTest`: crear una base temporal, crear `pagos` **sin** FK más una fila, luego `BaseDatos.usarRuta(...)` + `conexion()` y comprobar que la FK existe y que la fila sobrevivió (~15 líneas).
- **Si lo saltamos:** la única red de seguridad de la migración es que alguien la pruebe a mano.

---

## Nice to have (opcionales)

### 7. Columna "Estado" y badge CANCELADA muertos — `controllers/ReportesController.java:106-119, 179-182, 211-219` con `services/impl/ReporteServiceImpl.java:91-94`

- **Qué es:** `ReporteService.ventas()` devuelve solo ventas COMPLETADA, así que la tabla nunca puede mostrar una cancelada.
- **Problema:** la columna Estado siempre dice "Completada", el CSV siempre escribe "Completada", y los dos `filter(v -> v.estado() == COMPLETADA)` siempre dan true. Cancelar una venta desde aquí hace que la fila desaparezca en vez de mostrar "Cancelada".
- **Fix:** o devuelve todas las ventas del periodo (y dejas los filtros), o borra la columna Estado, el badge y los dos filtros que siempre dan true (~15 líneas).
- **Si lo saltamos:** código muerto que sugiere que el reporte muestra ventas canceladas cuando no.

### 8. La página de reportes carga el periodo dos veces — `services/impl/ReporteServiceImpl.java:63-80` + `repositories/sqlite/SqliteVentaRepository.java:27-61`

- **Qué es:** `generar()` llama a `ventas()` y luego a `productosMasVendidos()`, y cada una relee las ventas del periodo; cada venta además dispara su propia consulta de renglones (una consulta por venta).
- **Problema:** un rango de 30 días a ~30 ventas/día (~900 ventas) ≈ 1800 consultas en el hilo de JavaFX por refresco → congelamiento breve de la UI. `PromocionServiceImpl.calcularDescuento` además consulta el catálogo una vez por renglón del carrito en cada clic del POS.
- **Fix:** arma "más vendidos" con la lista ya cargada, y trae los renglones con una sola consulta `WHERE venta_id IN (...)`. Vale la pena cuando el historial crezca.
- **Si lo saltamos:** no se rompe nada; la página se pone lenta conforme crece el historial.

### 9. `guardar` puede devolver un pago sin fecha — `repositories/sqlite/SqlitePagoRepository.java:82-95`

- **Qué es:** con `fecha` null guarda `LocalDateTime.now()` pero devuelve el objeto con null.
- **Problema:** quien llama ve un pago sin fecha; hoy `VentaServiceImpl` siempre pasa la fecha de la venta, así que nada se rompe.
- **Fix:** calcula la fecha una vez y úsala tanto en el INSERT como en el objeto devuelto (2 líneas).
- **Si lo saltamos:** hoy es inofensivo, mañana es una sorpresa.

---

## Veredicto

No lo mandaría así: corrige **1** (la migración puede dejar la app inservible y perder el historial de pagos en silencio) y **2** (los ingresos se pueden escribir sin autorización). **3** es el siguiente que duele — dos pantallas discrepando sobre el dinero del día. Lo demás es menor.

**Lean: -15 líneas posibles** (hallazgo 7: borrar la columna Estado, el badge y los dos filtros que siempre dan true).

## No revisado

- La UI de JavaFX nunca se renderizó (no hay pruebas de UI/FXML) — no se revisaron los FXML/CSS.
- El esquema previo a A9 de `pagos`: **revisado con git** (`git show b5ca1ed^:.../Esquema.java`) → `id, venta_id, metodo, monto, fecha` **sin FK**; la migración solo tiene que agregar la FK, y el caso "no such column: fecha" no puede venir del historial de este repo.
- Si un segundo proceso escribe sobre el mismo .db (nada en los docs lo dice — se asumió que no).
- No se generó el CSV ni la impresión.

---

*revision por ponytail — informe generado sobre el commit `b5ca1ed` (20-10-2026). No se modificó código.*
