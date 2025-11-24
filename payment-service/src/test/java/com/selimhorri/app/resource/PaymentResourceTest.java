package com.selimhorri.app.resource;

import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.service.PaymentService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResourceTest {
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentResource paymentResource;

    private PaymentDto testPaymentDto;

    @BeforeEach
    void setUp() {
        testPaymentDto = PaymentDto.builder()
                .paymentId(1)
                .isPayed(false)
                .build();
    }

    @Test
    void findAllShouldReturnPayments() {
        List<PaymentDto> payments = new ArrayList<>();
        payments.add(testPaymentDto);
        when(paymentService.findAll()).thenReturn(payments);

        ResponseEntity<?> response = paymentResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnPayment() {
        when(paymentService.findById(any(Integer.class))).thenReturn(testPaymentDto);

        ResponseEntity<?> response = paymentResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).findById(any(Integer.class));
    }

    @Test
    void saveShouldCreatePayment() {
        when(paymentService.save(any(PaymentDto.class))).thenReturn(testPaymentDto);

        ResponseEntity<?> response = paymentResource.save(testPaymentDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).save(any(PaymentDto.class));
    }

    @Test
    void updateStatusShouldUpdatePaymentStatus() {
        when(paymentService.updateStatus(any(Integer.class))).thenReturn(testPaymentDto);

        ResponseEntity<?> response = paymentResource.updateStatus("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).updateStatus(any(Integer.class));
    }

    @Test
    void deleteByIdShouldDeletePayment() {
        doNothing().when(paymentService).deleteById(any(Integer.class));

        ResponseEntity<?> response = paymentResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).deleteById(any(Integer.class));
    }
}
