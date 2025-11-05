package com.selimhorri.app.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User-Credential Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    private User user;
    private Credential credential;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstName("Integration")
                .lastName("Test")
                .email("integration@test.com")
                .build();

        credential = Credential.builder()
                .username("integration@test.com")
                .password("$2a$10$test.encrypted.password")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(user)
                .build();

        user.setCredential(credential);
    }

    @Test
    @DisplayName("Should persist user and retrieve from database")
    @Transactional
    void testCreateAndRetrieveUser() {
        userRepository.save(user);
        entityManager.flush();

        User foundUser = userRepository.findById(user.getUserId()).orElse(null);

        assertNotNull(foundUser);
        assertEquals("Integration", foundUser.getFirstName());
        assertEquals("integration@test.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should create user with associated credential")
    @Transactional
    void testCreateUserWithCredentials() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        long userCount = userRepository.count();
        long credentialCount = credentialRepository.count();

        assertTrue(userCount > 0);
        assertTrue(credentialCount > 0);
    }

    @Test
    @DisplayName("Should find credential by username through integration")
    @Transactional
    void testRetrieveCredentialByUsernameIntegration() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findByUsername("integration@test.com").orElse(null);

        assertNotNull(foundCredential);
        assertEquals(user.getUserId(), foundCredential.getUser().getUserId());
    }

    @Test
    @DisplayName("Should validate encrypted password through integration")
    @Transactional
    void testPasswordEncryptionIntegration() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findByUsername("integration@test.com").orElse(null);

        assertNotNull(foundCredential);
        assertNotNull(foundCredential.getPassword());
        assertTrue(foundCredential.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("Should validate credential status through integration")
    @Transactional
    void testCredentialStatusIntegration() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findByUsername("integration@test.com").orElse(null);

        assertNotNull(foundCredential);
        assertTrue(foundCredential.getIsEnabled());
        assertTrue(foundCredential.getIsAccountNonExpired());
        assertTrue(foundCredential.getIsAccountNonLocked());
        assertTrue(foundCredential.getIsCredentialsNonExpired());
    }
}
