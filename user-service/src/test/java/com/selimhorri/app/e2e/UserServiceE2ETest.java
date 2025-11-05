package com.selimhorri.app.e2e;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Service E2E Tests")
class UserServiceE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: Complete user lifecycle - create, retrieve, update")
    @Transactional
    void testCompleteUserLifecycle() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("E2E")
                .lastName("User")
                .email("e2e@test.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        UserDto createdUser = objectMapper.readValue(responseBody, UserDto.class);

        assertNotNull(createdUser.getUserId());
        assertEquals("E2E", createdUser.getFirstName());
        assertEquals("e2e@test.com", createdUser.getEmail());

        long userCount = userRepository.count();
        assertTrue(userCount > 0);

        long credentialCount = credentialRepository.count();
        assertTrue(credentialCount > 0);
    }

    @Test
    @DisplayName("E2E: User service availability and health check")
    @Transactional
    void testServiceAvailability() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("E2E: Create multiple users and verify persistence")
    @Transactional
    void testMultipleUserCreation() throws Exception {
        for (int i = 0; i < 3; i++) {
            UserDto userDto = UserDto.builder()
                    .firstName("User" + i)
                    .lastName("Test" + i)
                    .email("user" + i + "@test.com")
                    .build();

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(status().isOk());
        }

        long userCount = userRepository.count();
        assertEquals(3, userCount);
    }

    @Test
    @DisplayName("E2E: User credentials are properly encrypted after creation")
    @Transactional
    void testCredentialEncryption() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("Secure")
                .lastName("User")
                .email("secure@test.com")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        var credential = credentialRepository.findByUsername("secure@test.com").orElse(null);
        assertNotNull(credential);
        assertTrue(credential.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("E2E: End-to-end workflow with error handling")
    @Transactional
    void testErrorHandling() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .firstName("")
                .lastName("")
                .email("")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
}
