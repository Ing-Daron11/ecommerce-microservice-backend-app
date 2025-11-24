package com.selimhorri.app.exception.handler;

import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;
import com.selimhorri.app.exception.wrapper.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<?> response = globalExceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleAddressNotFoundShouldReturnNotFound() {
        AddressNotFoundException ex = new AddressNotFoundException("Address not found");

        ResponseEntity<?> response = globalExceptionHandler.handleAddressNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleVerificationTokenNotFoundShouldReturnNotFound() {
        VerificationTokenNotFoundException ex = new VerificationTokenNotFoundException("Token not found");

        ResponseEntity<?> response = globalExceptionHandler.handleVerificationTokenNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleUsernameAlreadyExistsShouldReturnConflict() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("Username exists");

        ResponseEntity<?> response = globalExceptionHandler.handleUsernameAlreadyExists(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleUserNotFoundShouldReturnNotFound() {
        UserObjectNotFoundException ex = new UserObjectNotFoundException("User not found");

        ResponseEntity<?> response = globalExceptionHandler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleCredentialNotFoundShouldReturnNotFound() {
        CredentialNotFoundException ex = new CredentialNotFoundException("Credential not found");

        ResponseEntity<?> response = globalExceptionHandler.handleCredentialNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleEmptyResultDataAccessShouldReturnNotFound() {
        org.springframework.dao.EmptyResultDataAccessException ex = 
            new org.springframework.dao.EmptyResultDataAccessException("No data", 1);

        ResponseEntity<?> response = globalExceptionHandler.handleEmptyResultDataAccess(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleEntityNotFoundShouldReturnNotFound() {
        javax.persistence.EntityNotFoundException ex = new javax.persistence.EntityNotFoundException("Entity not found");

        ResponseEntity<?> response = globalExceptionHandler.handleEntityNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
