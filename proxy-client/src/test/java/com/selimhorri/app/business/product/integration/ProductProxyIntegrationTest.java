package com.selimhorri.app.business.product.integration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.business.product.model.ProductDto;
import com.selimhorri.app.business.product.model.response.ProductProductServiceCollectionDtoResponse;
import com.selimhorri.app.business.product.service.ProductClientService;

@Tag("integration")
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ProductProxyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductClientService productClientService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Mock data
        ProductDto product1 = ProductDto.builder()
                .productId(1)
                .productTitle("Product 1")
                .priceUnit(99.99)
                .build();

        ProductDto product2 = ProductDto.builder()
                .productId(2)
                .productTitle("Product 2")
                .priceUnit(149.99)
                .build();

        List<ProductDto> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        ProductProductServiceCollectionDtoResponse response = ProductProductServiceCollectionDtoResponse.builder()
                .collection(products)
                .build();

        // Mock service call
        when(productClientService.findAll()).thenReturn(ResponseEntity.ok(response));

        // Perform request and verify
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection.length()").value(2))
                .andExpect(jsonPath("$.collection[0].productId").value(1))
                .andExpect(jsonPath("$.collection[0].productTitle").value("Product 1"))
                .andExpect(jsonPath("$.collection[1].productId").value(2));

        verify(productClientService, times(1)).findAll();
    }

    @Test
    void shouldGetProductById() throws Exception {
        // Mock data
        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .priceUnit(99.99)
                .build();

        // Mock service call
        when(productClientService.findById(anyString())).thenReturn(ResponseEntity.ok(productDto));

        // Perform request and verify
        mockMvc.perform(get("/api/products/{productId}", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productTitle").value("Test Product"))
                .andExpect(jsonPath("$.priceUnit").value(99.99));

        verify(productClientService, times(1)).findById("1");
    }

    @Test
    void shouldSaveProduct() throws Exception {
        // Mock data
        ProductDto inputDto = ProductDto.builder()
                .productTitle("New Product")
                .priceUnit(199.99)
                .build();

        ProductDto savedDto = ProductDto.builder()
                .productId(3)
                .productTitle("New Product")
                .priceUnit(199.99)
                .build();

        // Mock service call
        when(productClientService.save(any(ProductDto.class))).thenReturn(ResponseEntity.ok(savedDto));

        // Perform request and verify
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());

        verify(productClientService, never()).save(any(ProductDto.class));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        // Mock data
        ProductDto inputDto = ProductDto.builder()
                .productId(1)
                .productTitle("Updated Product")
                .priceUnit(129.99)
                .build();

        // Mock service call
        when(productClientService.update(anyString(), any(ProductDto.class)))
                .thenReturn(ResponseEntity.ok(inputDto));

        // Perform request and verify
        mockMvc.perform(put("/api/products/{productId}", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());

        verify(productClientService, never()).update(anyString(), any(ProductDto.class));
    }

    @Test
    void shouldDeleteProductReturnForbidden() throws Exception {
        // Mock service call
        when(productClientService.deleteById(anyString())).thenReturn(ResponseEntity.ok(true));

        // Perform request and verify (read-only mode returns 403)
        mockMvc.perform(delete("/api/products/{productId}", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(productClientService, never()).deleteById(anyString());
    }

    @Test
    void shouldGetProductsWithSortParameter() throws Exception {
        // Mock data
        ProductDto product1 = ProductDto.builder()
                .productId(1)
                .productTitle("Product 1")
                .priceUnit(50.00)
                .build();

        List<ProductDto> products = new ArrayList<>();
        products.add(product1);

        ProductProductServiceCollectionDtoResponse response = ProductProductServiceCollectionDtoResponse.builder()
                .collection(products)
                .build();

        // Mock service call
        when(productClientService.findAll()).thenReturn(ResponseEntity.ok(response));

        // Perform request and verify
        mockMvc.perform(get("/api/products?sort=price")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").isArray());

        verify(productClientService, times(1)).findAll();
    }
}
