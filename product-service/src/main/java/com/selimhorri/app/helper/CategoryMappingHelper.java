package com.selimhorri.app.helper;

import java.util.Optional;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;

public interface CategoryMappingHelper {
	
	public static CategoryDto map(final Category category) {
		
		final var parentCategory = Optional.ofNullable(category
				.getParentCategory()).orElseGet(() -> new Category());
		
		return CategoryDto.builder()
				.categoryId(category.getCategoryId())
				.categoryTitle(category.getCategoryTitle())
				.imageUrl(category.getImageUrl())
				.parentCategoryDto(
						CategoryDto.builder()
							.categoryId(parentCategory.getCategoryId())
							.categoryTitle(parentCategory.getCategoryTitle())
							.imageUrl(parentCategory.getImageUrl())
							.build())
				.build();
	}
	
	public static Category map(final CategoryDto categoryDto) {
		
		final var parentCategoryDto = categoryDto.getParentCategoryDto();
		
		return Category.builder()
				.categoryId(categoryDto.getCategoryId())
				.categoryTitle(categoryDto.getCategoryTitle())
				.imageUrl(categoryDto.getImageUrl())
				.parentCategory(
						(parentCategoryDto == null || parentCategoryDto.getCategoryId() == null) ? null : Category.builder()
							.categoryId(parentCategoryDto.getCategoryId())
							.categoryTitle(parentCategoryDto.getCategoryTitle())
							.imageUrl(parentCategoryDto.getImageUrl())
							.build())
				.build();
	}
	
	
	
}










