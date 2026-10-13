package com.novedadesale.puntoventa.shared;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Hashing de contraseñas con PBKDF2-HMAC-SHA256 y salt aleatorio por usuario.
 * <p>
 * Formato almacenado: {@code pbkdf2sha256$<iteraciones>$<saltBase64>$<hashBase64>}.
 * <p>
 * Se conserva compatibilidad con los hashes SHA-256 en hexadecimal generados por
 * versiones anteriores ({@link #verificar} los reconoce automáticamente).
 * Las iteraciones se pueden ajustar con {@code -Dpos.security.iterations=N}.
 */
public final class Seguridad {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String FORMATO = "pbkdf2sha256";
    private static final int LONGITUD_SALT = 16;
    private static final int LONGITUD_HASH = 32;
    private static final int ITERACIONES = Integer.getInteger("pos.security.iterations", 120_000);
    private static final SecureRandom RANDOM = new SecureRandom();

    private Seguridad() {
    }

    /** Genera un hash nuevo con salt aleatorio. */
    public static String hash(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        byte[] salt = new byte[LONGITUD_SALT];
        RANDOM.nextBytes(salt);
        byte[] clave = derivar(texto.toCharArray(), salt, ITERACIONES, LONGITUD_HASH);
        return FORMATO + "$" + ITERACIONES + "$" + codificar(salt) + "$" + codificar(clave);
    }

    /** Compara una contraseña en claro contra el hash almacenado. */
    public static boolean verificar(String textoPlano, String almacenado) {
        if (textoPlano == null || almacenado == null) {
            return false;
        }
        if (almacenado.startsWith(FORMATO + "$")) {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4) {
                return false;
            }
            try {
                int iteraciones = Integer.parseInt(partes[1]);
                byte[] salt = decodificar(partes[2]);
                byte[] esperado = decodificar(partes[3]);
                byte[] calculado = derivar(textoPlano.toCharArray(), salt, iteraciones, esperado.length);
                return MessageDigest.isEqual(esperado, calculado);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return verificarLegado(textoPlano, almacenado);
    }

    /** Compatibilidad con los hashes SHA-256 en hexadecimal previos. */
    private static boolean verificarLegado(String textoPlano, String hex) {
        try {
            byte[] esperado = hexAbytes(hex);
            byte[] calculado = MessageDigest.getInstance("SHA-256")
                    .digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(esperado, calculado);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones, int longitud) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, longitud * 8);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String codificar(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decodificar(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    private static byte[] hexAbytes(String hex) {
        if (hex == null || (hex.length() % 2) != 0) {
            throw new IllegalArgumentException("Hexadecimal inválido");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Hexadecimal inválido");
            }
            bytes[i] = (byte) ((hi << 4) | lo);
        }
        return bytes;
    }
}
