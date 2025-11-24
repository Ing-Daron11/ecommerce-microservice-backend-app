package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.exception.wrapper.UsernameAlreadyExistsException;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private Credential credential;
    private CredentialDto credentialDto;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .build();

        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .build();

        credential = Credential.builder()
                .credentialId(1)
                .username("johndoe")
                .password("encodedPassword")
                .user(user)
                .build();

        credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("johndoe")
                .password("rawPassword")
                .userDto(userDto)
                .build();
    }

    @Test
    void shouldFindAllCredentials() {
        given(credentialRepository.findAll()).willReturn(List.of(credential));

        List<CredentialDto> result = credentialService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(credential.getUsername());
    }

    @Test
    void shouldFindCredentialById() {
        given(credentialRepository.findById(1)).willReturn(Optional.of(credential));

        CredentialDto result = credentialService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCredentialId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenCredentialNotFoundById() {
        given(credentialRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.findById(99))
                .isInstanceOf(CredentialNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldFindCredentialByUsername() {
        given(credentialRepository.findByUsername("johndoe")).willReturn(Optional.of(credential));

        CredentialDto result = credentialService.findByUsername("johndoe");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void shouldThrowExceptionWhenCredentialNotFoundByUsername() {
        given(credentialRepository.findByUsername(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.findByUsername("unknown"))
                .isInstanceOf(UserObjectNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldSaveCredential() {
        given(credentialRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.findById(anyInt())).willReturn(Optional.of(user));
        given(credentialRepository.existsByUserUserId(anyInt())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(credentialRepository.save(any(Credential.class))).willReturn(credential);

        CredentialDto result = credentialService.save(credentialDto);

        assertThat(result).isNotNull();
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExistsOnSave() {
        given(credentialRepository.existsByUsername(anyString())).willReturn(true);

        assertThatThrownBy(() -> credentialService.save(credentialDto))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnSave() {
        given(credentialRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.save(credentialDto))
                .isInstanceOf(UserObjectNotFoundException.class);
    }

    @Test
    void shouldUpdateCredential() {
        given(credentialRepository.findById(anyInt())).willReturn(Optional.of(credential));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");
        given(credentialRepository.save(any(Credential.class))).willReturn(credential);

        CredentialDto result = credentialService.update(credentialDto);

        assertThat(result).isNotNull();
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    void shouldThrowExceptionWhenCredentialNotFoundOnUpdate() {
        given(credentialRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> credentialService.update(credentialDto))
                .isInstanceOf(CredentialNotFoundException.class);
    }

    @Test
    void shouldDeleteCredentialById() {
        given(credentialRepository.existsById(anyInt())).willReturn(true);

        credentialService.deleteById(1);

        verify(credentialRepository, times(1)).deleteByCredentialId(anyInt());
    }
}
