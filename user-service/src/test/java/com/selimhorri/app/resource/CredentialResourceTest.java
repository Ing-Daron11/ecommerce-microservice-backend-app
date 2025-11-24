package com.selimhorri.app.resource;

import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.service.CredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialResourceTest {
    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private CredentialResource credentialResource;

    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    void findAllShouldReturnCredentials() {
        List<CredentialDto> credentials = new ArrayList<>();
        credentials.add(testCredentialDto);
        when(credentialService.findAll()).thenReturn(credentials);

        ResponseEntity<?> response = credentialResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).findAll();
    }

    @Test
    void findByUsernameShouldReturnCredential() {
        when(credentialService.findByUsername("testuser")).thenReturn(testCredentialDto);

        ResponseEntity<?> response = credentialResource.findByUsername("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).findByUsername("testuser");
    }

    @Test
    void findByIdShouldReturnCredential() {
        when(credentialService.findById(1)).thenReturn(testCredentialDto);

        ResponseEntity<?> response = credentialResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).findById(1);
    }

    @Test
    void saveShouldCreateCredential() {
        when(credentialService.save(any(CredentialDto.class))).thenReturn(testCredentialDto);

        ResponseEntity<?> response = credentialResource.save(testCredentialDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).save(any(CredentialDto.class));
    }

    @Test
    void updateWithoutIdShouldUpdateCredential() {
        when(credentialService.update(any(CredentialDto.class))).thenReturn(testCredentialDto);

        ResponseEntity<?> response = credentialResource.update(testCredentialDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).update(any(CredentialDto.class));
    }

    @Test
    void updateWithIdShouldUpdateCredentialById() {
        when(credentialService.update(anyInt(), any(CredentialDto.class))).thenReturn(testCredentialDto);

        ResponseEntity<?> response = credentialResource.update("1", testCredentialDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).update(1, testCredentialDto);
    }

    @Test
    void deleteByIdShouldDeleteCredential() {
        doNothing().when(credentialService).deleteById(1);

        ResponseEntity<?> response = credentialResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(credentialService, times(1)).deleteById(1);
    }
}
