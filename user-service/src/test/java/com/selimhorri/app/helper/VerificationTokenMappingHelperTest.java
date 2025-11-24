package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.VerificationToken;
import com.selimhorri.app.dto.VerificationTokenDto;

import java.time.LocalDate;

@DisplayName("VerificationTokenMappingHelper Unit Tests")
class VerificationTokenMappingHelperTest {

    private VerificationToken testToken;
    private VerificationTokenDto testTokenDto;
    private LocalDate expireDate;

    @BeforeEach
    void setUp() {
        expireDate = LocalDate.now().plusDays(1);
        testToken = VerificationToken.builder()
                .verificationTokenId(1)
                .token("test-token-123")
                .expireDate(expireDate)
                .build();

        testTokenDto = VerificationTokenDto.builder()
                .verificationTokenId(2)
                .token("another-token-456")
                .expireDate(expireDate)
                .build();
    }

    @Test
    @DisplayName("Should verify verification token object creation")
    void testTokenCreation() {
        assertNotNull(testToken);
        assertEquals(1, testToken.getVerificationTokenId());
        assertEquals("test-token-123", testToken.getToken());
        assertEquals(expireDate, testToken.getExpireDate());
    }

    @Test
    @DisplayName("Should verify verification token dto object creation")
    void testTokenDtoCreation() {
        assertNotNull(testTokenDto);
        assertEquals(2, testTokenDto.getVerificationTokenId());
        assertEquals("another-token-456", testTokenDto.getToken());
        assertEquals(expireDate, testTokenDto.getExpireDate());
    }

    @Test
    @DisplayName("Should handle token builder")
    void testTokenBuilder() {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        VerificationToken newToken = VerificationToken.builder()
                .verificationTokenId(3)
                .token("new-token-789")
                .expireDate(futureDate)
                .build();

        assertNotNull(newToken);
        assertEquals(3, newToken.getVerificationTokenId());
        assertEquals("new-token-789", newToken.getToken());
        assertEquals(futureDate, newToken.getExpireDate());
    }
}
