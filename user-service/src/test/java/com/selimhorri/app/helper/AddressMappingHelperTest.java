package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.dto.AddressDto;

@DisplayName("AddressMappingHelper Unit Tests")
class AddressMappingHelperTest {

    private Address testAddress;
    private AddressDto testAddressDto;

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .addressId(1)
                .fullAddress("123 Main St")
                .postalCode("12345")
                .city("Test City")
                .build();

        testAddressDto = AddressDto.builder()
                .addressId(2)
                .fullAddress("456 Oak Ave")
                .postalCode("54321")
                .city("Another City")
                .build();
    }

    @Test
    @DisplayName("Should verify address object creation")
    void testAddressCreation() {
        assertNotNull(testAddress);
        assertEquals(1, testAddress.getAddressId());
        assertEquals("123 Main St", testAddress.getFullAddress());
    }

    @Test
    @DisplayName("Should verify addressDto object creation")
    void testAddressDtoCreation() {
        assertNotNull(testAddressDto);
        assertEquals(2, testAddressDto.getAddressId());
        assertEquals("456 Oak Ave", testAddressDto.getFullAddress());
    }

    @Test
    @DisplayName("Should handle address builder")
    void testAddressBuilder() {
        Address newAddress = Address.builder()
                .addressId(3)
                .fullAddress("789 Elm St")
                .postalCode("99999")
                .city("New City")
                .build();

        assertNotNull(newAddress);
        assertEquals(3, newAddress.getAddressId());
        assertEquals("789 Elm St", newAddress.getFullAddress());
        assertEquals("99999", newAddress.getPostalCode());
        assertEquals("New City", newAddress.getCity());
    }
}
