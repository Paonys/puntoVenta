# Fuente única del dinero del día — qué se hizo y por qué

> **revision por ponytail**

**Fecha:** 21 de octubre de 2026
**Alcance:** hallazgo **#3** (Should fix) de `docs/informe_revision_ponytail_2026-10-21.md`.
**Verificación:** `mvn -B test` → **40/40 OK** (39 previos + 1 nueva).

---

## El problema

El dinero del día se medía en **dos tablas distintas**:

| Pantalla | Qué suma | Dónde |
|---|---|---|
| Dashboard | ventas `COMPLETADA` | `ReporteServiceImpl.resumenJornada` |
| Reportes → "Total vendido" | filas de `pagos` | `ReporteServiceImpl.ingresos` → `PagoRepository.entre` |
| Reportes → "Ticket promedio" | total de pagos ÷ número de ventas | `ReportesController:178-187` |

Solo coinciden mientras **toda venta tenga exactamente un pago**. Si falta un pago, Reportes
muestra `$0.00` con N tickets y el Dashboard sí muestra la venta: dos pantallas, mismo día,
respuestas distintas. Y nada impedía el caso contrario: `PagoServiceImpl.registrar` es una vía
pública de escritura a `pagos` sin `Autorizacion.exigirAdmin`.

## La decisión: la fuente son las **ventas `COMPLETADA`**

Se evaluaron las dos candidatas:

| | Ventas `COMPLETADA` | Pagos |
|---|---|---|
| ¿Existe para toda venta? | Sí, siempre | Solo si se escribió el pago |
| ¿Puede haber de más? | No | Sí (pago duplicado o manual) |
| ¿Está en la misma transacción? | — | Sí, junto con la venta |
| Coste de leerla | Ya se leía para tickets y "más vendidos" | Consulta extra |

El pago se escribe **en la misma transacción que la venta** (`VentaServiceImpl:104`), así que
cuando existe, cuadra exactamente. El problema nunca es que difieran: es que *falte* o *sobre*.
Sumar ventas no puede divergir. Los pagos se siguen guardando (su FK y su `monto` siguen siendo
datos válidos), pero **dejan de ser una segunda fuente de verdad**.

## Qué cambió

| Archivo | Cambio |
|---|---|
| `services/impl/ReporteServiceImpl.java` | `ingresos()` suma `Venta::total` de las ventas `COMPLETADA` del rango; fuera la dependencia de `pagoRepo` |
| `services/Servicios.java` | `new ReporteServiceImpl(VENTA_REPO)` |
| `repositories/PagoRepository.java` | eliminado `entre()` (ya no lo usa nadie) |
| `repositories/sqlite/SqlitePagoRepository.java` | eliminada su implementación y el import de `LocalDate` |
| `services/ReporteService.java` | una línea de javadoc: `ingresos` y `resumenJornada` miden lo mismo |
| `test/.../NegocioTest.java` | prueba `ingresosYResumenDeJornadaCoinciden` |
| `docs/plan_trabajo_2_agentes.md`, `docs/reporte_estado_proyecto.md` | fila A9, estado (40/40) y la fila de Reportes |

**La UI no se tocó:** `ReportesController` ya pedía el total a `Servicios.reportes().ingresos(...)`
y los tickets a `ReportesController.ventas(...)` (que solo trae `COMPLETADA`). Con esto, el promedio
ticket = total ÷ tickets sale de los dos lados del mismo conjunto.

**Balance:** −44 líneas / +33. El cambio es un borrado neto: se eliminó una consulta, una
dependencia y un método de repositorio.

## Qué se arregló de paso

- **Hallazgo 4 (NPE con fechas nulas) desaparecido:** `ingresos` ahora hereda el manejo de nulos
  del DAO de ventas, que arma el `WHERE` condicional. `ingresos(null, null)` ya no revienta.
- **El "dinero del día" es verificable:** la prueba nueva falla si alguien reintroduce una
  segunda fuente.

## La prueba

```java
@Test
void ingresosYResumenDeJornadaCoinciden() {
    registrar(item(producto("P-001"), 2), null, MetodoPago.EFECTIVO, new BigDecimal("100"));
    registrar(item(producto("P-003"), 1), null, MetodoPago.TARJETA, null);

    var resumen = Servicios.reportes().resumenJornada(LocalDate.now());
    BigDecimal ingresos = Servicios.reportes().ingresos(LocalDate.now(), LocalDate.now());

    assertEquals(0, ingresos.compareTo(resumen.totalVentas()));
    assertEquals(0, ingresos.compareTo(new BigDecimal("226.08")));
    assertEquals(2, resumen.numeroVentas());
}
```

Comprueba el invariante ("las dos pantallas dicen lo mismo") y además el número absoluto, para
que un cambio futuro en el cálculo no pase desapercibido.

## Lo que **no** se hizo (y por qué)

- **No se arregló la migración de `pagos` (hallazgo 1, Must fix).** Es el riesgo más grave que
  queda: un fallo a mitad de migración deja `pagos_tmp` huérfano y la app ya no arranca; la
  salida es borrar el .db. Es un arreglo aparte, con su propia prueba (`PersistenciaTest`).
- **No se puso `Autorizacion.exigirAdmin` en `PagoServiceImpl.registrar` (hallazgo 2).** Sigue
  abierto, pero **bajó de severidad**: los pagos ya no son la fuente del ingreso, así que
  escribir uno sin autorización ya no infla lo que se reporta. Sigue siendo un agujero de
  integridad.
- **No se borró la columna "Estado" muerta (hallazgo 7).** Es cosmético y está en la UI.
- **No se optimizó la carga (hallazgo 8).** `ingresos` ahora relee las ventas del rango, así que
  la página de Reportes hace una lectura completa más que antes (3 en total). Con ~30
  ventas/día es inmemorial; con 900 ventas en 30 días son ~1800 consultas por refresco, y eso ya
  es un tema de congelamiento de UI.

## Riesgo para el usuario final

Ninguno funcional: el total de ingresos no cambia en la base actual (15 ventas, 15 pagos, mismo
total). Lo único que cambia es **de dónde se lee** y que Reportes y Dashboard ya no pueden
discrepar.

---

*revision por ponytail — informe del cambio del 21-10-2026.*
