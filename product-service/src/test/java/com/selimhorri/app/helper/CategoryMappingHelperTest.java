package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;

@DisplayName("CategoryMappingHelper Unit Tests")
class CategoryMappingHelperTest {

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .build();
    }

    @Test
    @DisplayName("Should map Category to CategoryDto")
    void testCategoryToDto() {
        CategoryDto dto = CategoryMappingHelper.map(testCategory);

        assertNotNull(dto);
        assertEquals(1, dto.getCategoryId());
        assertEquals("Electronics", dto.getCategoryTitle());
    }

    @Test
    @DisplayName("Should map CategoryDto to Category")
    void testDtoToCategory() {
        CategoryDto dto = CategoryDto.builder()
                .categoryId(2)
                .categoryTitle("Furniture")
                .build();

        Category category = CategoryMappingHelper.map(dto);

        assertNotNull(category);
        assertEquals(2, category.getCategoryId());
        assertEquals("Furniture", category.getCategoryTitle());
    }

    @Test
    @DisplayName("Should throw NullPointerException when mapping null Category")
    void testNullCategory() {
        assertThrows(NullPointerException.class, () -> {
            CategoryMappingHelper.map((Category) null);
        });
    }

    @Test
    @DisplayName("Should throw NullPointerException when mapping null CategoryDto")
    void testNullCategoryDto() {
        assertThrows(NullPointerException.class, () -> {
            CategoryMappingHelper.map((CategoryDto) null);
        });
    }
}
