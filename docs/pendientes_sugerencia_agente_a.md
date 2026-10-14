# Análisis técnico y alcance — **Sugerencia de Agente A**

**Fecha:** 2026-10-14
**De:** Agente A (Backend)
**Para:** Agente B (Frontend) y equipo
**Base revisada:** `main` en `17579de` (incluye `docs/pendientes_sugerencia_agente_b.md`)
**Build:** `mvn -B clean test` → **BUILD SUCCESS**, **24/24** pruebas (16 nuevas)

> Este informe **complementa** `docs/pendientes_sugerencia_agente_b.md`. Verifico cada
> hallazgo del Agente B contra el código, agrego hallazgos de backend y propongo una
> **Fase 2** común para que ambos agentes trabajemos alineados.

---

## 0. Qué hice

1. `git pull --rebase` (traje `17579de` con el informe de B).
2. Leí y **verifiqué en el código** cada punto del informe de B.
3. Audité el acoplamiento del backend (servicios, repositorios, persistencia).
4. Añadí **pruebas de lógica de negocio** (Opción 3) → nueva clase `NegocioTest` (16 pruebas).
5. Alineé la documentación y dejé una **Fase 2** propuesta en `plan_trabajo_2_agentes.md`.

---

## 1. Verificación de los hallazgos del Agente B

| # | Hallazgo de B | Veredicto | Evidencia en código |
|---|---|---|---|
| 1 | Sin control de roles (RBAC) en la UI | **Confirmado** | `MainLayoutController.java:70` (el rol solo pinta una etiqueta); `ConfiguracionController.java:121` (alta/edición/borrado sin *gating*). |
| 1b | Sin autorización en la capa de servicio | **Confirmado** | No existe noción de sesión/rol en `services/`. Cualquier llamada a `Servicios.usuarios().eliminar(...)` se ejecuta. |
| 2 | POS no elige cliente → todo "Público general" | **Confirmado** | `PointOfSaleController.java:266` crea `SolicitudVenta(..., null, null, ...)`. |
| 2b | No aplica descuento de cliente frecuente | **Confirmado** | El cobro solo usa `descuentoPct` de promociones (`PointOfSaleController.java:159-161`). |
| 2c | No se puede cancelar una venta desde la UI | **Confirmado (solo UI)** | El backend **sí** lo soporta: `VentaService.cancelar(int)` (`VentaService.java:30`, `VentaServiceImpl.java:97`). Falta la acción en la vista. |
| 3 | Reportes no exporta ni imprime | **Confirmado** | `ReportesController` no tiene botón de imprimir/exportar. |
| 3b | `ReporteService.ingresos` no se usa | **Confirmado** | El controlador recalcula totales; `ingresos(...)` queda huérfano. |
| 4 | Configuración solo gestiona usuarios | **Confirmado** | `ConfiguracionController` únicamente maneja `Usuario`. |
| 5 | Precio confiado desde la UI | **Confirmado** | `VentaServiceImpl.registrar` usa los precios de la `SolicitudVenta` sin releerlos del catálogo. **Prueba que lo evidencia:** `NegocioTest.ventaUsaElPrecioDeLaSolicitudNoElDelCatalogo`. |
| 5b | Registro no atómico | **Confirmado** | `BaseDatos.java:39` abre con `autoCommit(true)`; `registrar` escribe venta → stock → pago en pasos separados. |
| 5c | Folio con posible carrera | **Confirmado** | `VentaServiceImpl.java:94` usa `ventaRepo.contar() + 1`. Mitigante: `ventas.folio` es `UNIQUE` (`Esquema.java:71`), así que una colisión falla en vez de duplicar. |
| 6 | Sin pruebas de UI / sin empaquetado | **Confirmado** | No hay tests de controladores ni `jpackage`/`jlink`. |

**Conclusión:** el informe de B es preciso. Mis pruebas confirman el punto 5 (precio).

---

## 2. Hallazgos adicionales del Agente A (backend / arquitectura)

### A. La lógica de promociones vive en el controlador, no en el servicio (violación de capas)
`PointOfSaleController.recalcular()` (`:159-161`) llama a
`promociones().calcularDescuento(...)` y **pasa el porcentaje ya calculado** en la
`SolicitudVenta`. `VentaServiceImpl` **no** resuelve promociones: un llamador distinto (otra
pantalla, un test o una API futura) puede registrar ventas **sin descuento**. La regla de
negocio debe ejecutarse dentro del servicio (o de `CalculadoraTotales`).

### B. No hay transacciones; el andamiaje ya existe pero se ignora
`BaseDatos` mantiene **una conexión singleton** con `autoCommit=true`. `SqlUtil.preparar(Connection c, …)`
(`SqlUtil.java:34`) **ya recibe una `Connection`**, señal de que se pensó en transacciones, pero los
repositorios `Sqlite*` obtienen su conexión internamente y no aceptan una externa. Consecuencia:
una venta a medias (venta guardada, stock no descontado) es posible. Agregar transacciones es
**viable con bajo cambio** si se propaga esa `Connection`.

### C. Generación de folio no monótona ni robusta
`contar()+1` depende del número de filas: si se borraran ventas o hubiera huecos por IDs, el folio
puede repetirse (lo salva el `UNIQUE`, pero con un error al usuario). Mejor: `MAX(id)+1` o una
tabla de secuencia.

### D. Riesgo de dejar el sistema sin administradores
`UsuarioServiceImpl` permite desactivar/eliminar usuarios **sin comprobar** que quede al menos un
`ADMIN` activo ni que no sea el usuario en sesión. Es un pie para bloquear la app.

### E. `Pago` sin llave foránea a la venta
`pagos` (`Esquema.java:100-108`) no declara `FOREIGN KEY (venta_id)`. Quedan pagos huérfanos si se
borrara una venta. `venta_items` sí la tiene (`:97`).

### F. `ReporteService.ingresos` y `ventas(...)` sin uso directo
La vista duplica el cálculo que ya ofrece el servicio. Conviene centralizarlo (una sola
fuente de verdad, como `CalculadoraTotales`).

### G. Constantes de negocio congeladas
`AppConstants.IVA_TASA` y `UMBRAL_STOCK_BAJO` están fijos. El requerimiento de "parámetros"
(Configuración) choca con "constantes congeladas": hay que **decidir** si se vuelven configurables
(afectando a A) o si se declara explícitamente que no son configurables.

---

## 3. Pruebas añadidas (Opción 3)

Nueva clase `src/test/java/com/novedadesale/puntoventa/NegocioTest.java` (16 pruebas) sobre base
SQLite temporal. Cubre:

- **CalculadoraTotales:** IVA 16%, redondeo HALF_UP, entradas nulas/vacías.
- **Promociones:** descuento por categoría, no aplica a otras categorías, cálculo global mixto,
  tope al importe del renglón.
- **Ventas:** venta vacía falla, stock insuficiente falla, descuento de stock + registro de pago,
  folios no repetidos, cancelación repone stock y marca `CANCELADA`, y (caracterización)
  el uso del precio de la solicitud.
- **Reportes:** resumen por método de pago, más vendidos ordenados por cantidad, exclusiones de
  ventas canceladas.

**Resultado:** `mvn -B test` → **24/24 OK** (8 previas + 16 nuevas).

---

## 4. Alineación: Fase 2 propuesta (para ambos agentes)

Añadida a `docs/plan_trabajo_2_agentes.md` como **Fase 2** (posterior al plan A0–A6 / B0–B8):

| ID | Tarea | Dueño | Prioridad |
|---|---|---|---|
| A7 | `VentaServiceImpl`: releer precios del catálogo, **transacción** venta+items+stock+pago, folio robusto, resolver promociones **en el servicio** | A | Alta |
| A8 | Autorización por rol en servicios + no dejar sin ADMIN / no auto-eliminarse | A | Alta |
| A9 | `Pago` con FK a `venta`; usar `ReporteService.ingresos` como fuente única | A | Media |
| B9 | *Gating* por rol en la UI (ocultar Configuración y acciones destructivas a CAJERO) | B | Alta |
| B10 | POS: seleccionar cliente, aplicar descuento de cliente y **cancelar venta** (usa `ventas().cancelar`) | B | Alta |
| B11 | Reportes: exportar (CSV/PDF) e imprimir | B | Media |
| B12 | Configuración: parámetros (requiere acuerdo con A sobre G) | B/A | Media |
| A10/B13 | Empaquetado (`jpackage`/`jlink`) y pruebas de UI | A/B | Media/Baja |

> **Acuerdo pendiente:** ¿la Fase 2 entra en el alcance de entrega o solo lo ya cubierto por
> A0–A6/B0–B8? Recomiendo declararlo explícitamente para dejar de trabajar "a ciegas".

---

## 5. Correcciones de documentación

- `docs/reporte_estado_proyecto.md` e `docs/informe_entrega_agente_B.md` decían "100% completo".
  Se ajustan para reflejar que el **núcleo está completo** y que la **Fase 2** (arriba) queda
  pendiente de decisión.
- `docs/plan_trabajo_2_agentes.md` incorpora la **Fase 2** como fuente única de verdad.

---

## 6. Recomendaciones de arquitectura (concretas)

1. **Servicios como frontera de reglas:** descuentos, validación de stock y precios se resuelven
   en `services/`; el controlador solo presenta.
2. **Transacciones:** propagar `Connection` a los repositorios y envolver el registro de venta en
   `begin/commit/rollback`.
3. **Autorización:** introducir un `ContextoSesion` (o pasar el `Usuario`) a los servicios para
   centralizar permisos, además del *gating* visual en B.
4. **Fuente única de cálculo:** usar `ReporteService`/`CalculadoraTotales` en todas las vistas.

---

*Sugerencia de Agente A — verificación de backend, pruebas nuevas y propuesta de Fase 2.
Incluye la clase `NegocioTest`; no modifica código de producción.*
