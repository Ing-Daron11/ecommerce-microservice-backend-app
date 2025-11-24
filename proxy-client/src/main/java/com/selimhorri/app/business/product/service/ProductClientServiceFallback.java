package com.selimhorri.app.business.product.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.selimhorri.app.business.product.model.ProductDto;
import com.selimhorri.app.business.product.model.response.ProductProductServiceCollectionDtoResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Component
@Slf4j
public class ProductClientServiceFallback implements ProductClientService {

    @Override
    public ResponseEntity<ProductProductServiceCollectionDtoResponse> findAll() {
        log.error("Circuit Breaker Fallback: Product Service is unavailable. Returning empty list.");
        return ResponseEntity.ok(ProductProductServiceCollectionDtoResponse.builder()
                .collection(Collections.emptyList())
                .build());
    }

    @Override
    public ResponseEntity<ProductDto> findById(String productId) {
        log.error("Circuit Breaker Fallback: Product Service is unavailable. Returning empty product.");
        return ResponseEntity.ok(ProductDto.builder()
                .productId(Integer.parseInt(productId))
                .productTitle("Unavailable")
                .build());
    }

    @Override
    public ResponseEntity<ProductDto> save(ProductDto productDto) {
        log.error("Circuit Breaker Fallback: Cannot save product.");
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<ProductDto> update(ProductDto productDto) {
        log.error("Circuit Breaker Fallback: Cannot update product.");
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<ProductDto> update(String productId, ProductDto productDto) {
        log.error("Circuit Breaker Fallback: Cannot update product.");
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<Boolean> deleteById(String productId) {
        log.error("Circuit Breaker Fallback: Cannot delete product.");
        return ResponseEntity.ok(false);
    }
}
