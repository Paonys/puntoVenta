# Guía para seguir mañana - Punto de Venta JavaFX

**Prioridad:** Empezar por Atomic Design (átomos → moléculas) para mantener componentes reutilizables.

## 1. Crear ÁTOMOS (views/atoms) - 30-40 min

Crear estos FXML en src/main/java/com/novedadesale/puntoventa/views/atoms/ (o mejor en resources + controller). Sugerido: crear primero con estilos base.

| Archivo | Qué hacer |
|---|---|
| PrimaryButton.fxml | Botón principal (gradiente morado). Props: text, icon opcional, onAction |
| SecondaryButton.fxml | Botón bordeado blanco/fondo superficie |
| IconButton.fxml | Botón circular con ícono (Ikonli) |
| TextField.fxml | Input genérico con focus |
| SearchField.fxml | Search con ícono izquierda |
| Badge.fxml | Badge de estado (success/warning/danger) |
| LabelTitle.fxml | Títulos reutilizables |
| ToggleChip.fxml | Chips de categorías (activo/inactivo) |
| QuantitySpinner.fxml | Control + - cantidad |

## 2. Crear MOLÉCULAS - 40-60 min

| Archivo | Componentes | Notas |
|---|---|---|
| ProductCard.fxml + Controller | Imagen + Precio + Nombre + Stock + Botón Agregar | Extraer de PointOfSaleController. Pasar Producto vía método/setter |
| CartItem.fxml + Controller | Nombre, precio, qty +/- , subtotal, eliminar | Reutilizable en carrito |
| CategoryChip.fxml | ToggleChip reutilizable con evento |
| SearchBar.fxml | SearchField + contenedor |
| TotalsSummary.fxml | Subtotal, IVA, Descuento, Total | Recibe valores desde ViewModel |

## 3. Crear ORGANISMOS - 40 min

| Archivo | Uso |
|---|---|
| ProductGrid.fxml + Controller | FlowPane que recibe lista de productos (observable) |
| ShoppingCart.fxml + Controller | Carrito con lista + totales + botón cobrar |
| CategoriesBar.fxml | ScrollPane con chips de categorías |
| TopBar.fxml, Sidebar.fxml | Extraer de MainLayout para modularizar |

## 4. ViewModels + Models - 60-90 min

**Models (domain):**
- Producto (id, nombre, precio, stock, categoria, imagen)
- Categoria (id, nombre)
- ItemVenta (producto, cantidad, subtotal)
- Venta (id, fecha, items, total)

**ViewModels:**
- BaseViewModel con observables
- PointOfSaleViewModel: ObservableList<ItemVenta> carrito, ObservableList<Producto> productos, calcula totales (usar **BigDecimal**)

## 5. Orden recomendado mañana

**Paso 1 (Mañana - inicio):** ProductCard (molécula) - más impacto visual  
**Paso 2:** CartItem (molécula)  
**Paso 3:** ProductGrid + ShoppingCart (organismos)  
**Paso 4:** Refactorizar PointOfSalePage.fxml para usar organismos (menos código, más reutilizable)  
**Paso 5:** PointOfSaleViewModel + lógica carrito

## 6. Tips rápidos

- Usar **BigDecimal** para precios (no double)
- Pasar datos vía ViewModel (MVVM), no inyectar lógica pesada en Controllers
- Aprovechar Ikonli: FontIcon en FXML para íconos limpios
- Aplicar clases CSS existentes (.card, .btn-primary) antes de crear nuevas
- Mantener FXML livianos, CSS por nivel (atoms/molecules...) ya creados

**Recomendación clave:** No avances a páginas hasta tener moléculas sólidas. Ahí ganas velocidad y consistencia visual.
