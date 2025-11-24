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
@DisplayName("Credential-User Integration Tests")
class CredentialServiceIntegrationTest {

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
                .firstName("CredTest")
                .lastName("User")
                .email("credtest@test.com")
                .build();

        credential = Credential.builder()
                .username("credtest@test.com")
                .password("$2a$10$encrypted.test.password")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(user)
                .build();

        user.setCredential(credential);
    }

    @Test
    @DisplayName("Should create credential when user is persisted")
    @Transactional
    void testCredentialCreatedWithUser() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        long credentialCount = credentialRepository.count();
        assertTrue(credentialCount > 0);
    }

    @Test
    @DisplayName("Should associate credential with user account through integration")
    @Transactional
    void testCredentialUserAssociation() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        User persistedUser = userRepository.findById(user.getUserId()).orElse(null);
        assertNotNull(persistedUser);

        Credential foundCredential = credentialRepository.findFirstByUsername("credtest@test.com").orElse(null);
        assertNotNull(foundCredential);
        assertEquals(persistedUser.getUserId(), foundCredential.getUser().getUserId());
    }

    @Test
    @DisplayName("Should store encrypted password for credential through integration")
    @Transactional
    void testPasswordEncryptionIntegration() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findFirstByUsername("credtest@test.com").orElse(null);
        assertNotNull(foundCredential);
        assertNotNull(foundCredential.getPassword());
        assertTrue(foundCredential.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("Should retrieve credential by username through repository")
    @Transactional
    void testCredentialRetrievalByUsernameIntegration() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findFirstByUsername("credtest@test.com").orElse(null);
        assertNotNull(foundCredential);
        assertTrue(foundCredential.getIsEnabled());
        assertTrue(foundCredential.getIsAccountNonExpired());
    }

    @Test
    @DisplayName("Should validate credential enabled status through integration")
    @Transactional
    void testCredentialEnabledStatus() {
        userRepository.save(user);
        credentialRepository.save(credential);
        entityManager.flush();

        Credential foundCredential = credentialRepository.findFirstByUsername("credtest@test.com").orElse(null);
        assertNotNull(foundCredential);
        assertTrue(foundCredential.getIsEnabled());
        assertTrue(foundCredential.getIsAccountNonExpired());
        assertTrue(foundCredential.getIsAccountNonLocked());
        assertTrue(foundCredential.getIsCredentialsNonExpired());
    }
}
