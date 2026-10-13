package com.novedadesale.puntoventa.repositories.sqlite;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Utilidades de mapeo JDBC compartidas por los repositorios SQLite. */
final class SqlUtil {

    private SqlUtil() {
    }

    static BigDecimal bd(String valor) {
        return valor == null || valor.isBlank() ? BigDecimal.ZERO : new BigDecimal(valor);
    }

    static String txt(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).toPlainString();
    }

    static Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }

    static int claveGenerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    static PreparedStatement preparar(java.sql.Connection c, String sql, boolean conClaves) throws SQLException {
        return conClaves ? c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : c.prepareStatement(sql);
    }
}
