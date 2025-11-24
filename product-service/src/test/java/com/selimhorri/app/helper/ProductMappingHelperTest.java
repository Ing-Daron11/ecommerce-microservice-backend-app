package com.selimhorri.app.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;

class ProductMappingHelperTest {

    @Test
    void mapToDto() {
        Category category = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .build();

        Product product = Product.builder()
                .productId(1)
                .productTitle("Laptop")
                .priceUnit(1000.0)
                .category(category)
                .build();

        ProductDto productDto = ProductMappingHelper.map(product);

        assertThat(productDto.getProductId()).isEqualTo(1);
        assertThat(productDto.getProductTitle()).isEqualTo("Laptop");
        assertThat(productDto.getCategoryDto().getCategoryId()).isEqualTo(1);
    }

    @Test
    void mapToEntity() {
        CategoryDto categoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Laptop")
                .priceUnit(1000.0)
                .categoryDto(categoryDto)
                .build();

        Product product = ProductMappingHelper.map(productDto);

        assertThat(product.getProductId()).isEqualTo(1);
        assertThat(product.getProductTitle()).isEqualTo("Laptop");
        assertThat(product.getCategory().getCategoryId()).isEqualTo(1);
    }
}
