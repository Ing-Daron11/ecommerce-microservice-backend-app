package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentDto testPayment;
    private OrderDto testOrder;

    @BeforeEach
    void setUp() {
        testOrder = OrderDto.builder()
                .orderId(1)
                .orderDate(java.time.LocalDateTime.now())
                .orderDesc("Integration Test Order")
                .orderFee(150.0)
                .build();

        testPayment = PaymentDto.builder()
                .paymentId(1)
                .isPayed(false)
                .orderDto(testOrder)
                .build();
    }

    @Test
    void testGetAllPayments() throws Exception {
        mockMvc.perform(get("/api/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPaymentById() throws Exception {
        mockMvc.perform(get("/api/payments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPaymentListStructure() throws Exception {
        mockMvc.perform(get("/api/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray());
    }

    @Test
    void testDeletePayment() throws Exception {
        mockMvc.perform(delete("/api/payments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testPaymentEndpointAccessibility() throws Exception {
        mockMvc.perform(get("/api/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
