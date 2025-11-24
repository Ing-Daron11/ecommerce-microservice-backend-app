package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.enums.OrderStatus;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDto orderDto;
    private Cart cart;
    private CartDto cartDto;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setCartId(1);
        
        cartDto = new CartDto();
        cartDto.setCartId(1);

        order = new Order();
        order.setOrderId(1);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setCart(cart);
        order.setActive(true);

        orderDto = new OrderDto();
        orderDto.setOrderId(1);
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setOrderStatus(OrderStatus.CREATED);
        orderDto.setCartDto(cartDto);
    }

    @Test
    void shouldFindAll() {
        given(orderRepository.findAllByIsActiveTrue()).willReturn(List.of(order));

        List<OrderDto> result = orderService.findAll();

        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findAllByIsActiveTrue();
    }

    @Test
    void shouldFindById() {
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));

        OrderDto result = orderService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1);
        verify(orderRepository, times(1)).findByOrderIdAndIsActiveTrue(1);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundById() {
        given(orderRepository.findByOrderIdAndIsActiveTrue(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id: 999 not found");
    }

    @Test
    void shouldSaveOrder() {
        given(cartRepository.findById(1)).willReturn(Optional.of(cart));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        OrderDto result = orderService.save(orderDto);

        assertThat(result).isNotNull();
        verify(cartRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingOrderWithoutCartId() {
        orderDto.getCartDto().setCartId(null);

        assertThatThrownBy(() -> orderService.save(orderDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order must be associated with a cart");
        
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingOrderWithNonExistentCart() {
        given(cartRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.save(orderDto))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Cart not found with ID: 1");
        
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldUpdateStatusFromCreatedToOrdered() {
        order.setStatus(OrderStatus.CREATED);
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderDto result = orderService.updateStatus(1);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldUpdateStatusFromOrderedToInPayment() {
        order.setStatus(OrderStatus.ORDERED);
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        OrderDto result = orderService.updateStatus(1);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.IN_PAYMENT);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusFromInPayment() {
        order.setStatus(OrderStatus.IN_PAYMENT);
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is already PAID and cannot be updated further");
        
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldUpdateOrder() {
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        OrderDto result = orderService.update(1, orderDto);

        assertThat(result).isNotNull();
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldDeleteById() {
        order.setStatus(OrderStatus.CREATED);
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));

        orderService.deleteById(1);

        verify(orderRepository, times(1)).save(order);
        assertThat(order.isActive()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenDeletingPaidOrder() {
        order.setStatus(OrderStatus.IN_PAYMENT);
        given(orderRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.deleteById(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("because it's already PAID");
        
        verify(orderRepository, never()).save(order);
    }
}
