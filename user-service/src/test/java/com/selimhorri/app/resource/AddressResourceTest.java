package com.selimhorri.app.resource;

import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.service.AddressService;
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
class AddressResourceTest {
    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressResource addressResource;

    private AddressDto testAddressDto;

    @BeforeEach
    void setUp() {
        testAddressDto = AddressDto.builder()
                .addressId(1)
                .fullAddress("123 Main St, TestCity")
                .postalCode("12345")
                .city("TestCity")
                .build();
    }

    @Test
    void findAllShouldReturnAddresses() {
        List<AddressDto> addresses = new ArrayList<>();
        addresses.add(testAddressDto);
        when(addressService.findAll()).thenReturn(addresses);

        ResponseEntity<?> response = addressResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnAddress() {
        when(addressService.findById(1)).thenReturn(testAddressDto);

        ResponseEntity<?> response = addressResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).findById(1);
    }

    @Test
    void saveShouldCreateAddress() {
        when(addressService.save(any(AddressDto.class))).thenReturn(testAddressDto);

        ResponseEntity<?> response = addressResource.save(testAddressDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).save(any(AddressDto.class));
    }

    @Test
    void updateWithoutIdShouldUpdateAddress() {
        when(addressService.update(any(AddressDto.class))).thenReturn(testAddressDto);

        ResponseEntity<?> response = addressResource.update(testAddressDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).update(any(AddressDto.class));
    }

    @Test
    void updateWithIdShouldUpdateAddressById() {
        when(addressService.update(anyInt(), any(AddressDto.class))).thenReturn(testAddressDto);

        ResponseEntity<?> response = addressResource.update("1", testAddressDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).update(1, testAddressDto);
    }

    @Test
    void deleteByIdShouldDeleteAddress() {
        doNothing().when(addressService).deleteById(1);

        ResponseEntity<?> response = addressResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService, times(1)).deleteById(1);
    }
}
