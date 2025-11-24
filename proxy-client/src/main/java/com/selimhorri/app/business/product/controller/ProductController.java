package com.selimhorri.app.business.product.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.business.product.model.ProductDto;
import com.selimhorri.app.business.product.model.response.ProductProductServiceCollectionDtoResponse;
import com.selimhorri.app.business.product.service.ProductClientService;
import com.selimhorri.app.business.product.strategy.ProductSortStrategy;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
	
	private final ProductClientService productClientService;
	private final List<ProductSortStrategy> sortStrategies;

	@Value("${feature.products.readonly:false}")
	private boolean readOnlyMode;
	
	@GetMapping
	public ResponseEntity<ProductProductServiceCollectionDtoResponse> findAll(
			@RequestParam(name = "sort", defaultValue = "default") String sortStrategyName) {
		
		var response = this.productClientService.findAll().getBody();
		
		if (response != null && response.getCollection() != null) {
			List<ProductDto> products = new ArrayList<>(response.getCollection());
			
			ProductSortStrategy strategy = sortStrategies.stream()
					.filter(s -> s.getStrategyName().equalsIgnoreCase(sortStrategyName))
					.findFirst()
					.orElse(sortStrategies.stream()
							.filter(s -> s.getStrategyName().equals("default"))
							.findFirst()
							.orElseThrow());
			
			response.setCollection(strategy.sort(products));
		}
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{productId}")
	public ResponseEntity<ProductDto> findById(@PathVariable("productId") final String productId) {
		return ResponseEntity.ok(this.productClientService.findById(productId).getBody());
	}
	
	@PostMapping
	public ResponseEntity<ProductDto> save(@RequestBody final ProductDto productDto) {
		if (readOnlyMode) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(this.productClientService.save(productDto).getBody());
	}
	
	@PutMapping("/{productId}")
	public ResponseEntity<ProductDto> update(@PathVariable("productId") final String productId, 
			@RequestBody final ProductDto productDto) {
		if (readOnlyMode) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(this.productClientService.update(productId, productDto).getBody());
	}
	
	@DeleteMapping("/{productId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("productId") final String productId) {
		if (readOnlyMode) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(this.productClientService.deleteById(productId).getBody());
	}
	
	
	
}










