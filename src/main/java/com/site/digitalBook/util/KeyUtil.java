package com.site.digitalBook.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyUtil {

    // Méthode pour générer une clé AES de 128 bits
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Taille de la clé (128, 192 ou 256 bits)
        return keyGen.generateKey();
    }

    // Méthode pour convertir la clé en chaîne Base64 pour le stockage
    public static String getKeyAsString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Méthode pour recréer la clé à partir d'une chaîne Base64
    public static SecretKey getKeyFromString(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}

