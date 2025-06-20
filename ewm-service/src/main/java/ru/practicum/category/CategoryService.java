package ru.practicum.category;

import ru.practicum.category.dto.CategoryCreateRequest;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryCreateRequest request);

    CategoryDto updateCategory(Long id, CategoryUpdateRequest request);

    void deleteCategory(Long id);

    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(Long id);
}
