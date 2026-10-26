# Reporte de ejecución - Seguridad RBAC Fase 1

**Fecha:** 2026-10-23  
**Estado:** Completado  
**Tests:** mvn -B test → **BUILD SUCCESS** (49/49)

## Cambios realizados

### Servicios
- services/impl/Autorizacion.java: agregado exigirSesion(String) y exigirAdmin ahora exige sesión activa.
- Se agregó guardas de sesión en lecturas y operaciones internas de: Categoria, Cliente, Producto, Promocion, Proveedor, Reporte, Usuario (excepto utenticar), Venta, Pago.
- VentaServiceImpl: registra con identidad de Sesion, resuelve descuento de cliente desde repositorio, permite descuento manual solo a ADMIN, exige sesión para todas las operaciones. Constructor recibe ClienteRepository.
- PagoServiceImpl: egistrar exige ADMIN (operación interna); listar/porVenta exigen sesión.
- Servicios: actualizado para inyectar ClienteRepository en VentaServiceImpl.

### Modelos/Contratos
- models/SolicitudVenta: eliminado usuarioId/usuarioNombre. La identidad sale de la sesión.
- services/VentaService, PagoService sin cambios de firma salvo comportamiento.

### UI/Controladores
- controllers/PointOfSaleController: envía descuento manual BigDecimal.ZERO (servicio resuelve cliente/promociones), usa sesión implícita. Removido uso directo de Sesion import innecesario.

### Pruebas
- AutorizacionTest: sesión obligatoria, autenticación sin sesión, venta ligada a sesión, descuento de cliente vs manual por rol, pago directo bloqueado.
- NegocioTest, PersistenciaTest: actualizados al nuevo contrato de SolicitudVenta; inician sesión en @BeforeEach/@BeforeAll según corresponda.

## Validación
- Todas las pruebas verdes. Políticas: cajero solo vende/consulta, admin gestiona catálogo/usuarios/ventas/pagos/cancelaciones, venta nunca se suplantada.
