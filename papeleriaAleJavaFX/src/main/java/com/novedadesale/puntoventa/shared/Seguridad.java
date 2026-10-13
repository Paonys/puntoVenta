package com.novedadesale.puntoventa.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Hashing de contraseñas. Cada usuario tiene contraseña individual. */
public final class Seguridad {

    private Seguridad() {
    }

    /** Hash SHA-256 en hexadecimal. */
    public static String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Compara una contraseña en claro contra su hash. */
    public static boolean verificar(String textoPlano, String hash) {
        return textoPlano != null && hash != null && hash.equals(hash(textoPlano));
    }
}
