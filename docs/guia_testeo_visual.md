# Guía de testeo visual — Punto de Venta "Novedades Ale"

**Fecha:** 2026-10-16
**Objetivo:** recorrido manual por la aplicación para verificar el avance real de la funcionalidad.
**Build verificado:** `mvn -B test` → **BUILD SUCCESS · 27/27 pruebas OK**.

---

## 1. Cómo ejecutar la aplicación

```powershell
# desde papeleriaAleJavaFX/
mvn javafx:run
```

Se abre la ventana **"Punto de Venta - Papelería Novedades Ale"**. La base SQLite
(`novedadesale.db`) se crea y siembra automáticamente en el directorio de trabajo si está vacía.

## 2. Usuarios semilla

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador (acceso total) |
| `caja1` | `caja123` | Cajero (sin Configuración, sin Eliminar) |

## 3. Ruta de testeo sugerida

1. **Login**
   - Entrar con `admin/admin123`.
   - Probar una contraseña incorrecta: debe mostrarse el diálogo de error (`Dialogos.error`).
2. **Inicio / Dashboard**
   - Verificar métricas del día, más vendidos y ventas recientes (datos desde `ReporteService`).
3. **Productos**
   - Crear, editar y buscar productos.
   - Cerrar sesión y entrar con `caja1`: el botón *Eliminar* debe desaparecer (gating por rol, B9).
4. **Ventas (Punto de Venta)**
   - Agregar productos al carrito, ajustar cantidades.
   - *Cobrar*: elegir **cliente** y aplicar su **descuento** (B10); el descuento de promociones lo
     resuelve el servicio solo (A7) — no digitarlo.
   - Confirmar el pago: ver método de pago, importe, **cambio** y ticket con opción de impresión.
5. **Reportes**
   - Filtrar por rango de fechas; verificar ingresos, más vendidos y ventas.
   - **Exportar CSV** e **Imprimir** (B11).
   - Con sesión `admin`: acción **Cancelar venta** (reponene stock y marca `CANCELADA`).
6. **Configuración** (solo `admin`)
   - CRUD de usuarios, cambiar rol, ver contador de ADMINs.
7. **Otras secciones**
   - **Clientes**: CRUD + descuento por cliente frecuentes (se usa en el POS).
   - **Inventario**: stock, alertas de stock bajo y ajustes.
   - **Ofertas / Regalos / Proveedores**: CRUD (Regalos es catálogo filtrado de solo lectura).

## 4. Pendientes visibles al probar (Fase 2, fuera de lo cubierto hoy)

- **PDF** en Reportes (CSV e impresión sí existen; falta exportar PDF — requiere dependencia en `pom.xml`).
- **Parámetros de negocio** en Configuración (IVA y umbral de stock aún fijos en `AppConstants`).
- Empaquetado/distribución (`jpackage`/`jlink`) — la app corre solo desde Maven.

> La **autorización por rol en la capa de servicio (A8)** ya está hecha: además del gating visual,
> los servicios lanzan `ServiceException` si un `CAJERO` intenta mutar catálogo/usuarios o cancelar
> ventas, y el sistema impide auto-eliminarse o quedar sin `ADMIN` activo (prueba: `AutorizacionTest`).

> Estado detallado de la Fase 2: `docs/plan_trabajo_2_agentes.md` (sección 4.bis).
