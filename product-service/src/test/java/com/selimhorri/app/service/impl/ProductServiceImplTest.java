package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryTitle("Electronics");

        product = new Product();
        product.setProductId(1);
        product.setProductTitle("Laptop");
        product.setPriceUnit(1000.0);
        product.setCategory(category);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryId(1);
        categoryDto.setCategoryTitle("Electronics");

        productDto = new ProductDto();
        productDto.setProductId(1);
        productDto.setProductTitle("Laptop");
        productDto.setPriceUnit(1000.0);
        productDto.setCategoryDto(categoryDto);
    }

    @Test
    void shouldFindAll() {
        given(productRepository.findAll()).willReturn(List.of(product));

        List<ProductDto> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductTitle()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldFindById() {
        given(productRepository.findById(1)).willReturn(Optional.of(product));

        ProductDto result = productService.findById(1);

        assertThat(result.getProductTitle()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundById() {
        given(productRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product with id: 1 not found");
    }

    @Test
    void shouldSaveProduct() {
        given(productRepository.save(any(Product.class))).willReturn(product);

        ProductDto result = productService.save(productDto);

        assertThat(result.getProductTitle()).isEqualTo("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() {
        given(productRepository.save(any(Product.class))).willReturn(product);

        ProductDto result = productService.update(productDto);

        assertThat(result.getProductTitle()).isEqualTo("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldUpdateProductById() {
        given(productRepository.findById(1)).willReturn(Optional.of(product));
        given(productRepository.save(any(Product.class))).willReturn(product);

        ProductDto result = productService.update(1, productDto);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldDeleteById() {
        given(productRepository.findById(1)).willReturn(Optional.of(product));

        productService.deleteById(1);

        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).delete(product);
    }
}
