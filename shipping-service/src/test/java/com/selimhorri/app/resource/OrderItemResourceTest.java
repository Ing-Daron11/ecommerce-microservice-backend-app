package com.selimhorri.app.resource;

import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.service.OrderItemService;
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
class OrderItemResourceTest {
    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderItemResource orderItemResource;

    private OrderItemDto testOrderItemDto;

    @BeforeEach
    void setUp() {
        testOrderItemDto = OrderItemDto.builder()
                .productId(1)
                .orderId(100)
                .orderedQuantity(2)
                .build();
    }

    @Test
    void findAllShouldReturnOrderItems() {
        List<OrderItemDto> orderItems = new ArrayList<>();
        orderItems.add(testOrderItemDto);
        when(orderItemService.findAll()).thenReturn(orderItems);

        ResponseEntity<?> response = orderItemResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderItemService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnOrderItem() {
        when(orderItemService.findById(any(Integer.class))).thenReturn(testOrderItemDto);

        ResponseEntity<?> response = orderItemResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderItemService, times(1)).findById(any(Integer.class));
    }

    @Test
    void saveShouldCreateOrderItem() {
        when(orderItemService.save(any(OrderItemDto.class))).thenReturn(testOrderItemDto);

        ResponseEntity<?> response = orderItemResource.save(testOrderItemDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderItemService, times(1)).save(any(OrderItemDto.class));
    }

    @Test
    void deleteByIdShouldDeleteOrderItem() {
        doNothing().when(orderItemService).deleteById(any(Integer.class));

        ResponseEntity<?> response = orderItemResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderItemService, times(1)).deleteById(any(Integer.class));
    }
}
