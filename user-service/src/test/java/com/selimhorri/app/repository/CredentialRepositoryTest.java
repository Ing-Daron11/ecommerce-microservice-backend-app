package com.selimhorri.app.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CredentialRepository Unit Tests")
class CredentialRepositoryTest {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Credential testCredential;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();

        testUser = userRepository.save(testUser);

        testCredential = Credential.builder()
                .username("johndoe")
                .password("$2a$10$hashedPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(testUser)
                .build();

        credentialRepository.save(testCredential);
    }

    @Test
    @DisplayName("Should find credential by username")
    void testFindByUsername() {
        Optional<Credential> found = credentialRepository.findFirstByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
        assertEquals(testUser.getUserId(), found.get().getUser().getUserId());
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsernameNotFound() {
        Optional<Credential> found = credentialRepository.findFirstByUsername("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should persist credential with user reference")
    void testCredentialPersistenceWithUserReference() {
        User newUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("9876543210")
                .build();

        newUser = userRepository.save(newUser);

        Credential newCredential = Credential.builder()
                .username("janesmith")
                .password("$2a$10$hashedPassword2")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_ADMIN)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(newUser)
                .build();

        Credential saved = credentialRepository.save(newCredential);

        assertNotNull(saved.getCredentialId());
        assertEquals("janesmith", saved.getUsername());
        assertNotNull(saved.getUser());
        assertEquals("Jane", saved.getUser().getFirstName());
    }

    @Test
    @DisplayName("Should retrieve credential with all enabled flags true")
    void testCredentialEnabledFlags() {
        Optional<Credential> found = credentialRepository.findFirstByUsername("johndoe");

        assertTrue(found.isPresent());
        assertTrue(found.get().getIsEnabled());
        assertTrue(found.get().getIsAccountNonExpired());
        assertTrue(found.get().getIsAccountNonLocked());
        assertTrue(found.get().getIsCredentialsNonExpired());
    }

    @Test
    @DisplayName("Should update credential password")
    void testUpdateCredentialPassword() {
        Optional<Credential> found = credentialRepository.findFirstByUsername("johndoe");
        assertTrue(found.isPresent());

        Credential credential = found.get();
        String oldPassword = credential.getPassword();
        credential.setPassword("$2a$10$newHashedPassword");

        credentialRepository.save(credential);

        Optional<Credential> updated = credentialRepository.findFirstByUsername("johndoe");
        assertTrue(updated.isPresent());
        assertNotEquals(oldPassword, updated.get().getPassword());
        assertEquals("$2a$10$newHashedPassword", updated.get().getPassword());
    }
}
