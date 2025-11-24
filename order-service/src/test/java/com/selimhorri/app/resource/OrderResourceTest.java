package com.selimhorri.app.resource;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderResourceTest {
    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderResource orderResource;

    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(99.99)
                .build();
    }

    @Test
    void findAllShouldReturnOrders() {
        List<OrderDto> orders = new ArrayList<>();
        orders.add(testOrderDto);
        when(orderService.findAll()).thenReturn(orders);

        ResponseEntity<?> response = orderResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnOrder() {
        when(orderService.findById(any(Integer.class))).thenReturn(testOrderDto);

        ResponseEntity<?> response = orderResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).findById(any(Integer.class));
    }

    @Test
    void saveShouldCreateOrder() {
        when(orderService.save(any(OrderDto.class))).thenReturn(testOrderDto);

        ResponseEntity<?> response = orderResource.save(testOrderDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).save(any(OrderDto.class));
    }

    @Test
    void updateShouldUpdateOrderById() {
        when(orderService.update(any(Integer.class), any(OrderDto.class))).thenReturn(testOrderDto);

        ResponseEntity<?> response = orderResource.update("1", testOrderDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).update(any(Integer.class), any(OrderDto.class));
    }

    @Test
    void updateStatusShouldUpdateOrderStatus() {
        when(orderService.updateStatus(any(Integer.class))).thenReturn(testOrderDto);

        ResponseEntity<?> response = orderResource.updateStatus(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).updateStatus(any(Integer.class));
    }

    @Test
    void deleteByIdShouldDeleteOrder() {
        doNothing().when(orderService).deleteById(any(Integer.class));

        ResponseEntity<?> response = orderResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).deleteById(any(Integer.class));
    }
}
