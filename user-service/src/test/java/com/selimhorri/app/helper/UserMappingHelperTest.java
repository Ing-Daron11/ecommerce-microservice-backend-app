package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;

@DisplayName("UserMappingHelper Unit Tests")
class UserMappingHelperTest {

    private User testUser;
    private Credential testCredential;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("hashedPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testAddress = Address.builder()
                .addressId(1)
                .fullAddress("123 Main St")
                .postalCode("12345")
                .city("Test City")
                .build();

        Set<Address> addresses = new HashSet<>();
        addresses.add(testAddress);

        testUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .imageUrl("https://example.com/image.jpg")
                .credential(testCredential)
                .addresses(addresses)
                .build();

        testCredential.setUser(testUser);
        testAddress.setUser(testUser);
    }

    @Test
    @DisplayName("Should map User entity to UserDto correctly")
    void testMapUserToUserDto() {
        UserDto userDto = UserMappingHelper.map(testUser);

        assertNotNull(userDto);
        assertEquals(testUser.getUserId(), userDto.getUserId());
        assertEquals(testUser.getFirstName(), userDto.getFirstName());
        assertEquals(testUser.getLastName(), userDto.getLastName());
        assertEquals(testUser.getEmail(), userDto.getEmail());
        assertEquals(testUser.getPhone(), userDto.getPhone());
        assertEquals(testUser.getImageUrl(), userDto.getImageUrl());
    }

    @Test
    @DisplayName("Should map credential from User entity to CredentialDto")
    void testMapCredentialInUserDto() {
        UserDto userDto = UserMappingHelper.map(testUser);

        assertNotNull(userDto.getCredentialDto());
        assertEquals(testCredential.getCredentialId(), userDto.getCredentialDto().getCredentialId());
        assertEquals(testCredential.getUsername(), userDto.getCredentialDto().getUsername());
        assertEquals(testCredential.getRoleBasedAuthority(), userDto.getCredentialDto().getRoleBasedAuthority());
        assertTrue(userDto.getCredentialDto().getIsEnabled());
    }

    @Test
    @DisplayName("Should map UserDto to User entity with credential")
    void testMapUserDtoToUser() {
        CredentialDto credentialDto = CredentialDto.builder()
                .username("newuser")
                .password("plainPassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        UserDto userDto = UserDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("9876543210")
                .credentialDto(credentialDto)
                .build();

        User user = UserMappingHelper.map(userDto);

        assertNotNull(user);
        assertEquals(userDto.getFirstName(), user.getFirstName());
        assertEquals(userDto.getLastName(), user.getLastName());
        assertEquals(userDto.getEmail(), user.getEmail());
        assertEquals(userDto.getPhone(), user.getPhone());
        assertNotNull(user.getCredential());
        assertEquals("newuser", user.getCredential().getUsername());
    }

    @Test
    @DisplayName("Should preserve all credential fields during mapping")
    void testMapAllCredentialFields() {
        UserDto userDto = UserMappingHelper.map(testUser);
        CredentialDto credentialDto = userDto.getCredentialDto();

        assertEquals(RoleBasedAuthority.ROLE_USER, credentialDto.getRoleBasedAuthority());
        assertTrue(credentialDto.getIsAccountNonExpired());
        assertTrue(credentialDto.getIsAccountNonLocked());
        assertTrue(credentialDto.getIsCredentialsNonExpired());
    }
}
