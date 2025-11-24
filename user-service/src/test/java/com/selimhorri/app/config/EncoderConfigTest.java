package com.selimhorri.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.selimhorri.app.config.encoder.EncoderConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncoderConfigTest {

    @Test
    void testPasswordEncoderBeanCreation() {
        EncoderConfig config = new EncoderConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        
        assertNotNull(encoder);
        
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderEncoding() {
        EncoderConfig config = new EncoderConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        
        String password1 = "myPassword";
        String password2 = "myPassword";
        
        String encoded1 = encoder.encode(password1);
        String encoded2 = encoder.encode(password2);
        
        assertTrue(encoder.matches(password1, encoded1));
        assertTrue(encoder.matches(password2, encoded2));
    }
}
