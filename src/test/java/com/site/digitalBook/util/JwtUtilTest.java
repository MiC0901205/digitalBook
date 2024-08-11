package com.site.digitalBook.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil(); 
    }

    @Test
    public void testGenerateToken() {
        String token = jwtUtil.generateToken("test@example.com");
        assertNotNull(token, "Le token ne doit pas être nul");
        System.out.println("Generated Token: " + token);
    }
}
