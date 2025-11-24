package com.selimhorri.app.resource;

import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.service.CategoryService;
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
class CategoryResourceTest {
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryResource categoryResource;

    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Test Category")
                .build();
    }

    @Test
    void findAllShouldReturnCategories() {
        List<CategoryDto> categories = new ArrayList<>();
        categories.add(testCategoryDto);
        when(categoryService.findAll()).thenReturn(categories);

        ResponseEntity<?> response = categoryResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnCategory() {
        when(categoryService.findById(1)).thenReturn(testCategoryDto);

        ResponseEntity<?> response = categoryResource.findById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).findById(1);
    }

    @Test
    void saveShouldCreateCategory() {
        when(categoryService.save(any(CategoryDto.class))).thenReturn(testCategoryDto);

        ResponseEntity<?> response = categoryResource.save(testCategoryDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).save(any(CategoryDto.class));
    }

    @Test
    void updateWithoutIdShouldUpdateCategory() {
        when(categoryService.update(any(CategoryDto.class))).thenReturn(testCategoryDto);

        ResponseEntity<?> response = categoryResource.update(testCategoryDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).update(any(CategoryDto.class));
    }

    @Test
    void updateWithIdShouldUpdateCategoryById() {
        when(categoryService.update(anyInt(), any(CategoryDto.class))).thenReturn(testCategoryDto);

        ResponseEntity<?> response = categoryResource.update("1", testCategoryDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).update(1, testCategoryDto);
    }

    @Test
    void deleteByIdShouldDeleteCategory() {
        doNothing().when(categoryService).deleteById(1);

        ResponseEntity<?> response = categoryResource.deleteById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService, times(1)).deleteById(1);
    }
}
