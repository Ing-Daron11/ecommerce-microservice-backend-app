package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.domain.enums.OrderStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.exception.wrapper.PaymentServiceException;
import com.selimhorri.app.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setPaymentId(1);
        payment.setPaymentStatus(PaymentStatus.NOT_STARTED);
        payment.setOrderId(100);

        orderDto = new OrderDto();
        orderDto.setOrderId(100);
        orderDto.setOrderStatus(OrderStatus.ORDERED.name());

        paymentDto = new PaymentDto();
        paymentDto.setPaymentId(1);
        paymentDto.setPaymentStatus(PaymentStatus.NOT_STARTED);
        paymentDto.setOrderDto(orderDto);
    }

    @Test
    void shouldFindAll() {
        orderDto.setOrderStatus("IN_PAYMENT");
        given(paymentRepository.findAll()).willReturn(List.of(payment));
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        List<PaymentDto> result = paymentService.findAll();

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
    }

    @Test
    void shouldFindById() {
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        PaymentDto result = paymentService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1);
        verify(paymentRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFoundById() {
        given(paymentRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findById(1))
                .isInstanceOf(PaymentServiceException.class)
                .hasMessageContaining("Payment with id: 1 not found");
    }

    @Test
    void shouldSavePayment() {
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);

        PaymentDto result = paymentService.save(paymentDto);

        assertThat(result).isNotNull();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(restTemplate, times(1)).patchForObject(anyString(), any(), eq(Void.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingPaymentForNonOrderedOrder() {
        orderDto.setOrderStatus("CREATED");
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        assertThatThrownBy(() -> paymentService.save(paymentDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot start the payment of an order that is not ordered");
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldUpdateStatusFromNotStartedToInProgress() {
        payment.setPaymentStatus(PaymentStatus.NOT_STARTED);
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        PaymentDto result = paymentService.updateStatus(1);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void shouldUpdateStatusFromInProgressToCompleted() {
        payment.setPaymentStatus(PaymentStatus.IN_PROGRESS);
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        PaymentDto result = paymentService.updateStatus(1);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusFromCompleted() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.updateStatus(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is already COMPLETED");
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldDeleteById() {
        payment.setPaymentStatus(PaymentStatus.NOT_STARTED);
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));

        paymentService.deleteById(1);

        verify(paymentRepository, times(1)).save(payment);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void shouldThrowExceptionWhenDeletingCompletedPayment() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        given(paymentRepository.findById(1)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.deleteById(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot cancel a completed payment");
        
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusForNonExistentPayment() {
        given(paymentRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updateStatus(1))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment with id: 1 not found");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundDuringSave() {
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> paymentService.save(paymentDto))
                .isInstanceOf(PaymentServiceException.class)
                .hasMessageContaining("Order with ID 100 not found");
        
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
