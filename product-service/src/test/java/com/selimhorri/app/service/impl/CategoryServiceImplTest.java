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
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1);
        category.setCategoryTitle("Electronics");

        categoryDto = new CategoryDto();
        categoryDto.setCategoryId(1);
        categoryDto.setCategoryTitle("Electronics");
    }

    @Test
    void shouldFindAll() {
        given(categoryRepository.findAll()).willReturn(List.of(category));

        List<CategoryDto> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void shouldFindById() {
        given(categoryRepository.findById(1)).willReturn(Optional.of(category));

        CategoryDto result = categoryService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isEqualTo(1);
        verify(categoryRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundById() {
        given(categoryRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(1))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category with id: 1 not found");
    }

    @Test
    void shouldSaveCategory() {
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        CategoryDto result = categoryService.save(categoryDto);

        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldUpdateCategory() {
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        CategoryDto result = categoryService.update(categoryDto);

        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldUpdateCategoryById() {
        given(categoryRepository.findById(1)).willReturn(Optional.of(category));
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        CategoryDto result = categoryService.update(1, categoryDto);

        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).findById(1);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldDeleteById() {
        categoryService.deleteById(1);
        verify(categoryRepository, times(1)).deleteById(1);
    }
}
