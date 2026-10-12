# Reporte de Estado y Pendientes — Punto de Venta "Novedades Ale"

**Fecha:** 12 de octubre de 2026
**Proyecto:** Sistema de Punto de Venta — Papelería Novedades Ale
**Stack:** JavaFX 21.0.6 · Maven · FXML · CSS · Ikonli · Atomic Design
**Build:** `mvn -B clean compile` → **BUILD SUCCESS** (release 17)

---

## 1. Resumen ejecutivo

El proyecto quedó **limpio y compilable**. La base visual (identidad de marca proveniente del
prototipo de Figma/React) está preservada en los CSS mediante *design tokens*.

Sin embargo, **el frontEnd está apenas iniciado**: existen 4 páginas, de las cuales **3 son
placeholders vacíos**, y **7 secciones de navegación apuntan todas a la misma página** (el Punto
de Venta). Faltan por completo las vistas de **Login, Usuarios, Dashboard de Inicio, Pagos,
Ticket, Productos, Proveedores y Configuración**, y las capas de soporte (Modelos, ViewModels,
Servicios y Persistencia).

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
        │   ├── controllers/
        │   │   ├── MainLayoutController.java
        │   │   └── PointOfSaleController.java
        │   └── ui/Animations.java
        └── resources/com/novedadesale/puntoventa/
            ├── images/   logo.png · logo-dark.png
            ├── styles/   animations · atoms · molecules · organisms
            │             pages · templates · theme · utilities  (.css)
            └── views/
                ├── pages/     PointOfSalePage · InventarioPage
                │              ClientesPage · ReportesPage  (.fxml)
                └── templates/ MainLayout.fxml
```

**Conteo real:** 4 Page FXML · 1 Template FXML · 3 controllers Java · 8 CSS · 2 imágenes.

---

## 3. Limpieza realizada

Se dejó **únicamente el proyecto JavaFX** en la raíz del repositorio.

### 3.1 Eliminado

| Elemento | Motivo |
|---|---|
| `papeleriaAle/` (Swing + `.form`) | Prototipo obsoleto de NetBeans |
| `ProtitpoFigmaReact/` | Identidad visual ya embebida en los CSS |
| Documentación duplicada en 2 niveles (`.md`, `.docx`, `.txt`) | Redundancia |
| `Logo_blanco.png` / `Logo_pastel.png` (raíz) | Idénticos a `logo.png` / `logo-dark.png` |
| 23 FXML duplicados en `src/main/java/.../views/` | El runtime solo usa los de `resources` |
| `InicioController.java` + `InicioPage.fxml` | Nunca se cargaban (código muerto) |
| 18 FXML de atoms/molecules/organisms sin referencias | Scaffolding sin uso |
| `ProductCardController.java` / `CartItemController.java` | Huérfanos tras borrar los FXML |

### 3.2 Conservado

- `App.java`, `MainLayoutController.java`, `PointOfSaleController.java`, `Animations.java`.
- Las 4 páginas y el template en uso.
- Los 8 CSS con los *design tokens* (`pom.xml` → filtrado Maven sobre `**/*.css`).
- Logos de marca en `resources/images/`.

### 3.3 Verificación de compilación

- **Maven:** `C:\apache-maven-3.10.0` (requiere recargar el PATH del proceso).
- `BUILD SUCCESS` — 4 archivos fuente compilados.
- **0 placeholders** `${ui.*}` sin reemplazar en el CSS compilado (ej. `-fx-background-color: #F7F6FF`, `-fx-text-fill: #2411A7`).

---

## 4. Identidad visual

La identidad buscada desde el inicio (**paleta morada, gradientes de marca, tipografía Segoe UI,
estética tipo Apple — sombras *crisp*, bordes redondeados, micro-interacciones *spring***) se
conserva íntegra:

- Los *design tokens* viven como propiedades `ui.*` en `pom.xml`.
- Los valores del prototipo React están replicados: `#8453E9`, `#CF36D9`, `#535DF0`,
  base `#F7F6FF`, superficie `#FFFFFF`, texto `#2411A7`.
- `ui/Animations.java` implementa hover con escala, press y entrada de vista.

---

## 5. Alcance según la documentación del proyecto

Los documentos del proyecto (recuperables desde el historial de git) definen:

### 5.1 Requerimientos funcionales
- Inicio de sesión
- Control de usuarios
- Consultar y actualizar inventario
- Registrar pagos
- Realizar y consultar ventas
- Registro, actualización y eliminación de productos
- Calcular total
- Buscar productos

### 5.2 Alcance (propuesta)
- Módulo de **cobro rápido** (calcular totales y **cambios**)
- **Inventario dinámico** categorizado por secciones (cuadernos, mercería, artículos escolares)
  con descuento automático de existencias y **alertas de stock bajo**
- **Acceso seguro** con contraseñas individuales por integrante
- **Reportes de ventas** de fin de jornada (ingresos, productos más vendidos, **corte de caja**)
  e impresión

### 5.3 Requerimientos adicionales
- **Volátiles:** promociones (10% útiles escolares en regreso a clases; 15% regalos en febrero),
  nuevos métodos de pago, descuentos a clientes frecuentes.
- **Duraderos:** registrar Productos, Ventas, Inventario y Pagos.

### 5.4 Plan de calidad (alcance de pruebas)
Inicio de sesión · productos · ventas · pagos · inventario · tickets.
Pruebas: login válido · registrar venta · calcular total · actualizar inventario · generar ticket.

### 5.5 Atomic Design (diseño objetivo)
Átomos (8) · Moléculas (5) · Organismos (5) · Plantilla (1) · Páginas (5).
**Estado:** los componentes atómicos fueron removidos en la limpieza (eran scaffolding sin uso);
deben **reconstruirse** en la fase de frontEnd.

---

## 6. Análisis de brechas — Vistas / Secciones

| Módulo / Sección | Estado actual | Qué falta |
|---|---|---|
| **Inicio de sesión** | ❌ No existe | Vista login + validación de credenciales |
| **Control de usuarios** | ❌ No existe | CRUD de usuarios (contraseñas individuales) |
| **Inicio / Dashboard** | ⚠️ Carga el POS | Vista dashboard (métricas, resumen) |
| **Ventas / Cobro rápido** | ⚠️ Parcial | Carrito funcional, total y **cambio** |
| **Pagos** | ❌ No existe | Diálogo de pago, métodos, registrar pago |
| **Ticket / Comprobante** | ❌ No existe | Generar e imprimir ticket |
| **Productos** | ⚠️ Botón → POS | Vista CRUD (registrar, actualizar, eliminar) + búsqueda |
| **Inventario** | ⚠️ Placeholder | Categorías, stock bajo, descuento automático |
| **Clientes** | ⚠️ Placeholder | CRUD + descuentos a clientes frecuentes |
| **Proveedores** | ⚠️ Botón → POS | Vista de proveedores |
| **Reportes** | ⚠️ Placeholder | Ingresos, más vendidos, corte de caja, imprimir |
| **Ofertas / Promociones** | ⚠️ Botón → POS | Reglas de descuento por temporada |
| **Regalos** | ⚠️ Botón → POS | Sección/categoría de productos |
| **Configuración** | ⚠️ Botón → POS | Vista de configuración |

**Leyenda:** ❌ ausente · ⚠️ incompleto o placeholder.

### 6.1 Navegación actual vs. esperada

| Botón sidebar | Carga hoy | Debería cargar |
|---|---|---|
| Inicio | PointOfSalePage | Dashboard de Inicio |
| Ventas | PointOfSalePage | Nueva Venta / Cobro |
| Productos | PointOfSalePage | Productos (CRUD) |
| Regalos | PointOfSalePage | Regalos |
| Ofertas | PointOfSalePage | Ofertas / Promociones |
| Inventario | InventarioPage (vacía) | Inventario real |
| Clientes | ClientesPage (vacía) | Clientes real |
| Proveedores | PointOfSalePage | Proveedores |
| Reportes | ReportesPage (vacía) | Reportes reales |
| Configuración | PointOfSalePage | Configuración |

---

## 7. Capas de soporte faltantes (MVVM)

La documentación pide MVVM + persistencia, pero **no existe** ninguna de estas capas:

| Capa | Paquete propuesto | Estado |
|---|---|---|
| Modelo / Dominio | `models/` (Producto, Venta, ItemVenta, Cliente, Usuario, Pago, Categoria, Promocion) | ❌ |
| ViewModel | `viewmodels/` (estado observable, cálculo de totales) | ❌ |
| Servicios | `services/` (registrarVenta, actualizarStock, etc.) | ❌ |
| Repositorios / DAO | `repositories/` | ❌ |
| Persistencia | SQLite + JDBC | ❌ |
| Utilidades | `shared/{constants,utils}` | ❌ |

> Sin estas capas, las vistas no tendrán datos reales (hoy todo es mock hardcodeado).
> Recomendación de la propia documentación: **usar `BigDecimal`** para precios, no `double`.

---

## 8. Cómo recuperar la documentación original

Los archivos eliminados siguen en el historial de git:

```powershell
# Listar documentación versionada
git ls-tree -r --name-only HEAD | Select-String "\.(md|docx|txt)$"

# Recuperar un archivo a un directorio docs/
git checkout HEAD -- "Requerimientos Funcionales y No Funcionales (1).docx"
```

Se pueden restaurar en un `docs/` ordenado si se desea conservarlos como referencia.

---

## 9. Próximos pasos recomendados

**Opción A — Reconstruir primero la librería atómica (recomendada)**
1. Átomos: `PrimaryButton`, `SecondaryButton`, `IconButton`, `TextField`, `SearchField`,
   `Badge`, `LabelTitle`, `ToggleChip`.
2. Moléculas: `ProductCard`, `CartItem`, `CategoryChip`, `SearchBar`, `TotalsSummary`.
3. Organismos: `TopBar`, `Sidebar`, `ProductGrid`, `ShoppingCart`, `CategoriesBar`.

**Opción B — Atacar primero lo que más "se siente ausente"**
1. **Login** + **Control de usuarios**.
2. **Dashboard de Inicio** (métricas: ventas del día, productos, valor inventario, stock bajo;
   gráfico de ingresos; actividad; transacciones recientes — según el prototipo).
3. Conectar la navegación a páginas reales.

**Transversal:** crear las capas `models` / `viewmodels` para que las vistas dejen de usar mock.

---

*Reporte generado a partir del análisis del código, de la limpieza ejecutada y de la
documentación del proyecto (docx de requerimientos/propuesta, plan de calidad y reportes de
Atomic Design) recuperada desde el historial de git.*
