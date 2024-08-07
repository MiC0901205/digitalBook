package com.site.digitalBook;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BCryptPasswordEncoderTest {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void testPasswordEncoding() {
        // Mot de passe en clair
        String rawPassword = "password";

        // Encodage du mot de passe
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Vérification que le mot de passe encodé est valide
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "Password should match");
    }

    @Test
    public void testPasswordEncodingDoesNotMatchDifferentPassword() {
        // Mot de passe en clair
        String rawPassword = "password";

        // Encodage du mot de passe
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Mot de passe incorrect
        String wrongPassword = "wrongPassword";

        // Vérification que le mot de passe incorrect ne correspond pas
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword), "Password should not match");
    }
}
