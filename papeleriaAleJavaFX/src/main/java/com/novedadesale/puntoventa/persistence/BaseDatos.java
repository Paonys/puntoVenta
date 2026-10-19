package com.novedadesale.puntoventa.persistence;

import com.novedadesale.puntoventa.exceptions.ServiceException;

import java.sql.Connection;
import java.sql.DriverManager;
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
