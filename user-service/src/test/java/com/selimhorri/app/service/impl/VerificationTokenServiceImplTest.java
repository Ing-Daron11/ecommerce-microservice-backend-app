package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.VerificationToken;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.VerificationTokenDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    private VerificationToken verificationToken;
    private VerificationTokenDto verificationTokenDto;
    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        credential = Credential.builder()
                .credentialId(1)
                .username("johndoe")
                .build();

        credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("johndoe")
                .build();

        verificationToken = VerificationToken.builder()
                .verificationTokenId(1)
                .token(UUID.randomUUID().toString())
                .expireDate(LocalDate.now().plusDays(1))
                .credential(credential)
                .build();

        verificationTokenDto = VerificationTokenDto.builder()
                .verificationTokenId(1)
                .token(verificationToken.getToken())
                .expireDate(verificationToken.getExpireDate())
                .credentialDto(credentialDto)
                .build();
    }

    @Test
    void shouldFindAllVerificationTokens() {
        given(verificationTokenRepository.findAll()).willReturn(List.of(verificationToken));

        List<VerificationTokenDto> result = verificationTokenService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToken()).isEqualTo(verificationToken.getToken());
    }

    @Test
    void shouldFindVerificationTokenById() {
        given(verificationTokenRepository.findById(1)).willReturn(Optional.of(verificationToken));

        VerificationTokenDto result = verificationTokenService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getVerificationTokenId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFoundById() {
        given(verificationTokenRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> verificationTokenService.findById(99))
                .isInstanceOf(VerificationTokenNotFoundException.class);
    }

    @Test
    void shouldSaveVerificationToken() {
        given(credentialRepository.findById(anyInt())).willReturn(Optional.of(credential));
        given(verificationTokenRepository.save(any(VerificationToken.class))).willReturn(verificationToken);

        VerificationTokenDto result = verificationTokenService.save(verificationTokenDto);

        assertThat(result).isNotNull();
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenCredentialNotFoundOnSave() {
        given(credentialRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> verificationTokenService.save(verificationTokenDto))
                .isInstanceOf(CredentialNotFoundException.class);
    }

    @Test
    void shouldUpdateVerificationToken() {
        given(verificationTokenRepository.findById(anyInt())).willReturn(Optional.of(verificationToken));
        given(verificationTokenRepository.save(any(VerificationToken.class))).willReturn(verificationToken);

        VerificationTokenDto result = verificationTokenService.update(verificationTokenDto);

        assertThat(result).isNotNull();
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFoundOnUpdate() {
        given(verificationTokenRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> verificationTokenService.update(verificationTokenDto))
                .isInstanceOf(VerificationTokenNotFoundException.class);
    }

    @Test
    void shouldDeleteVerificationTokenById() {
        given(verificationTokenRepository.existsById(anyInt())).willReturn(true);

        verificationTokenService.deleteById(1);

        verify(verificationTokenRepository, times(1)).deleteByIdCustom(anyInt());
    }
}
