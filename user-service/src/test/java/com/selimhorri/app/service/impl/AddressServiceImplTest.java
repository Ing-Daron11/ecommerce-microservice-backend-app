package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Unit Tests")
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AddressDto addressDto;
    private Address address;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        user = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        addressDto = AddressDto.builder()
                .addressId(1)
                .fullAddress("123 Main Street")
                .postalCode("12345")
                .city("Test City")
                .userDto(userDto)
                .build();

        address = Address.builder()
                .addressId(1)
                .fullAddress("123 Main Street")
                .postalCode("12345")
                .city("Test City")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should save new address")
    void testSaveAddress() {
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDto result = addressService.save(addressDto);

        assertNotNull(result);
        assertEquals("123 Main Street", result.getFullAddress());
        assertEquals("12345", result.getPostalCode());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    @DisplayName("Should delete address by ID")
    void testDeleteAddress() {
        addressService.deleteById(1);

        verify(addressRepository, times(1)).deleteById(1);
    }
}
