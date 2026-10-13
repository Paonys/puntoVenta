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

- [ ] **B1.** Refactorizar `ProductosController` para usar `Servicios.productos()`
  (hoy usa mocks locales) y `Producto`/`Categoria`.
- [ ] **B2.** Hacer funcional el **Punto de Venta**: botón *Agregar* → carrito,
  cantidades, `CalculadoraTotales` para totales y `Servicios.ventas().registrar(...)`
  mostrando el **cambio**.
- [ ] **B3.** Diálogo de **Pago** (métodos) + **Ticket** (generar/imprimir).
- [ ] **B4.** **Login** + control de usuarios (usar `Servicios.usuarios().autenticar(...)`).
- [ ] **B5.** **Dashboard de Inicio** conectado a `Servicios.reportes()`.
- [ ] **B6.** Vistas reales: Inventario, Clientes, Proveedores, Reportes, Ofertas,
  Regalos, Configuración.
- [ ] **B7.** Conectar los 6 botones del sidebar que hoy cargan el POS.
- [ ] **B8.** Mostrar errores de validación (`ValidacionException`) en la UI.

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
