package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.category.dto.CategoryCreateRequest;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryUpdateRequest;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Category name already exists.");
        }

        Category category = Category.builder()
                .name(request.getName())
                .build();

        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Category name already exists.");
        }

        category.setName(request.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));

        if (!category.getEvents().isEmpty()) {
            throw new ConflictException("Cannot delete category with linked events.");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id = " + id));
        return CategoryMapper.toDto(category);
    }
}
