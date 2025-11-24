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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.OrderStatus;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem orderItem;
    private OrderItemDto orderItemDto;
    private ProductDto productDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderItem = new OrderItem();
        orderItem.setOrderId(1);
        orderItem.setProductId(100);
        orderItem.setOrderedQuantity(2);
        orderItem.setActive(true);

        orderItemDto = new OrderItemDto();
        orderItemDto.setOrderId(1);
        orderItemDto.setProductId(100);
        orderItemDto.setOrderedQuantity(2);

        productDto = new ProductDto();
        productDto.setProductId(100);
        productDto.setQuantity(10);

        orderDto = new OrderDto();
        orderDto.setOrderId(1);
        orderDto.setOrderStatus(OrderStatus.ORDERED.name());
        
        // Set nested DTOs for findAll/findById mapping
        orderItemDto.setProductDto(productDto);
        orderItemDto.setOrderDto(orderDto);
    }

    @Test
    void shouldFindAll() {
        given(orderItemRepository.findByIsActiveTrue()).willReturn(Collections.singletonList(orderItem));
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        List<OrderItemDto> result = orderItemService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1);
        verify(orderItemRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void shouldFindById() {
        given(orderItemRepository.findById(1)).willReturn(Optional.of(orderItem));
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        OrderItemDto result = orderItemService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1);
        verify(orderItemRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowExceptionWhenOrderItemNotFoundById() {
        given(orderItemRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.findById(1))
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessageContaining("Active OrderItem with id: 1 not found");
    }

    @Test
    void shouldSaveOrderItem() {
        orderDto.setOrderStatus(OrderStatus.CREATED.name());
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);
        given(orderItemRepository.save(any(OrderItem.class))).willReturn(orderItem);

        OrderItemDto result = orderItemService.save(orderItemDto);

        assertThat(result).isNotNull();
        verify(restTemplate, times(2)).getForObject(anyString(), any());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(restTemplate, times(1)).patchForObject(anyString(), any(), eq(Void.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInvalidOrderStatus() {
        orderDto.setOrderStatus(OrderStatus.ORDERED.name());
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        assertThatThrownBy(() -> orderItemService.save(orderItemDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create a shipping for an order that is in any state other than CREATED");
        
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithInsufficientProductQuantity() {
        orderDto.setOrderStatus(OrderStatus.CREATED.name());
        productDto.setQuantity(1); // Less than ordered quantity (2)
        
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);

        assertThatThrownBy(() -> orderItemService.save(orderItemDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You cannot order more units than there is available");
        
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    void shouldDeleteById() {
        given(orderItemRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(orderItem));
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        orderItemService.deleteById(1);

        verify(orderItemRepository, times(1)).save(orderItem);
        assertThat(orderItem.isActive()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithInvalidOrderStatus() {
        orderDto.setOrderStatus(OrderStatus.CREATED.name());
        given(orderItemRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.of(orderItem));
        given(restTemplate.getForObject(anyString(), eq(OrderDto.class))).willReturn(orderDto);

        assertThatThrownBy(() -> orderItemService.deleteById(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete order item - associated order is not in ORDERED status");
        
        verify(orderItemRepository, never()).save(orderItem);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentOrderItem() {
        given(orderItemRepository.findByOrderIdAndIsActiveTrue(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.deleteById(1))
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessageContaining("OrderItem with id: 1 not found");
    }
}