# Reporte: Componentes generados con Atomic Design

**Proyecto:** papeleriaAleJavaFX  
**Ruta:** C:\Users\Sauwi\OneDrive\Documents\seminario\puntoVenta\papeleriaAleJavaFX\

## Resumen

- **Total FXML:** 16 archivos
- **Total Java (controllers):** 5 archivos
- **Componentes creados:** Atoms (6), Molecules (2), Organisms (3), Templates (1), Pages (4)

## 1. ATOMS (Átomos) - Elementos indivisibles
| Componente | Ubicación | Propósito |
|---|---|---|
| Badge.fxml | views/atoms/ | Badge/etiqueta para estados (success, warning, danger) |
| IconButton.fxml | views/atoms/ | Botón circular con ícono (Ikonli) |
| PrimaryButton.fxml | views/atoms/ | Botón primario con gradiente |
| SearchField.fxml | views/atoms/ | Campo de búsqueda con ícono |
| TextFieldAtom.fxml | views/atoms/ | Campo de texto genérico |
| ToggleChip.fxml | views/atoms/ | Chip seleccionable (categorías) |

## 2. MOLECULES (Moléculas) - Combinación de átomos
| Componente | Ubicación | Controller | Propósito |
|---|---|---|---|
| ProductCard.fxml | views/molecules/ | ProductCardController | Tarjeta de producto (imagen, precio, nombre, stock, botón agregar) |
| CartItem.fxml | views/molecules/ | CartItemController | Item del carrito (+/-, cantidad, subtotal, eliminar) |

## 3. ORGANISMS (Organismos) - Secciones completas
| Componente | Ubicación | Propósito |
|---|---|---|
| CategoriesBar.fxml | views/organisms/ | Barra de categorías (chips) |
| ProductGrid.fxml | views/organisms/ | Grid responsive de productos (FlowPane) |
| ShoppingCart.fxml | views/organisms/ | Panel lateral del carrito (items + totales + cobrar) |

## 4. TEMPLATES (Plantillas)
| Componente | Ubicación | Controller | Propósito |
|---|---|---|---|
| MainLayout.fxml | views/templates/ | MainLayoutController | Layout base (Topbar + Sidebar + Content) |

## 5. PAGES (Páginas)
| Componente | Ubicación | Controller | Propósito |
|---|---|---|---|
| PointOfSalePage.fxml | views/pages/ | PointOfSaleController | Punto de venta (principal) |
| InventarioPage.fxml | views/pages/ | — | Inventario (placeholder) |
| ClientesPage.fxml | views/pages/ | — | Clientes (placeholder) |
| ReportesPage.fxml | views/pages/ | — | Reportes (placeholder) |

## 6. Controllers creados
- controllers/MainLayoutController.java - Navegación y carga de páginas
- controllers/PointOfSaleController.java - Lógica POS
- controllers/molecules/ProductCardController.java - Datos y eventos de tarjeta
- controllers/molecules/CartItemController.java - Cantidad, subtotal, eliminar

## Conclusión
Se aplicó correctamente la técnica Atomic Design: átomos reutilizables, moléculas compuestas con lógica, organismos formados por moléculas, plantillas para layout y páginas con datos. El proyecto queda modular, escalable y coherente con el prototipo de diseño.
