package com.selimhorri.app.resource;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.service.UserService;
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
class UserResourceTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void findAllShouldReturnUsers() {
        List<UserDto> users = new ArrayList<>();
        users.add(testUserDto);
        when(userService.findAll()).thenReturn(users);

        ResponseEntity<?> response = userResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnUser() {
        when(userService.findById(1)).thenReturn(testUserDto);

        ResponseEntity<?> response = userResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).findById(1);
    }

    @Test
    void findByIdShouldThrowException() {
        when(userService.findById(anyInt())).thenThrow(new UserObjectNotFoundException("Not found"));

        assertThrows(UserObjectNotFoundException.class, () -> userResource.findById("999"));
    }

    @Test
    void findByUsernameShouldReturnUser() {
        when(userService.findByUsername("testuser")).thenReturn(testUserDto);

        ResponseEntity<?> response = userResource.findByUsername("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    void saveShouldCreateUser() {
        when(userService.save(any(UserDto.class))).thenReturn(testUserDto);

        ResponseEntity<?> response = userResource.save(testUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).save(any(UserDto.class));
    }

    @Test
    void updateWithoutIdShouldUpdateUser() {
        when(userService.update(any(UserDto.class))).thenReturn(testUserDto);

        ResponseEntity<?> response = userResource.update(testUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).update(any(UserDto.class));
    }

    @Test
    void updateWithIdShouldUpdateUserById() {
        when(userService.update(anyInt(), any(UserDto.class))).thenReturn(testUserDto);

        ResponseEntity<?> response = userResource.update("1", testUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).update(1, testUserDto);
    }

    @Test
    void deleteByIdShouldDeleteUser() {
        doNothing().when(userService).deleteById(1);

        ResponseEntity<?> response = userResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteById(1);
    }

    @Test
    void nicolasCarechimbaShouldReturnMessage() {
        String response = userResource.nicolasCarechimba();

        assertNotNull(response);
        assertEquals("Nicolas carechimba", response);
    }
}
