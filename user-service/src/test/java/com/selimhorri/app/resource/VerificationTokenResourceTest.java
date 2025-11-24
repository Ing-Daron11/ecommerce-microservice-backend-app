package com.selimhorri.app.resource;

import com.selimhorri.app.dto.VerificationTokenDto;
import com.selimhorri.app.service.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenResourceTest {
    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private VerificationTokenResource verificationTokenResource;

    private VerificationTokenDto testTokenDto;

    @BeforeEach
    void setUp() {
        testTokenDto = VerificationTokenDto.builder()
                .verificationTokenId(1)
                .token("testtoken123")
                .expireDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Test
    void findAllShouldReturnTokens() {
        List<VerificationTokenDto> tokens = new ArrayList<>();
        tokens.add(testTokenDto);
        when(verificationTokenService.findAll()).thenReturn(tokens);

        ResponseEntity<?> response = verificationTokenResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(verificationTokenService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnToken() {
        when(verificationTokenService.findById(1)).thenReturn(testTokenDto);

        ResponseEntity<?> response = verificationTokenResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(verificationTokenService, times(1)).findById(1);
    }

    @Test
    void saveShouldCreateToken() {
        when(verificationTokenService.save(any(VerificationTokenDto.class))).thenReturn(testTokenDto);

        ResponseEntity<?> response = verificationTokenResource.save(testTokenDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(verificationTokenService, times(1)).save(any(VerificationTokenDto.class));
    }

    @Test
    void updateWithIdShouldUpdateTokenById() {
        when(verificationTokenService.update(anyInt(), any(VerificationTokenDto.class))).thenReturn(testTokenDto);

        ResponseEntity<?> response = verificationTokenResource.update("1", testTokenDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(verificationTokenService, times(1)).update(1, testTokenDto);
    }

    @Test
    void deleteByIdShouldDeleteToken() {
        doNothing().when(verificationTokenService).deleteById(1);

        ResponseEntity<?> response = verificationTokenResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(verificationTokenService, times(1)).deleteById(1);
    }
}
