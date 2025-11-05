package com.selimhorri.app.config.encoder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("EncoderConfig PasswordEncoder Unit Tests")
class EncoderConfigTest {

    private PasswordEncoder passwordEncoder;
    private static final String RAW_PASSWORD = "testPassword123!@#";

    @BeforeEach
    void setUp() {
        EncoderConfig encoderConfig = new EncoderConfig();
        passwordEncoder = encoderConfig.passwordEncoder();
    }

    @Test
    @DisplayName("Should encode password with BCrypt")
    void testEncodePassword() {
        String encodedPassword = passwordEncoder.encode(RAW_PASSWORD);

        assertNotNull(encodedPassword);
        assertNotEquals(RAW_PASSWORD, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"));
    }

    @Test
    @DisplayName("Should match raw password with encoded password")
    void testMatchPassword() {
        String encodedPassword = passwordEncoder.encode(RAW_PASSWORD);

        assertTrue(passwordEncoder.matches(RAW_PASSWORD, encodedPassword));
    }

    @Test
    @DisplayName("Should not match wrong password with encoded password")
    void testMismatchWrongPassword() {
        String encodedPassword = passwordEncoder.encode(RAW_PASSWORD);
        String wrongPassword = "wrongPassword";

        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    @DisplayName("Should produce different hashes for same password")
    void testEncodingSamePasswordProducesDifferentHashes() {
        String encodedPassword1 = passwordEncoder.encode(RAW_PASSWORD);
        String encodedPassword2 = passwordEncoder.encode(RAW_PASSWORD);

        assertNotEquals(encodedPassword1, encodedPassword2);
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, encodedPassword1));
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, encodedPassword2));
    }

    @Test
    @DisplayName("Should handle empty password")
    void testEncodeEmptyPassword() {
        String emptyPassword = "";
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testEncodeSpecialCharactersPassword() {
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
        String encodedPassword = passwordEncoder.encode(specialPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("P@ssw0rd", encodedPassword));
    }
}
