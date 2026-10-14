# Informe de entrega — Handoff al Agente B (Frontend)

**Fecha:** 2026-10-14
**De:** Agente A (Backend + arranque de Frontend)
**Para:** Agente B (Frontend)
**Repo:** https://github.com/Paonys/puntoVenta.git — rama `main`
**Estado:** **Núcleo completo.** Backend con persistencia SQLite y **frontend con las 11 páginas
operativas**, login, navegación y utilidades compartidas. `mvn test` = **24/24 OK**.
Pendientes de alcance en **Fase 2** (`docs/plan_trabajo_2_agentes.md`, sección 4.bis).

> Regla de oro: **dos agentes nunca editan el mismo archivo a la vez.**
> Antes de empezar: `git pull --rebase origin main`.

---

## 1. Lo que está hecho y funcionando

### 1.1 Backend (Agente A) — completo
- **Persistencia SQLite** (`org.xerial:sqlite-jdbc`): `persistence/BaseDatos.java`,
  `Esquema.java`, `Seeder.java` (datos demo si la base está vacía).
- **Repositorios DAO** en `repositories/` + `repositories/sqlite/` (8 repositorios).
- **Servicios sobre repositorios** en `services/impl/*ServiceImpl` (9 servicios).
  Se eliminaron las implementaciones en memoria (`InMemory*`, `Datos`).
- **Contrato congelado intacto**: la UI consume `services.Servicios.*` igual que antes.
- **Hashing PBKDF2** con salt (`shared/Seguridad`), compatible con SHA-256 previo.
- **Validaciones** (`shared/Validadores`): correo, teléfono, precio > 0, descuento 0–100.
- **Tests JUnit 5**: `PersistenciaTest`, `SharedTest` → `mvn test` = **8/8 OK**.

### 1.2 Frontend (Agente B) — completo
| Archivo | Qué es |
|---|---|
| `ui/Sesion.java` | Usuario logueado en memoria: `Sesion.id()`, `Sesion.nombre()`, `Sesion.rol()`, `Sesion.esAdmin()`, `Sesion.iniciar(u)`, `Sesion.cerrar()`. |
| `ui/Dialogos.java` | Alertas con el tema de la app: `Dialogos.error/aviso/info/confirmar(msg)`, `Dialogos.aplicarEstilos(dialogPane)`. |
| `ui/Componentes.java` | **Utilidad compartida**: `iconButton`, `etiqueta`, `placeholder`, `moneyCell`, `numberCell`, `texto`, `nulo`. Evita duplicar estos patrones en cada controlador. |
| `controllers/LoginController.java` + `views/pages/LoginPage.fxml` | **B4 (Login)**: autenticación con `Servicios.usuarios().autenticar(...)`, `Sesion.iniciar(u)` y carga del `MainLayout`. Cierre de sesión desde el menú. |
| `controllers/ProductosController.java` | **B1**: CRUD real sobre `Servicios.productos()` + chips de categoría desde `Servicios.categorias()`. |
| `controllers/PointOfSaleController.java` + `views/pages/PointOfSalePage.fxml` | **B2/B3**: catálogo real, carrito con cantidades, totales con descuento automático por promociones, **diálogo de pago** (método + cambio) y **ticket** con intento de impresión (`PrinterJob`). |
| `Dashboard`, `Inventario`, `Clientes`, `Proveedores`, `Ofertas`, `Regalos`, `Reportes`, `Configuracion` (controller + FXML) | **B5/B6**: vistas reales conectadas a sus servicios. |
| `controllers/MainLayoutController.java` | **B7**: cada botón del sidebar carga su `*Page.fxml`; muestra nombre/rol del usuario (`Sesion`). |

La app **arranca en Login** y navega sin errores por todas las secciones
(verificado con `mvn clean compile` + `mvn test`).

---

## 2. Cómo consumir los servicios (obligatorio)

```java
import com.novedadesale.puntoventa.services.Servicios;

var productos = Servicios.productos().listar();          // List<Producto>
var cliente   = Servicios.clientes().guardar(new Cliente(0, "Ana", "555-1", "a@b.com", BigDecimal.ZERO));
var resumen   = Servicios.reportes().resumenJornada(LocalDate.now()); // ResumenJornada
var venta     = Servicios.ventas().registrar(solicitud); // calcula totales/folio/stock/cambio
```

- **Nunca** instanciar implementaciones (`*ServiceImpl`, `Sqlite*Repository`) directamente.
- Totales: única fuente de verdad = `shared.CalculadoraTotales.calcular(items, descuentoPct)`.
- Dinero siempre con `BigDecimal` (nunca `double`); formato con `shared.Formato.money(...)`.
- Usuario actual para la venta: `Sesion.nombre()`.
- UI (botones de acción, etiquetas, celdas y placeholders): usar `ui/Componentes`.

### Contrato de servicios
```java
List<Producto> productos().listar();
Cliente        clientes().guardar(Cliente);
ResumenJornada reportes().resumenJornada(LocalDate);
BigDecimal     reportes().ingresos(LocalDate desde, LocalDate hasta);
List<Venta>    reportes().ventas(LocalDate desde, LocalDate hasta);
List<ProductoMasVendido> reportes().productosMasVendidos(LocalDate desde, LocalDate hasta, int limite);
Usuario        usuarios().autenticar(usuario, password);
Venta          ventas().registrar(solicitud);
```

---

## 3. Tareas del Agente B — todas completadas

- [x] **B4. Login + control de usuarios.** Login con `Servicios.usuarios().autenticar(...)`,
  `Sesion.iniciar(u)`, arranque por Login en `App.java`, gestión de usuarios en Configuración.
  Usuarios semilla: `admin / admin123` (ADMIN), `caja1 / caja123` (CAJERO).
- [x] **B5. Dashboard de Inicio** conectado a `Servicios.reportes()` (ventas del día, ticket
  promedio, más vendidos, desglose por método de pago).
- [x] **B6. Vistas reales** (ya no hay placeholders):
  Inventario, Clientes, Proveedores, Reportes, Ofertas, Regalos y Configuración.
- [x] **B7. Sidebar (`MainLayoutController`)**: cada botón carga su página; nombre/rol del usuario visibles.
- [x] **B8. Errores de validación en la UI** con `Dialogos.error(e.getMessage())`.

---

## 3.bis Refactor de duplicación (post-entrega)

Se extrajeron a `ui/Componentes.java` los helpers que estaban repetidos en los controladores:
`iconButton`, `etiqueta`, `placeholder`, `moneyCell`, `numberCell`, `texto` y `nulo`. Los
controladores ahora los consumen en lugar de redefinirlos.

> Nota: `PointOfSaleController.etiqueta` pasó a `Componentes.etiqueta` (aplica `.label-body`),
> unificando el estilo del ticket con el resto de la app.

---

## 4. Protocolo git

```powershell
git pull --rebase origin main
# ...cambios...
git add <archivos>
git commit -m "descripcion"
git push origin main
```

- **Nunca** instanciar implementaciones ni editar `models/`, `services/` (interfaces) ni
  `shared/` sin coordinarlo con el Agente A.

---

## 5. Artefactos relevantes

```
controllers/  Login · MainLayout · Dashboard · PointOfSale · Productos · Inventario
              Clientes · Proveedores · Ofertas · Regalos · Reportes · Configuracion
ui/           Sesion · Dialogos · Componentes · Animations
views/pages/  11 páginas (.fxml)   views/templates/ MainLayout.fxml
```

`novedadesale.db` se crea al ejecutar la app y **está ignorado por git** (`.gitignore`).
