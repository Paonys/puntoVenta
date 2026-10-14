# Pendientes y brechas detectadas — **Sugerencia de Agente B**

**Fecha:** 2026-10-14
**De:** Agente B (Frontend)
**Para:** Agente A (Backend) y equipo
**Base analizada:** commit `f3c8ef1` (rama `main`)
**Método:** revisión del código real (`controllers/`, `services/impl/`, FXML), no solo de los informes.

> **Etiqueta: Sugerencia de Agente B.**
> Este documento NO es código ni una tarea asignada: es un inventario de lo que
> falta, para que el otro agente lo revise y decidamos prioridades.

---

## 0. Resumen

`docs/reporte_estado_proyecto.md` e `docs/informe_entrega_agente_B.md` afirman que el
proyecto está **"100% completo"**. Tras revisar el código, el **núcleo funciona**, pero
**no está 100% completo**: hay módulos que existen pero están a medias y requerimientos
de la documentación que no se cumplen.

Las brechas grandes, en orden de prioridad:

1. **No hay control de roles** (RBAC) en la interfaz.
2. **POS incompleto**: no elige cliente, no aplica descuento de cliente, no cancela ventas.
3. **Reportes**: no exporta ni imprime (el informe dice que sí).
4. **Configuración**: solo usuarios, sin parámetros.
5. **Robustez backend**: precio confiado desde la UI, venta no atómica, folio con carrera.
6. **Calidad/distribución**: sin pruebas de frontend, sin empaquetado.

---

## 1. Seguridad / roles — **Prioridad ALTA** (dueño: **B**, apoyo **A**)

- **No existe *gating* por rol en la UI.** `ui/Sesion.esAdmin()` solo se usa para mostrar
  la etiqueta "Administrador/Cajero" en el `MainLayoutController`; no controla el acceso.
- Un usuario **CAJERO** puede abrir **Configuración** y **crear / editar / eliminar
  usuarios**, y puede eliminar productos, proveedores y ofertas. El sidebar muestra todas
  las secciones a cualquier rol.
- **No hay autorización en la capa de servicio** (solo lo que la UI permita). Cualquier
  código que llame a `Servicios.usuarios().eliminar(...)` puede hacerlo.
- **Recomendación:** ocultar/deshabilitar secciones administrativas para no-admin
  (`Configuración`, y acciones destructivas) usando `Sesion.esAdmin()`; y definir en A si
  se quiere además autorización por rol en los servicios.

## 2. Punto de Venta — **Prioridad ALTA** (dueño: **B**)

- **No se puede seleccionar cliente en la venta.** En `PointOfSaleController.cobrar()`
  la `SolicitudVenta` se crea con `clienteId = null` y `clienteNombre = null`, así que
  **toda venta queda como "Público general"**. El módulo de Clientes existe y la venta
  soporta cliente, pero el POS nunca lo ofrece.
- **No se aplica el descuento de cliente frecuente.** El modelo `Cliente` tiene un campo
  de descuento, pero el cobro solo usa el descuento por promociones
  (`promociones().calcularDescuento(...)`).
- **No hay descuento manual** en el cobro.
- **No hay cancelación / devolución de ventas.** Existe `EstadoVenta.CANCELADA` y Reportes
  lo pinta, pero no hay ninguna acción en la UI para cancelar una venta ni reponer stock.

## 3. Reportes — **Prioridad MEDIA** (dueño: **B**)

- **No exporta ni imprime.** El reporte de estado dice que Reportes incluye "impresión",
  pero `ReportesController` solo consulta y pinta tablas/métricas: no hay botón de imprimir
  ni de exportar (CSV/PDF).
- `ReporteService.ingresos(...)` existe pero **no se usa**; los totales del reporte se
  recalculan manualmente en el controlador.

## 4. Configuración / parámetros — **Prioridad MEDIA** (dueño: **B**, apoyo **A**)

- Configuración gestiona **solo usuarios**. No hay edición de parámetros de negocio
  (IVA = 0.16 y umbral de stock bajo están fijos en `shared/AppConstants`).
- La documentación menciona "Usuarios y parámetros"; los parámetros no están.

## 5. Robustez del backend — **Prioridad ALTA** (dueño: **A**)

Detectado en `services/impl/VentaServiceImpl.java`:

- **Precio confiado desde la UI**: el registro de venta usa los precios que llegan en la
  `SolicitudVenta` en lugar de releerlos del repositorio de productos. Un cliente malicioso
  o un bug de UI podría registrar ventas a precio arbitrario.
- **Registro no atómico**: venta, ítems, pago y descuento de stock se escriben en pasos
  separados; un fallo intermedio deja datos inconsistentes (sin transacción).
- **Folio con posible condición de carrera**: la generación del folio no está protegida
  ante registros concurrentes.

## 6. Calidad y distribución — **Prioridad MEDIA/BAJA** (dueños: **B** pruebas UI, **A** build)

- **Sin pruebas de frontend**: solo hay 8 tests de backend (`PersistenciaTest`,
  `SharedTest`). No hay tests de controladores ni de flujos de caja.
- **Sin empaquetado/distribución**: no hay `jpackage`/`jlink`/instalador; solo se ejecuta
  desde Maven. El informe lo lista como "próximo paso", pero sigue pendiente.
- **Regalos** es de solo lectura y filtra por la categoría llamada exactamente "Regalos";
  si esa categoría no existe, la sección sale vacía.
- **Proveedores** es un CRUD aislado: no hay vínculo proveedor↔producto ni entradas de
  mercancía (compras/recepción).

---

## 7. Correspondencia con el plan

Las tareas **A0–A6** y **B0–B8** del `plan_trabajo_2_agentes.md` figuran como `[x]`.
Estas brechas son **posteriores al plan** y no están listadas en él; sugiero añadirlas como
una nueva fase (p. ej. **Fase 2**) si el equipo decide abordarlas.

## 8. Prioridad sugerida (para acordar)

| # | Brecha | Prioridad | Dueño |
|---|---|---|---|
| 1 | Control de roles en la UI | Alta | B |
| 2 | Cliente + descuento + cancelación en POS | Alta | B |
| 5 | Robustez de `VentaServiceImpl` (precio/atómico/folio) | Alta | A |
| 3 | Exportar/imprimir reportes | Media | B |
| 4 | Parámetros en Configuración | Media | B / A |
| 6 | Pruebas de UI + empaquetado | Media/Baja | B / A |

---

*Sugerencia de Agente B — para revisión del Agente A. No contiene cambios de código.*
