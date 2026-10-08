# Informe: Migración a JavaFX + Atomic Design - Punto de Venta Papelería Novedades Ale

**Fecha:** 06 de Octubre de 2026  
**Proyecto:** Sistema de Punto de Venta - Papelería Novedades Ale  
**Tecnologías:** JavaFX 21.0.6, Maven, FXML, CSS, Ikonli, SQLite, MVVM + Atomic Design

## 1. Objetivo

Migrar el prototipo actual en Java Swing (NetBeans GUI Builder) hacia una aplicación JavaFX moderna, con diseño limpio (no estilo 2000), arquitectura escalable y siguiendo la metodología Atomic Design.

## 2. Estado actual

- **Código base previo:** Solo istas/Inicio.java + Inicio.form (Swing), sin separación por capas.
- **Documentación existente:** Requerimientos, propuesta y plan de calidad (aprovechados para orientar el desarrollo).
- **Prototipo de referencia:** Se contó con un prototipo exportado desde Figma vía Anima (React + Tailwind) para extraer paleta, gradientes, tipografías y estructura visual.

## 3. Solución implementada

Se creó un nuevo proyecto Maven independiente: papeleriaAleJavaFX

### Arquitectura
- **Patrón:** MVVM (Model-View-ViewModel)
- **UI:** JavaFX + FXML + CSS
- **Componentización:** Atomic Design (Atoms → Molecules → Organisms → Templates → Pages)
- **Separación de capas:** models, repositories, services, viewmodels, views, controllers, shared

### Estructura generada

`
papeleriaAleJavaFX/
├── pom.xml
└── src/main/
    ├── java/com/novedadesale/puntoventa/
    │   ├── App.java
    │   ├── controllers/
    │   │   ├── MainLayoutController.java
    │   │   └── PointOfSaleController.java
    │   ├── models/ (base)
    │   ├── repositories/ (base)
    │   ├── services/ (base)
    │   ├── viewmodels/ (base)
    │   ├── shared/{constants,utils} (base)
    │   └── views/
    │       ├── atoms/ (vacío - pendiente crear FXML)
    │       ├── molecules/ (vacío - pendiente crear FXML)
    │       ├── organisms/ (vacío - pendiente crear FXML)
    │       ├── templates/MainLayout.fxml
    │       └── pages/
    │           ├── PointOfSalePage.fxml
    │           ├── InventarioPage.fxml
    │           ├── ClientesPage.fxml
    │           └── ReportesPage.fxml
    └── resources/com/novedadesale/puntoventa/
        ├── styles/
        │   ├── theme.css
        │   ├── animations.css
        │   ├── atoms.css
        │   ├── molecules.css
        │   ├── organisms.css
        │   ├── templates.css
        │   ├── pages.css
        │   └── utilities.css
        ├── images/
        │   ├── logo.png
        │   └── logo-dark.png
        ├── fonts/ (estructura)
        ├── views/ (estructura)
        └── icons/ (estructura)
`

### Dependencias Maven (pom.xml)
- javafx-controls, javafx-fxml, javafx-media, javafx-graphics (21.0.6)
- ikonli-javafx, ikonli-materialdesign2-pack, ikonli-feather-pack (12.3.1)
- sqlite-jdbc (persistencia)
- alidatorfx, controlsfx
- gson
- junit-jupiter (testing)

## 4. Diseño UI implementado

Basado en paleta del prototipo React:

| Token | Color | Uso |
|---|---|---|
| Primary/Accent | #CF36D9 → #8651DE → #535DF0 | Gradientes (topbar, sidebar, métricas) |
| Background | #F7F6FF | Fondo general |
| Surface | #FFFFFF | Tarjetas, carrito, inputs |
| Text Primary | #2411A7 | Títulos |
| Text Secondary | #655AC5 | Subtítulos/labels |
| Border | #DDD8FF | Bordes suaves |

**Características CSS:**
- Variables CSS (design tokens) reutilizables
- Animaciones suaves: fadeIn, fadeUp, scaleIn
- Efectos hover con scale/transition
- Clases reutilizables: .card, .btn-primary, .btn-secondary, .chip, .nav-item
- Responsive-friendly con HBox.vgrow, FlowPane, ScrollPane

## 5. Vistas implementadas

### MainLayout.fxml + MainLayoutController
- **Topbar:** Logo, buscador global, selector "Caja 1"
- **Sidebar:** Navegación principal + administración (Inicio, Ventas, Productos, Regalos, Ofertas, Inventario, Clientes, Proveedores, Reportes, Configuración)
- **Content:** StackPane para carga dinámica de páginas

### PointOfSalePage.fxml + PointOfSaleController
- **Métricas:** 4 tarjetas con gradientes (Ventas del día, Productos, Valor inventario, Stock bajo)
- **Grid de productos:** FlowPane responsive con tarjetas mock
- **Carrito lateral:** Lista de items + totales (Subtotal, IVA 16%, Descuento, Total) + botón "Cobrar"
- **UX:** Cards con hover, botones con transición

### Páginas placeholder
- InventarioPage.fxml, ClientesPage.fxml, ReportesPage.fxml creadas para navegación

## 6. Archivos generados

**Total:** 63 archivos  
**Ubicación:** C:\Users\Sauwi\OneDrive\Documents\seminario\puntoVenta\papeleriaAleJavaFX\

Archivos destacados: pom.xml, App.java, MainLayout.fxml, PointOfSalePage.fxml, 8 CSS, 2 controllers.

## 7. Estado de compilación

- Estructura válida para JavaFX/Maven
- FXML referenciados correctamente
- CSS cargados en App.java
- **Nota:** Maven no detectado en PATH actual. Compilación/ejecución prevista vía IDE (NetBeans/IntelliJ/VS Code) o agregando Maven al PATH.

## 8. Sugerencias para continuar mañana

### Prioridad ALTA (Fundamentos)
1. **Crear átomos FXML** (iews/atoms/)
   - PrimaryButton.fxml, SecondaryButton.fxml, IconButton.fxml
   - TextField.fxml, SearchField.fxml
   - Badge.fxml, LabelTitle.fxml, ToggleChip.fxml, QuantitySpinner.fxml
2. **Crear moléculas** (iews/molecules/)
   - ProductCard.fxml (extraer lógica de tarjeta actual)
   - CartItem.fxml (item carrito con + - eliminar)
   - CategoryChip.fxml, SearchBar.fxml, TotalsSummary.fxml
3. **Crear organismos** (iews/organisms/)
   - ProductGrid.fxml, ShoppingCart.fxml, CategoriesBar.fxml, TopBar.fxml, Sidebar.fxml

### Prioridad MEDIA (Funcionalidad)
4. **ViewModels** (iewmodels/)
   - BaseViewModel.java (observables)
   - PointOfSaleViewModel.java (estado carrito/productos, cálculos BigDecimal)
5. **Modelos** (models/)
   - Producto.java, Categoria.java, ItemVenta.java, Venta.java con getters/setters + propiedades JavaFX si aplica
6. **Lógica carrito**: Agregar/quitar/aumentar/disminuir, recalcular subtotal+IVA+total
7. **Búsqueda/filtros** por categoría

### Prioridad BAJA (Pulido)
8. **Ikonli**: Agregar íconos Feather/Material a sidebar/botones
9. **Responsive**: Ajustar FlowPane + breakpoints sutiles
10. **Persistencia SQLite**: Crear esquema + repositorios básicos
11. **Animaciones**: Aplicar .animate-* en transiciones de página

## 9. Recomendación para mañana

**Empezar por ÁTOMOS → MOLÉCULAS.** Extraer ProductCard a molécula reutilizable (FXML + controller ligero). Luego CartItem. Esto permitirá tener componentes puros y migrar PointOfSalePage a usar estos componentes, siguiendo fielmente Atomic Design.

**Justificación:** Reduce duplicidad, facilita ajustes visuales y deja base sólida para resto de módulos.
