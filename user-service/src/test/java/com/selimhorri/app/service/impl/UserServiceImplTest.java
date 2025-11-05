package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;
    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        credentialDto = CredentialDto.builder()
                .username("testuser")
                .password("plainPassword123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userDto = UserDto.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phone("1234567890")
                .credentialDto(credentialDto)
                .build();

        credential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("hashedPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user = User.builder()
                .userId(1)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phone("1234567890")
                .credential(credential)
                .build();

        credential.setUser(user);
    }

    @Test
    @DisplayName("Should encrypt password before saving user")
    void testSaveUserEncryptsPassword() {
        String encryptedPassword = "$2a$10$hashedPassword";
        when(passwordEncoder.encode("plainPassword123")).thenReturn(encryptedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.save(userDto);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserObjectNotFoundException when user not found by ID")
    void testFindByIdThrowsExceptionWhenNotFound() {
        int userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findById(userId);
        });

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should find user by username from credential")
    void testFindByUsername() {
        String username = "testuser";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.of(user));

        UserDto result = userService.findByUsername(username);

        assertNotNull(result);
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }

    @Test
    @DisplayName("Should throw exception when finding user by non-existent username")
    void testFindByUsernameThrowsExceptionWhenNotFound() {
        String username = "nonexistent";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findByUsername(username);
        });

        verify(userRepository, times(1)).findByCredentialUsername(username);
    }
}
