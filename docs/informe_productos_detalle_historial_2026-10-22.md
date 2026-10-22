# Informe: detalle de productos e historial de cambios

**Fecha:** 22 de octubre de 2026  
**Área:** Catálogo de productos (JavaFX + SQLite)  
**Estado:** implementado, compilado y probado

## Resumen

Se rediseñó la pantalla de Productos para eliminar los botones repetidos de cada
fila y abrir un detalle dentro de la misma página. Desde ahí se puede consultar,
editar y eliminar un producto, cambiar su imagen y revisar quién cambió qué y
cuándo. El historial se llena automáticamente mediante triggers de SQLite, sin
código extra en cada operación de negocio.

También quedaron integrados en el catálogo la asociación con proveedor y la
imagen de referencia del producto.

## Qué se hizo

### 1. Catálogo con proveedor e imagen

- El modelo `Producto` ahora incluye `proveedorId`, `proveedor` e `imagen`.
- La tabla `productos` agrega las columnas `proveedor_id` e `imagen`.
- El repositorio SQLite hace `LEFT JOIN` con `proveedores` y guarda/actualiza
  ambos campos, incluyendo proveedor nulo.
- El alta de producto permite elegir proveedor y una imagen de referencia con
  `FileChooser` (PNG, JPG, JPEG, GIF, BMP).
- La tabla del catálogo muestra una miniatura de la imagen y el proveedor.
- Los datos de ejemplo (`Seeder`) asocian los productos sembrados con sus
  proveedores.

### 2. Vista de detalle dentro de Productos

- Se eliminó la columna **Acciones** y sus botones por fila.
- Un clic sobre cualquier producto abre la vista de detalle; **Volver** regresa
  al catálogo.
- El detalle muestra:
  - Imagen grande (240 px) y nombre del archivo.
  - Código, nombre, categoría, proveedor, contacto del proveedor, precio, stock
    y stock mínimo.
  - Estado del producto como badge: Disponible, Stock bajo o Agotado.
- Los campos aparecen bloqueados en modo consulta y se habilitan al pulsar
  **Editar**.
- **Guardar** valida y persiste los cambios; **Cancelar** descarta lo editado y
  recarga el producto desde la base.
- **Eliminar** pide confirmación antes de borrar y vuelve al catálogo.

### 3. Historial de cambios

- Nueva tabla `productos_historial` con producto, acción, usuario, fecha y
  detalle.
- Triggers `AFTER INSERT`, `AFTER UPDATE` y `AFTER DELETE` sobre `productos`
  registran el movimiento automáticamente:
  - `CREADO` al insertar.
  - `ACTUALIZADO` al cambiar algún campo, con detalle campo por campo
    (código, nombre, categoría, proveedor, precio, stock, stock mínimo e
    imagen).
  - `ELIMINADO` al borrar, conservando el historial aunque el producto ya no
    exista.
- La comparación de valores usa `IS NOT`, por lo que también detecta cambios
  entre `NULL` y valores.
- El usuario se toma de la tabla `auditoria_contexto`, que se actualiza al
  iniciar y cerrar sesión (`Sesion.iniciar` / `Sesion.cerrar`). Sin sesión queda
  como `Sistema`.
- La fecha se guarda con `datetime('now', 'localtime')`.
- El detalle muestra el historial en una tabla ordenada del cambio más reciente
  al más antiguo.
- El historial solo está disponible para administradores; el servicio también
  exige rol ADMIN (`Autorizacion.exigirAdmin`).

### 4. Base de datos y migraciones

Al abrir una base existente se agregan automáticamente, sin borrar datos:

- Columnas `proveedor_id` e `imagen` en `productos`.
- Tabla `auditoria_contexto` con la fila inicial `(1, 'Sistema')`.
- Tabla `productos_historial`.
- Triggers de auditoría de productos.

Las bases nuevas ya se crean con todo el esquema y los triggers desde el inicio.

## Archivos principales

### Nuevos

- `models/HistorialProducto.java`
- `repositories/HistorialProductoRepository.java`
- `repositories/sqlite/SqliteHistorialProductoRepository.java`

### Modificados

- `controllers/ProductosController.java`
- `models/Producto.java`
- `persistence/BaseDatos.java`
- `persistence/Esquema.java`
- `persistence/Seeder.java`
- `repositories/sqlite/SqliteProductoRepository.java`
- `services/ProductoService.java`
- `services/impl/ProductoServiceImpl.java`
- `services/Servicios.java`
- `ui/Sesion.java`
- `resources/.../views/pages/ProductosPage.fxml`
- `resources/.../styles/molecules.css`
- `src/test/.../AutorizacionTest.java`
- `src/test/.../PersistenciaTest.java`

## Pruebas y verificación

- `mvn -B test`: **43 pruebas, 0 fallos**.
- Se agregó una prueba que verifica que guardar un producto registra `CREADO`
  con usuario y fecha, y que editarlo registra `ACTUALIZADO` con el detalle de
  los campos modificados.
- Se verificó que `ProductosPage.fxml` carga correctamente con todos sus
  controles e `initialize()`.
- La aplicación se reinició con los cambios y abre correctamente.

## Cómo usarlo

1. Iniciar sesión como administrador.
2. Entrar a **Productos**.
3. Hacer clic en un producto para abrir su detalle.
4. Usar **Editar** para modificar campos o cambiar la imagen; **Guardar** para
   aplicar o **Cancelar** para descartar.
5. Revisar la tabla **Historial de cambios** al final del detalle.
6. Usar **Eliminar** para borrar el producto o **Volver** para regresar.

## Consideraciones y límites

- El historial empieza a registrarse con esta versión; no se reconstruyen
  cambios anteriores de productos ya existentes.
- Las ventas que descuentan stock también generan un movimiento `ACTUALIZADO`
  a nombre del usuario que tenga la sesión abierta.
- La imagen se guarda como ruta absoluta del archivo, no se copia dentro del
  proyecto. Si el archivo se mueve o renombra, la vista previa deja de mostrarlo.
- El contacto del proveedor se actualiza al guardar el producto; mientras se
  edita, el cambio de proveedor no refresca el contacto hasta confirmar.
- Los cajeros pueden consultar el detalle, pero no editar, eliminar ni ver el
  historial.
