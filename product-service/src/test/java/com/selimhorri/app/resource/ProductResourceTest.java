package com.selimhorri.app.resource;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.service.ProductService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductResourceTest {
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductResource productResource;

    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .priceUnit(99.99)
                .quantity(10)
                .build();
    }

    @Test
    void findAllShouldReturnProducts() {
        List<ProductDto> products = new ArrayList<>();
        products.add(testProductDto);
        when(productService.findAll()).thenReturn(products);

        ResponseEntity<?> response = productResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnProduct() {
        when(productService.findById(1)).thenReturn(testProductDto);

        ResponseEntity<?> response = productResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).findById(1);
    }

    @Test
    void saveShouldCreateProduct() {
        when(productService.save(any(ProductDto.class))).thenReturn(testProductDto);

        ResponseEntity<?> response = productResource.save(testProductDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).save(any(ProductDto.class));
    }

    @Test
    void updateWithoutIdShouldUpdateProduct() {
        when(productService.update(any(ProductDto.class))).thenReturn(testProductDto);

        ResponseEntity<?> response = productResource.update(testProductDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).update(any(ProductDto.class));
    }

    @Test
    void updateWithIdShouldUpdateProductById() {
        when(productService.update(anyInt(), any(ProductDto.class))).thenReturn(testProductDto);

        ResponseEntity<?> response = productResource.update("1", testProductDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).update(1, testProductDto);
    }

    @Test
    void deleteByIdShouldDeleteProduct() {
        doNothing().when(productService).deleteById(1);

        ResponseEntity<?> response = productResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).deleteById(1);
    }
}
