package com.novedadesale.puntoventa.persistence;

import com.novedadesale.puntoventa.exceptions.ServiceException;
import com.novedadesale.puntoventa.shared.Seguridad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * Conexión única a la base de datos SQLite y creación del esquema.
 * <p>
 * La ruta del archivo se puede cambiar con la propiedad de sistema {@code pos.db}
 * (útil para pruebas: {@code -Dpos.db=ruta\prueba.db} o {@code :memory:}).
 */
public final class BaseDatos {

    private static final String RUTA_POR_DEFECTO = "novedadesale.db";

    private static String ruta = System.getProperty("pos.db", RUTA_POR_DEFECTO);
    private static Connection conexion;
    private static boolean esquemaCreado;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver SQLite (org.xerial:sqlite-jdbc) no disponible", e);
        }
    }

    private BaseDatos() {
    }

    public static synchronized Connection conexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);
                conexion.setAutoCommit(true);
                try (Statement st = conexion.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            if (!esquemaCreado) {
                crearEsquema(conexion);
                Seeder.sembrarSiVacio(conexion);
                esquemaCreado = true;
            }
            return conexion;
        } catch (SQLException e) {
            throw new ServiceException("No se pudo abrir la base de datos: " + e.getMessage(), e);
        }
    }

    private static void crearEsquema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            for (String ddl : Esquema.DDL) {
                st.execute(ddl);
            }
        }
        migrarPagosSiFaltaForeignKeys(c);
        migrarProductosSiFaltanColumnas(c);
        migrarUsuariosSiFaltanColumnas(c);
    }

    /**
     * S8: bases creadas antes de la Fase 2 de seguridad no tienen las columnas
     * de credenciales. Además marca el cambio obligatorio en las cuentas
     * sembradas que todavía conservan su contraseña inicial.
     */
    private static void migrarUsuariosSiFaltanColumnas(Connection c) throws SQLException {
        agregarColumnaSiFalta(c, "usuarios", "debe_cambiar_password", "INTEGER NOT NULL DEFAULT 0");
        agregarColumnaSiFalta(c, "usuarios", "intentos_fallidos", "INTEGER NOT NULL DEFAULT 0");
        agregarColumnaSiFalta(c, "usuarios", "bloqueado_hasta", "TEXT");
        marcarClavesSembradasPorCambiar(c);
    }

    private static void marcarClavesSembradasPorCambiar(Connection c) throws SQLException {
        String[][] sembrados = {{"admin", "admin123"}, {"caja1", "caja123"}};
        for (String[] cuenta : sembrados) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT password_hash FROM usuarios WHERE usuario = ? AND debe_cambiar_password = 0")) {
                ps.setString(1, cuenta[0]);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && Seguridad.verificar(cuenta[1], rs.getString(1))) {
                        try (PreparedStatement up = c.prepareStatement(
                                "UPDATE usuarios SET debe_cambiar_password = 1 WHERE usuario = ?")) {
                            up.setString(1, cuenta[0]);
                            up.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    /**
     * Bases creadas antes de asociar proveedores o guardar imágenes no tienen
     * esas columnas. SQLite no permite agregar una FK con {@code ALTER TABLE},
     * pero la columna basta para la asociación; las bases nuevas sí llevan la
     * FK declarada en el DDL.
     */
    private static void migrarProductosSiFaltanColumnas(Connection c) throws SQLException {
        agregarColumnaSiFalta(c, "productos", "proveedor_id", "INTEGER");
        agregarColumnaSiFalta(c, "productos", "imagen", "TEXT");
    }

    private static void agregarColumnaSiFalta(Connection c, String tabla,
                                              String columna, String tipo) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + tipo);
        }
    }

    /**
     * A9: bases creadas antes de la tarea A9 tenían {@code pagos} sin FK a
     * {@code ventas}; reconstruye la tabla para añadirla. Idempotente: solo
     * actúa si la tabla existente no tiene ninguna foreign key.
     */
    private static void migrarPagosSiFaltaForeignKeys(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA foreign_key_list(pagos)")) {
            if (rs.next()) {
                return;
            }
        }
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = OFF");
            st.execute("""
                    CREATE TABLE pagos_tmp (
                        id        INTEGER PRIMARY KEY AUTOINCREMENT,
                        venta_id  INTEGER NOT NULL,
                        metodo    TEXT NOT NULL,
                        monto     TEXT NOT NULL DEFAULT '0',
                        fecha     TEXT NOT NULL,
                        FOREIGN KEY (venta_id) REFERENCES ventas(id)
                    )
                    """);
            st.execute("INSERT INTO pagos_tmp (id, venta_id, metodo, monto, fecha) "
                    + "SELECT id, venta_id, metodo, monto, fecha FROM pagos");
            st.execute("DROP TABLE pagos");
            st.execute("ALTER TABLE pagos_tmp RENAME TO pagos");
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            try (Statement st = c.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            } catch (SQLException ignorada) {
                e.addSuppressed(ignorada);
            }
            throw e;
        }
    }

    /**
     * Fija el usuario que registran los triggers de auditoría. Los cambios hechos
     * sin sesión quedan a nombre de "Sistema".
     */
    public static synchronized void registrarUsuarioAuditoria(String nombre) {
        try (PreparedStatement ps = conexion().prepareStatement(
                "INSERT INTO auditoria_contexto (id, usuario) VALUES (1, ?) "
                + "ON CONFLICT(id) DO UPDATE SET usuario = excluded.usuario")) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("No se pudo registrar el usuario de auditoría", e);
        }
    }

    /** Cambia la ruta de la base (reinicia la conexión). Para pruebas. */
    public static synchronized void usarRuta(String nuevaRuta) {
        cerrar();
        ruta = nuevaRuta;
    }

    public static synchronized void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignored) {
                // sin acción
            }
            conexion = null;
        }
        esquemaCreado = false;
    }

    /**
     * Ejecuta {@code trabajo} dentro de una transacción sobre la conexión compartida:
     * confirma si termina sin excepciones y revierte ante cualquier error. La
     * conexión vuelve siempre a modo autocommit para no afectar a las operaciones
     * posteriores.
     *
     * @return el resultado del trabajo (puede ser {@code null}).
     */
    public static <T> T enTransaccion(Supplier<T> trabajo) {
        return ejecutarEnTransaccion(trabajo);
    }

    private static synchronized <T> T ejecutarEnTransaccion(Supplier<T> trabajo) {
        Connection c = conexion();
        try {
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new ServiceException("No se pudo iniciar la transacción: " + e.getMessage(), e);
        }
        try {
            T resultado = trabajo.get();
            c.commit();
            return resultado;
        } catch (Exception e) {
            try {
                c.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new ServiceException("Error en la transacción: " + e.getMessage(), e);
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // sin acción
            }
        }
    }
}
