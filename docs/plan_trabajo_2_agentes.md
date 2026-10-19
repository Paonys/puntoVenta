# Plan de Trabajo — 2 Agentes IA en paralelo

**Proyecto:** Punto de Venta "Novedades Ale" · JavaFX 21 · Maven
**Estrategia:** división por capas (Frontend / Backend) con **contrato congelado**.
**Regla de oro:** *dos agentes nunca editan el mismo archivo a la vez*.

---

## 1. Reparto de roles

| | **Agente A — Backend** | **Agente B — Frontend** |
|---|---|---|
| **Posee** | `repositories/`, `persistence/`, `services/impl/`, `resources/db/`, `src/test/`, `pom.xml` | `controllers/`, `views/`, `styles/`, `viewmodels/`, `ui/` |
| **No toca** | vistas / CSS | `services/impl/`, `repositories/`, `persistence/`, `pom.xml` |
| **Consume** | `models`, `services` (interfaces), `shared` | `models`, `services` (interfaces), `shared`, `services.Servicios` |

- **`models/`, `services/` (interfaces) y `shared/` están CONGELADOS** (Fase 0).
  Si alguien necesita cambiarlos, se acuerda **primero** y se hace un commit exclusivo.
- **`pom.xml` lo edita solo el Agente A.** B pide dependencias nuevas por mensaje/PR.

## 2. Fase 0 — Contrato congelado (YA HECHO por Agente A)

Estructura creada:

```
models/      Producto, Categoria, Cliente, Proveedor, Usuario, Venta, ItemVenta,
             Pago, Promocion, Totales, SolicitudVenta, ResumenJornada,
             ProductoMasVendido + enums Rol, MetodoPago, EstadoVenta, TipoPromocion
shared/      AppConstants (IVA 0.16, umbral stock bajo), Formato, Seguridad,
             Validadores, CalculadoraTotales
exceptions/  ServiceException, ValidacionException
services/    CategoriaService, ProductoService, UsuarioService, ClienteService,
             ProveedorService, PromocionService, VentaService, PagoService,
             ReporteService, y el localizador Services.Servicios
services/impl/  Implementaciones (CategoriaServiceImpl, ...) sobre repositories
repositories/   Interfaces DAO + repositories/sqlite/ (persistencia SQLite)
persistence/    BaseDatos (conexión + esquema), Esquema (DDL), Seeder (datos demo)
ui/             Sesion (usuario logueado), Dialogos (alertas/confirmación), Animations
controllers/    MainLayout, PointOfSale (funcional), Productos (funcional)
```

> Persistencia: **SQLite** (archivo `novedadesale.db`, ignorado por git). El contrato
> `services` no cambió: la UI sigue usando `Servicios.*` igual que antes.

### Cómo consume la UI los servicios (obligatorio)

```java
import com.novedadesale.puntoventa.services.Servicios;
import com.novedadesale.puntoventa.models.Producto;

var productos = Servicios.productos().listar();
var venta = Servicios.ventas().registrar(new SolicitudVenta(...));
```

Nunca instanciar `InMemory...Service` directamente.

### Cálculo de totales (única fuente de verdad)

```java
Totales t = CalculadoraTotales.calcular(items, descuentoPorcentaje);
// t.subtotal(), t.descuento(), t.subtotalConDescuento(), t.iva(), t.total()
```

---

## 3. Tareas del Agente A (Backend)

- [x] **A0.** Fase 0: models + shared + exceptions + interfaces + impl en memoria.
- [x] **A1.** Persistencia SQLite + JDBC.
  - Dependencia `org.xerial:sqlite-jdbc` en el `pom.xml`.
  - `persistence/BaseDatos.java` (conexión única + esquema), `persistence/Esquema.java` (DDL),
    `persistence/Seeder.java` (datos de ejemplo si la base está vacía).
  - `repositories/` (interfaces DAO) + `repositories/sqlite/` (implementaciones).
- [x] **A1b.** Tests de humo con JUnit 5 (`src/test`): catálogo, autenticación,
  cálculo de totales y registro de venta con descuento de stock. `mvn test` → 4/4 OK.
- [x] **A2.** Servicios respaldados por repositorios SQLite (`services/impl/*ServiceImpl`);
  se eliminaron las implementaciones en memoria. **No** cambió la firma de `Servicios`.
- [x] **A3.** Autenticación con contraseñas hasheadas: PBKDF2-HMAC-SHA256 + salt
  aleatorio por usuario (`shared/Seguridad`); compatible con los hashes SHA-256 previos.
- [x] **A4.** `ReporteService` real (resumen de jornada, más vendidos, ingresos).
- [x] **A5.** Tests (`src/test/java`) con JUnit 5: `CalculadoraTotales`, registrar venta
  (descuento de stock), folios, autenticación, hashing y validadores. `mvn test` → 8/8 OK.
- [x] **A6.** Validaciones en la capa de servicio (correo, teléfono, precio > 0,
  descuento 0-100) vía `shared/Validadores`.

## 4. Tareas del Agente B (Frontend)

- [x] **B1.** Refactorizar `ProductosController` para usar `Servicios.productos()`
  y `Servicios.categorias()` (CRUD real; ya no usa mocks locales).
- [x] **B2.** Punto de Venta funcional: catálogo real, botón *Agregar* → carrito,
  cantidades, `CalculadoraTotales` para totales y `Servicios.ventas().registrar(...)`
  mostrando el **cambio**.
- [x] **B3.** Diálogo de **Pago** (métodos + cambio) + **Ticket** (con intento de impresión).
- [x] **B0 (infra UI).** `ui/Sesion` (usuario logueado) y `ui/Dialogos`
  (alertas/confirmación con el tema de la app).
- [x] **B4.** **Login** + control de usuarios (`Servicios.usuarios().autenticar(...)`,
  `Sesion.iniciar(...)`; arrancar `App` por el Login). Usuarios semilla:
  `admin/admin123`, `caja1/caja123`.
- [x] **B5.** **Dashboard de Inicio** conectado a `Servicios.reportes()`.
- [x] **B6.** Vistas reales: Inventario, Clientes, Proveedores, Reportes, Ofertas,
  Regalos y Configuración (ya no son placeholders "en construcción").
- [x] **B7.** Conectar los botones del sidebar a sus vistas reales
  (`Inicio`, `Ventas`, `Regalos`, `Ofertas`, `Proveedores`, `Configuración`…) y
  mostrar el usuario y su rol en el topbar con `Sesion`.
- [x] **B8.** Mostrar errores de validación (`ValidacionException`/`ServiceException`)
  en la UI con `Dialogos.error(...)` (POS, Productos y los CRUD nuevos).

> Detalle completo del handoff en `docs/informe_entrega_agente_B.md` (firmas de servicio,
> archivos tocados y qué falta).

---

## 4.bis Fase 2 — Pendientes acordados (posteriores al plan A0–A6 / B0–B8)

Fuente: `docs/pendientes_sugerencia_agente_b.md` (inventario de B) + `docs/pendientes_sugerencia_agente_a.md`
(verificación y hallazgos de A). **El núcleo funciona; esto es lo que falta para cerrar el alcance.**

| ID | Tarea | Dueño | Prioridad |
|---|---|---|---|
| **A7** ✅ | `VentaServiceImpl`: relee precios del catálogo, envuelve venta+items+stock+pago en **transacción**, folio robusto (`MAX(id)+1`) y resuelve **promociones dentro del servicio** — **hecho** | A | Alta |
| **A8** | Autorización por rol en servicios + impedir quedarse sin `ADMIN` o auto-eliminarse | A | Alta |
| **A9** | `Pago` con FK a `venta`; usar `ReporteService.ingresos` como fuente única | A | Media |
| **B9** ✅ | *Gating* por rol en la UI (**política: CAJERO solo vende/consulta; ADMIN crea/edita catálogo**): oculta Configuración, botones "Nuevo" y columnas de acciones (Productos, Clientes, Proveedores, Ofertas, Inventario, Reportes) a `CAJERO`. Unificado con `Componentes.soloAdmin` + columna oculta — **hecho** (`746488f`, `fe92245`) | B | Alta |
| **B10** ✅ | POS: seleccionar cliente, aplicar **descuento de cliente** y **cancelar venta** (usa `ventas().cancelar`) — **hecho** (`018832f`) | B | Alta |
| **B11** ✅ | Reportes: exportar **CSV** e **imprimir** — **hecho** (`415d9ad`). PDF queda pendiente: requeriría una dependencia en `pom.xml` (propiedad de A) | B | Media |
| **B12** | Configuración: parámetros de negocio (requiere acuerdo con A sobre las constantes congeladas) | B/A | Media |
| **A10/B13** | Empaquetado (`jpackage`/`jlink`) y pruebas de UI | A/B | Media/Baja |

> **Acuerdo pendiente:** declarar si la Fase 2 entra en el **alcance de entrega**. Hasta que no se
> decida, no se considera "terminado" el proyecto (evita trabajar "a ciegas").
>
> **Nota A7 ↔ B10:** con A7, `SolicitudVenta.descuentoPorcentaje` pasó a ser **solo el descuento del
> cliente**; las promociones las resuelve el servicio. El POS (`PointOfSaleController`) se ajustó
> para enviar únicamente el descuento de cliente y evitar el **doble conteo**. Cualquier otro
> llamador debe hacer lo mismo.

---

## 5. Protocolo de sincronización (git)

```powershell
# 1) SIEMPRE traer lo último antes de trabajar
git pull --rebase origin main

# 2) Trabajar en rama propia, commit pequeño y frecuente
git checkout -b feat/pos-carrito          # rama por tarea
git add -A; git commit -m "POS: carrito y registro de venta"
git pull --rebase origin main             # integrar lo del otro agente
git push -u origin feat/pos-carrito       # push
# 3) PR -> main (o merge directo acordado)
```

- Commits **pequeños y frecuentes**: menos superficie de conflicto.
- **Un archivo = un dueño.** Ante necesidad de tocar un archivo congelado,
  avisar antes y hacerlo en un commit dedicado.
- Si hay conflicto: `git pull --rebase` y resolver; nunca `push --force` a `main`.

## 6. Contrato congelado — resumen de firmas

```java
// services
List<Producto> ProductoService.listar() / buscar(String) / stockBajo();
Producto ProductoService.guardar(Producto);            // id==0 => crea
void     ProductoService.eliminar(int) / ajustarStock(int, int delta);
Optional<Usuario> UsuarioService.autenticar(String usuario, String password);
Venta    VentaService.registrar(SolicitudVenta);       // calcula totales, cambio, folio, stock
String   VentaService.siguienteFolio();
ResumenJornada ReporteService.resumenJornada(LocalDate);
```

> Si la UI necesita un método que no existe, **pedirlo**; no crear servicios paralelos.
