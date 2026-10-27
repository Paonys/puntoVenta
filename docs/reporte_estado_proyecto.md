# Reporte de Estado y Pendientes — Punto de Venta "Novedades Ale"

**Fecha:** 12 de octubre de 2026 — **actualizado:** 2026-10-27
**Proyecto:** Sistema de Punto de Venta — Papelería Novedades Ale
**Stack:** JavaFX 21.0.6 · Maven · FXML · CSS · Ikonli · SQLite (JDBC)
**Build:** `mvn -B test` → **BUILD SUCCESS** · **61/61 tests OK**

---

## 1. Resumen ejecutivo

El **núcleo del proyecto está completo y compilable**: backend con persistencia SQLite (DAO +
servicios), **todas las vistas del frontend operativas** (11 páginas), **login**, navegación
conectada y utilidades compartidas.

Se pasó de un frontend con 3 páginas placeholder y 7 botones apuntando al Punto de Venta, a una
aplicación con **cada sección cargando su propia página** conectada a datos reales.

> **Alcance pendiente (Fase 2).** Ya están cerradas: transacciones/folio/promociones en la venta
> (A7), *gating* por rol en la UI (B9), cliente + descuento y **cancelar venta** en el POS (B10),
> **exportar CSV / imprimir** reportes (B11), **autorización por rol en los servicios** con
> invariante de último `ADMIN` (A8), **FK `pago`→`venta` + fuente única de ingresos** (A9, con
> migración automática de bases existentes) y **detalle de productos con historial de cambios**
> (auditoría por triggers). Queda: **parámetros de negocio** en Configuración
> (B12), **PDF** de reportes, **empaquetado y pruebas de UI** (A10/B13). Detalle en
> `docs/plan_trabajo_2_agentes.md` (sección **4.bis**). **Falta decidir si la Fase 2 entra en la entrega.**
>
> **Seguridad RBAC (fases 1 y 2): ejecutadas.** Fase 1 (sesión obligatoria, identidad de venta
> desde la sesión, descuentos y pagos por rol) el 2026-10-23 y fase 2 (cambio obligatorio de
> contraseña, bloqueo a los 5 intentos, política de contraseñas, cierre por inactividad a los
> 15 min) el 2026-10-27. Informes en `docs/reporte_ejecucion_seguridad_rbac_fase1_2026-10-23.md`
> y `docs/reporte_ejecucion_seguridad_rbac_fase2_2026-10-27.md`.
> **Solo queda la verificación manual de la S13** (migración sobre una copia de la base real y
> los 4 escenarios de la matriz).

---

## 2. Estado actual del repositorio

```
puntoVenta/
├── .gitignore
└── papeleriaAleJavaFX/
    ├── pom.xml
    └── src/main/
        ├── java/com/novedadesale/puntoventa/
        │   ├── App.java
        │   ├── controllers/    (12: Login, MainLayout, Dashboard, POS, Productos, Inventario,
        │   │                    Clientes, Proveedores, Ofertas, Regalos, Reportes, Configuracion)
        │   ├── exceptions/     ServiceException · ValidacionException
        │   ├── models/         (18 records/enums: Producto, Venta, ItemVenta, Cliente, Usuario,
        │   │                    Categoria, Promocion, Proveedor, Pago, Rol, EstadoVenta, …)
        │   ├── persistence/    BaseDatos · Esquema · Seeder
        │   ├── repositories/   (8 interfaces) + repositories/sqlite/ (8 DAO + SqlUtil)
        │   ├── services/       (10 interfaces) + services/impl/ (9 implementaciones + Autorizacion)
        │   ├── shared/         AppConstants · CalculadoraTotales · Formato · Seguridad · Validadores
        │   └── ui/             Sesion · Dialogos · Componentes · Animations
        └── resources/com/novedadesale/puntoventa/
            ├── images/   logo.png · logo-dark.png
            ├── styles/   animations · atoms · molecules · organisms
            │             pages · templates · theme · utilities  (.css)
            └── views/
                ├── pages/     11 páginas: Login, Dashboard, PointOfSale, Productos, Inventario,
                │              Clientes, Proveedores, Ofertas, Regalos, Reportes, Configuracion
                └── templates/ MainLayout.fxml
```

**Conteo real:** 11 Page FXML · 1 Template FXML · 12 controllers · 8 CSS · 2 imágenes.

---

## 3. Limpieza realizada (histórico)

Se dejó **únicamente el proyecto JavaFX** en la raíz del repositorio.

### 3.1 Eliminado
| Elemento | Motivo |
|---|---|
| `papeleriaAle/` (Swing + `.form`) | Prototipo obsoleto de NetBeans |
| `ProtitpoFigmaReact/` | Identidad visual ya embebida en los CSS |
| Documentación duplicada en 2 niveles | Redundancia |
| `Logo_blanco.png` / `Logo_pastel.png` (raíz) | Idénticos a los de `resources` |
| 23 FXML duplicados en `src/main/java/.../views/` | El runtime solo usa los de `resources` |
| `InicioController.java` + `InicioPage.fxml` | Código muerto |
| 18 FXML de atoms/molecules/organisms sin referencias | Scaffolding sin uso |
| `ProductCardController.java` / `CartItemController.java` | Huérfanos tras borrar los FXML |

### 3.2 Verificación de compilación
- `mvn -B clean compile` → **BUILD SUCCESS**.
- `mvn -B test` → **8/8 OK** (`PersistenciaTest`, `SharedTest`).
- **0 placeholders** `${ui.*}` sin reemplazar en el CSS compilado.

---

## 4. Identidad visual

La identidad buscada desde el inicio (**paleta morada, gradientes de marca, tipografía Segoe UI,
estética tipo Apple — sombras *crisp*, bordes redondeados, micro-interacciones *spring**) se
conserva íntegra:

- Las *design tokens* viven como propiedades `ui.*` en `pom.xml`.
- Valores del prototipo replicados: `#8453E9`, `#CF36D9`, `#535DF0`, base `#F7F6FF`,
  superficie `#FFFFFF`, texto `#2411A7`.
- `ui/Animations.java` implementa hover con escala, press y entrada de vista.

---

## 5. Alcance según la documentación del proyecto

### 5.1 Requerimientos funcionales
- Inicio de sesión ✅
- Control de usuarios ✅
- Consultar y actualizar inventario ✅
- Registrar pagos ✅
- Realizar y consultar ventas ✅
- Registro, actualización y eliminación de productos ✅
- Calcular total ✅
- Buscar productos ✅

### 5.2 Alcance
- Módulo de **cobro rápido** (totales y cambios) ✅
- **Inventario dinámico** categorizado, con descuento automático y **alertas de stock bajo** ✅
- **Acceso seguro** con contraseñas individuales (PBKDF2 + salt) ✅
- **Reportes de ventas** de fin de jornada (ingresos, más vendidos, corte) ✅ · **exportar CSV e imprimir** ✅ · PDF ⚠️ pendiente

### 5.3 Requerimientos adicionales
- **Volátiles:** promociones, métodos de pago, descuentos a clientes frecuentes ✅
- **Duraderos:** Productos, Ventas, Inventario y Pagos persistidos en SQLite ✅

---

## 6. Análisis de brechas — Vistas / Secciones

| Módulo / Sección | Estado |
|---|---|
| **Inicio de sesión** | ✅ Login + validación de credenciales |
| **Control de usuarios** | ✅ CRUD de usuarios en Configuración |
| **Inicio / Dashboard** | ✅ Métricas, más vendidos y ventas recientes |
| **Ventas / Cobro rápido** | ✅ Carrito, total y cambio · ✅ cliente + descuento · ✅ cancelar venta (ADMIN) |
| **Pagos** | ✅ Diálogo de pago con métodos |
| **Ticket / Comprobante** | ✅ Generación + impresión · ✅ rediseño de confirmación de cobro/ticket |
| **Productos** | ✅ CRUD real + búsqueda · ✅ detalle en página con **historial de cambios** (ADMIN) y asociación a proveedor + imagen |
| **Inventario** | ✅ Stock, alertas y ajustes |
| **Clientes** | ✅ CRUD + descuentos |
| **Proveedores** | ✅ CRUD |
| **Reportes** | ✅ Ingresos (fuente única: ventas `COMPLETADA`), más vendidos, corte por fechas · ✅ exportar CSV e imprimir · ⚠️ PDF pendiente |
| **Ofertas / Promociones** | ✅ Reglas de descuento por temporada |
| **Regalos** | ✅ Catálogo filtrado |
| **Configuración** | ✅ Usuarios · ⚠️ parámetros de negocio pendientes (B12) |
| **Seguridad / RBAC** | ✅ Gating en UI (B9) + **autorización en servicios** (A8): ADMIN gestiona catálogo/usuarios/cancela; CAJERO solo vende/consulta · ✅ sin auto-eliminación ni quedar sin ADMIN activo |

**Leyenda:** ✅ implementado · ⚠️ parcial / pendiente en Fase 2.

### 6.1 Navegación actual
Cada botón del sidebar carga su `*Page.fxml` correspondiente (`MainLayoutController`); el nombre
y el rol del usuario se muestran desde `ui/Sesion`.

---

## 7. Refactor de duplicación

Se creó `ui/Componentes.java` con los patrones que antes se repetían en cada controlador:
`iconButton`, `etiqueta`, `placeholder`, `moneyCell`, `numberCell`, `texto` y `nulo`. Todos los
controladores ahora los consumen, eliminando helpers privados duplicados.

---

## 7.bis Capas de soporte (MVVM)

| Capa | Paquete | Estado |
|---|---|---|
| Modelo / Dominio | `models/` | ✅ |
| Servicios | `services/` + `services/impl/` | ✅ |
| Repositorios / DAO | `repositories/` + `repositories/sqlite/` | ✅ |
| Persistencia | `persistence/` (SQLite + JDBC) | ✅ |
| Utilidades | `shared/` | ✅ |

---

## 8. Próximos pasos recomendados

Ejecutar lo que queda de la **Fase 2** de `docs/plan_trabajo_2_agentes.md` (sección 4.bis).
**Ya hecho:** A7, A8, A9, B9, B10, B11 (CSV + imprimir) + rediseño de confirmaciones de venta.
**Pendiente:**

1. **Frontend (B, media):** **B12** — parámetros de negocio en Configuración (IVA, umbral de stock
   bajo) sacándolos de `shared/AppConstants`; **requiere acuerdo con A** por ser contrato congelado.
2. **Reportes (B, baja):** **PDF**, que requiere una dependencia nueva en `pom.xml` (propiedad de A).
3. **Empaquetado y pruebas (A/B):** **A10/B13** — `jpackage`/`jlink` y pruebas de UI/FXML (hoy
   6 clases de test de lógica: `SharedTest`, `PersistenciaTest`, `NegocioTest`, `AutorizacionTest`,
   `CredencialesTest`, `InactividadTest`; **61/61**).
4. **Decisión de alcance:** confirmar si todo lo anterior entra en la entrega.
5. **Seguridad (S13):** verificación manual de fase 2 sobre una copia de la base real
   (`docs/reporte_ejecucion_seguridad_rbac_fase2_2026-10-27.md`).

---

*Reporte actualizado a partir del análisis del código actual (backend + frontend completos).*

