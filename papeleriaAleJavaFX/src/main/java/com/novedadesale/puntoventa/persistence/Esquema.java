package com.novedadesale.puntoventa.persistence;

/**
 * Definición del esquema SQLite (DDL). Se ejecuta una vez al abrir la base.
 * Los montos se guardan como TEXT para preservar la precisión de {@code BigDecimal}.
 */
final class Esquema {

    static final String[] DDL = {
            """
            CREATE TABLE IF NOT EXISTS categorias (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre  TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS productos (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo        TEXT NOT NULL UNIQUE,
                nombre        TEXT NOT NULL,
                categoria_id  INTEGER,
                proveedor_id  INTEGER,
                precio        TEXT NOT NULL DEFAULT '0',
                stock         INTEGER NOT NULL DEFAULT 0,
                stock_minimo  INTEGER NOT NULL DEFAULT 10,
                imagen        TEXT,
                FOREIGN KEY (categoria_id) REFERENCES categorias(id),
                FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auditoria_contexto (
                id       INTEGER PRIMARY KEY CHECK (id = 1),
                usuario  TEXT NOT NULL DEFAULT 'Sistema'
            )
            """,
            """
            INSERT OR IGNORE INTO auditoria_contexto (id, usuario) VALUES (1, 'Sistema')
            """,
            """
            CREATE TABLE IF NOT EXISTS productos_historial (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                producto_id  INTEGER NOT NULL,
                accion       TEXT NOT NULL,
                usuario      TEXT NOT NULL DEFAULT 'Sistema',
                fecha        TEXT NOT NULL,
                detalle      TEXT
            )
            """,
            """
            CREATE TRIGGER IF NOT EXISTS trg_productos_historial_insert
            AFTER INSERT ON productos
            BEGIN
                INSERT INTO productos_historial (producto_id, accion, usuario, fecha, detalle)
                VALUES (
                    NEW.id,
                    'CREADO',
                    COALESCE((SELECT usuario FROM auditoria_contexto WHERE id = 1), 'Sistema'),
                    datetime('now', 'localtime'),
                    'Código: ' || NEW.codigo || ' | Nombre: ' || NEW.nombre ||
                    ' | Precio: ' || NEW.precio || ' | Stock: ' || NEW.stock
                );
            END
            """,
            """
            CREATE TRIGGER IF NOT EXISTS trg_productos_historial_update
            AFTER UPDATE ON productos
            WHEN OLD.codigo IS NOT NEW.codigo
              OR OLD.nombre IS NOT NEW.nombre
              OR OLD.categoria_id IS NOT NEW.categoria_id
              OR OLD.proveedor_id IS NOT NEW.proveedor_id
              OR OLD.precio IS NOT NEW.precio
              OR OLD.stock IS NOT NEW.stock
              OR OLD.stock_minimo IS NOT NEW.stock_minimo
              OR OLD.imagen IS NOT NEW.imagen
            BEGIN
                INSERT INTO productos_historial (producto_id, accion, usuario, fecha, detalle)
                VALUES (
                    NEW.id,
                    'ACTUALIZADO',
                    COALESCE((SELECT usuario FROM auditoria_contexto WHERE id = 1), 'Sistema'),
                    datetime('now', 'localtime'),
                    TRIM(
                        CASE WHEN OLD.codigo IS NOT NEW.codigo
                             THEN 'Código: ' || OLD.codigo || ' -> ' || NEW.codigo || '. ' ELSE '' END ||
                        CASE WHEN OLD.nombre IS NOT NEW.nombre
                             THEN 'Nombre: ' || OLD.nombre || ' -> ' || NEW.nombre || '. ' ELSE '' END ||
                        CASE WHEN OLD.categoria_id IS NOT NEW.categoria_id
                             THEN 'Categoría actualizada. ' ELSE '' END ||
                        CASE WHEN OLD.proveedor_id IS NOT NEW.proveedor_id
                             THEN 'Proveedor actualizado. ' ELSE '' END ||
                        CASE WHEN OLD.precio IS NOT NEW.precio
                             THEN 'Precio: ' || OLD.precio || ' -> ' || NEW.precio || '. ' ELSE '' END ||
                        CASE WHEN OLD.stock IS NOT NEW.stock
                             THEN 'Stock: ' || OLD.stock || ' -> ' || NEW.stock || '. ' ELSE '' END ||
                        CASE WHEN OLD.stock_minimo IS NOT NEW.stock_minimo
                             THEN 'Stock mínimo: ' || OLD.stock_minimo || ' -> ' || NEW.stock_minimo || '. ' ELSE '' END ||
                        CASE WHEN OLD.imagen IS NOT NEW.imagen
                             THEN 'Imagen actualizada. ' ELSE '' END
                    )
                );
            END
            """,
            """
            CREATE TRIGGER IF NOT EXISTS trg_productos_historial_delete
            AFTER DELETE ON productos
            BEGIN
                INSERT INTO productos_historial (producto_id, accion, usuario, fecha, detalle)
                VALUES (
                    OLD.id,
                    'ELIMINADO',
                    COALESCE((SELECT usuario FROM auditoria_contexto WHERE id = 1), 'Sistema'),
                    datetime('now', 'localtime'),
                    'Código: ' || OLD.codigo || ' | Nombre: ' || OLD.nombre
                );
            END
            """,
            """
            CREATE TABLE IF NOT EXISTS clientes (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre     TEXT NOT NULL,
                telefono   TEXT,
                email      TEXT,
                descuento  TEXT NOT NULL DEFAULT '0'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS proveedores (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre    TEXT NOT NULL,
                contacto  TEXT,
                telefono  TEXT,
                email     TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre         TEXT NOT NULL,
                usuario        TEXT NOT NULL UNIQUE,
                password_hash  TEXT NOT NULL,
                rol            TEXT NOT NULL,
                activo         INTEGER NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS promociones (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre        TEXT NOT NULL,
                tipo          TEXT NOT NULL,
                valor         TEXT NOT NULL DEFAULT '0',
                fecha_inicio  TEXT,
                fecha_fin     TEXT,
                activa        INTEGER NOT NULL DEFAULT 1,
                categoria_id  INTEGER
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS ventas (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                folio           TEXT NOT NULL UNIQUE,
                fecha           TEXT NOT NULL,
                usuario_id      INTEGER,
                usuario_nombre  TEXT,
                cliente_id      INTEGER,
                cliente_nombre  TEXT,
                subtotal        TEXT NOT NULL DEFAULT '0',
                descuento       TEXT NOT NULL DEFAULT '0',
                iva             TEXT NOT NULL DEFAULT '0',
                total           TEXT NOT NULL DEFAULT '0',
                metodo_pago     TEXT NOT NULL,
                monto_recibido  TEXT NOT NULL DEFAULT '0',
                cambio          TEXT NOT NULL DEFAULT '0',
                estado          TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS venta_items (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id         INTEGER NOT NULL,
                producto_id      INTEGER,
                codigo           TEXT,
                nombre           TEXT,
                precio_unitario  TEXT NOT NULL DEFAULT '0',
                cantidad         INTEGER NOT NULL DEFAULT 0,
                importe          TEXT NOT NULL DEFAULT '0',
                FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pagos (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id  INTEGER NOT NULL,
                metodo    TEXT NOT NULL,
                monto     TEXT NOT NULL DEFAULT '0',
                fecha     TEXT NOT NULL,
                FOREIGN KEY (venta_id) REFERENCES ventas(id)
            )
            """
    };

    private Esquema() {
    }
}
