package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.CredentialDto;

@DisplayName("CredentialMappingHelper Unit Tests")
class CredentialMappingHelperTest {

    private Credential testCredential;
    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("hashedPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testCredentialDto = CredentialDto.builder()
                .credentialId(2)
                .username("admin")
                .password("securePassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_ADMIN)
                .isEnabled(false)
                .build();
    }

    @Test
    @DisplayName("Should verify credential object creation")
    void testCredentialCreation() {
        assertNotNull(testCredential);
        assertEquals(1, testCredential.getCredentialId());
        assertEquals("testuser", testCredential.getUsername());
        assertEquals(RoleBasedAuthority.ROLE_USER, testCredential.getRoleBasedAuthority());
    }

    @Test
    @DisplayName("Should verify credentialDto object creation")
    void testCredentialDtoCreation() {
        assertNotNull(testCredentialDto);
        assertEquals(2, testCredentialDto.getCredentialId());
        assertEquals("admin", testCredentialDto.getUsername());
        assertEquals(RoleBasedAuthority.ROLE_ADMIN, testCredentialDto.getRoleBasedAuthority());
    }

    @Test
    @DisplayName("Should handle credential builder")
    void testCredentialBuilder() {
        Credential newCredential = Credential.builder()
                .credentialId(3)
                .username("newuser")
                .password("newPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .build();

        assertNotNull(newCredential);
        assertEquals(3, newCredential.getCredentialId());
        assertEquals("newuser", newCredential.getUsername());
        assertTrue(newCredential.getIsEnabled());
    }
}
