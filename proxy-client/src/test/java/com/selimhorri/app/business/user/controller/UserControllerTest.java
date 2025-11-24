package com.selimhorri.app.business.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.selimhorri.app.business.user.model.UserDto;
import com.selimhorri.app.business.user.model.response.UserUserServiceCollectionDtoResponse;
import com.selimhorri.app.business.user.service.UserClientService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserClientService userClientService;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setUserId(1);
    }

    @Test
    void shouldFindAll() {
        UserUserServiceCollectionDtoResponse response = new UserUserServiceCollectionDtoResponse();
        given(userClientService.findAll()).willReturn(ResponseEntity.ok(response));

        ResponseEntity<UserUserServiceCollectionDtoResponse> result = userController.findAll();

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo(response);
        verify(userClientService, times(1)).findAll();
    }

    @Test
    void shouldFindById() {
        given(userClientService.findById(anyString())).willReturn(ResponseEntity.ok(userDto));

        ResponseEntity<UserDto> result = userController.findById("1");

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo(userDto);
        verify(userClientService, times(1)).findById("1");
    }

    @Test
    void shouldSave() {
        given(userClientService.save(any(UserDto.class))).willReturn(ResponseEntity.ok(userDto));

        ResponseEntity<UserDto> result = userController.save(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo(userDto);
        verify(userClientService, times(1)).save(any(UserDto.class));
    }
}
