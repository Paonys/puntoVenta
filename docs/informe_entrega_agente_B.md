# Informe de entrega — Handoff al Agente B (Frontend)

**Fecha:** 2026-10-13
**De:** Agente A (Backend + arranque de Frontend)
**Para:** Agente B (Frontend)
**Repo:** https://github.com/Paonys/puntoVenta.git — rama `main`
**Estado:** Backend completo. Frontend iniciado (infra + POS + Productos). **Falta B4–B8.**

> Regla de oro: **dos agentes nunca editan el mismo archivo a la vez.**
> Antes de empezar: `git pull --rebase origin main`.

---

## 1. Lo que YA está hecho y funcionando

### 1.1 Backend (Agente A) — completo
- **Persistencia SQLite** (`org.xerial:sqlite-jdbc`): `persistence/BaseDatos.java`,
  `Esquema.java`, `Seeder.java` (datos demo si la base está vacía).
- **Repositorios DAO** en `repositories/` + `repositories/sqlite/`.
- **Servicios sobre repositorios** en `services/impl/*ServiceImpl`.
  Se eliminaron las implementaciones en memoria (`InMemory*`, `Datos`).
- **Contrato congelado intacto**: la UI consume `services.Servicios.*` igual que antes.
- **Hashing PBKDF2** con salt (`shared/Seguridad`), compatible con SHA-256 previo.
- **Validaciones** (`shared/Validadores`): correo, teléfono, precio > 0, descuento 0–100.
- **Tests JUnit 5**: `PersistenciaTest`, `SharedTest` → `mvn test` = **8/8 OK**.

### 1.2 Frontend — infraestructura y pantallas hechas por Agente A
| Archivo | Qué es |
|---|---|
| `ui/Sesion.java` | Usuario logueado en memoria: `Sesion.id()`, `Sesion.nombre()`, `Sesion.rol()`, `Sesion.esAdmin()`, `Sesion.iniciar(u)`, `Sesion.cerrar()`. |
| `ui/Dialogos.java` | Alertas con el tema de la app: `Dialogos.error/aviso/info/confirmar(msg)`, `Dialogos.aplicarEstilos(dialogPane)`. **Úsalo para B8.** |
| `controllers/ProductosController.java` | **B1 hecho**: CRUD real sobre `Servicios.productos()` + chips de categoría desde `Servicios.categorias()`. |
| `controllers/PointOfSaleController.java` + `views/pages/PointOfSalePage.fxml` | **B2/B3 hechos**: catálogo real, carrito con cantidades, totales con descuento automático por promociones, **diálogo de pago** (método + cambio) y **ticket** con intento de impresión (`PrinterJob`). |

La app **arranca sin errores** con estas pantallas (verificado ejecutando `mvn javafx:run`;
no se revisó el aspecto visual final).

---

## 2. Cómo consumir los servicios (obligatorio para B)

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
- Usuario actual para la venta: `Sesion.id()` / `Sesion.nombre()`.

Firmas clave (contrato congelado):
```java
List<Producto> ProductoService.listar() / buscar(String) / stockBajo();
Producto       ProductoService.guardar(Producto);          // id==0 => crea
void           ProductoService.eliminar(int) / ajustarStock(int, int delta);
Usuario        UsuarioService.crear(String nombre, String usuario, String password, Rol rol);
Usuario        UsuarioService.actualizar(int id, String nombre, String usuario, Rol rol, boolean activo, String passOpcional);
Optional<Usuario> UsuarioService.autenticar(String usuario, String password);
List<Promocion>   PromocionService.listar() / activas(LocalDate);
BigDecimal        PromocionService.calcularDescuento(List<ItemVenta>, LocalDate);
ResumenJornada    ReporteService.resumenJornada(LocalDate);
List<ProductoMasVendido> ReporteService.productosMasVendidos(LocalDate desde, LocalDate hasta, int limite);
BigDecimal        ReporteService.ingresos(LocalDate desde, LocalDate hasta);
List<Venta>       ReporteService.ventas(LocalDate desde, LocalDate hasta);
```

---

## 3. Lo que FALTA — tareas del Agente B

- [ ] **B4. Login + control de usuarios.**
  - Ventana/página de Login usando `Servicios.usuarios().autenticar(usuario, password)`.
  - Al autenticar: `Sesion.iniciar(usuario)` y cargar `MainLayout`.
  - En `App.java`, arrancar por el Login (hoy arranca directo en `MainLayout`).
  - Usuarios semilla: `admin / admin123` (ADMIN), `caja1 / caja123` (CAJERO).
  - Gestión de usuarios (crear/editar/cambiar contraseña/activar) en la página de Configuración.
- [ ] **B5. Dashboard de Inicio** conectado a `Servicios.reportes()` (ventas del día, ticket
  promedio, más vendidos, desglose por método de pago).
- [ ] **B6. Vistas reales** (hoy son placeholders "en construcción"):
  - **Inventario**: tabla de productos + `ajustarStock(id, delta)` + alertas de stock bajo.
  - **Clientes**: CRUD con `Servicios.clientes()` (validar correo/teléfono/descuento).
  - **Proveedores**: CRUD con `Servicios.proveedores()`.
  - **Reportes**: usar `Servicios.reportes()` + `Servicios.ventas().porFecha(...)`.
  - **Ofertas**: CRUD de `Promocion` con `Servicios.promociones()`.
  - **Regalos**: catálogo filtrado por categoría "Regalos" (reusar estilo de Productos).
  - **Configuración**: usuarios (B4) y parámetros.
- [ ] **B7. Sidebar (`MainLayoutController`).** Hoy **6 botones cargan el POS** por error:
  `Inicio`, `Ventas`, `Regalos`, `Ofertas`, `Proveedores`, `Configuración`.
  Corregir cada `goTo...()` para que cargue su `*Page.fxml` correspondiente.
  Además, `searchField` y el nombre/avatar del usuario **no** se muestran aún
  (usar `Sesion`).
- [ ] **B8. Mostrar errores de validación en la UI.** Los servicios lanzan
  `ValidacionException` / `ServiceException`: captúralas y muéstralas con
  `Dialogos.error(e.getMessage())` en todos los formularios (POS y Productos ya lo hacen).

---

## 4. Protocolo git (recordatorio)

```powershell
git pull --rebase origin main
git checkout -b feat/<tarea>
# ...trabajo...
git add -A; git commit -m "<tarea>: descripcion"
git pull --rebase origin main
git push -u origin feat/<tarea>
# PR -> main (o merge acordado)
```

- **Un archivo = un dueño.** No tocar `models/`, `services/` (interfaces), `shared/`,
  `repositories/`, `persistence/` ni `pom.xml` sin acordarlo antes.
- Si necesitas un método de servicio que no existe: **pídelo**; no crees servicios paralelos.

---

## 5. Archivos que el Agente A tocó en esta entrega (para evitar conflictos)

```
ui/Sesion.java                      (nuevo)
ui/Dialogos.java                    (nuevo)
controllers/ProductosController.java        (reescrito)
controllers/PointOfSaleController.java      (reescrito)
resources/.../views/pages/PointOfSalePage.fxml  (ajustado)
docs/plan_trabajo_2_agentes.md      (actualizado)
docs/informe_entrega_agente_B.md    (este archivo)
```

`novedadesale.db` se crea al ejecutar la app y **está ignorado por git** (`.gitignore`).
