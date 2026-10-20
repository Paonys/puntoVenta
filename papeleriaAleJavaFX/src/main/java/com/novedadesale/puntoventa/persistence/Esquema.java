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
                precio        TEXT NOT NULL DEFAULT '0',
                stock         INTEGER NOT NULL DEFAULT 0,
                stock_minimo  INTEGER NOT NULL DEFAULT 10,
                FOREIGN KEY (categoria_id) REFERENCES categorias(id)
            )
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
