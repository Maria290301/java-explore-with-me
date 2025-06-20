package ru.practicum.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryCreateRequest;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryUpdateRequest;
import ru.practicum.event.Event;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConflictException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldSaveNewCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest("Sports");

        when(categoryRepository.existsByName("Sports")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category cat = inv.getArgument(0);
            cat.setId(1L);
            return cat;
        });

        CategoryDto result = categoryService.createCategory(request);

        assertEquals("Sports", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void createCategory_shouldThrowIfNameExists() {
        when(categoryRepository.existsByName("Duplicate")).thenReturn(true);

        CategoryCreateRequest request = new CategoryCreateRequest("Duplicate");

        assertThrows(AlreadyExistsException.class,
                () -> categoryService.createCategory(request));
    }

    @Test
    void updateCategory_shouldUpdateName() {
        Category category = Category.builder().id(1L).name("Old").build();
        CategoryUpdateRequest request = new CategoryUpdateRequest("New");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("New")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = categoryService.updateCategory(1L, request);

        assertEquals("New", result.getName());
    }

    @Test
    void deleteCategory_shouldThrowIfHasEvents() {
        Category category = Category.builder()
                .id(1L)
                .name("WithEvents")
                .events(Set.of(new Event()))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(ConflictException.class,
                () -> categoryService.deleteCategory(1L));
    }

    @Test
    void getAllCategories_shouldReturnPagedList() {
        Category cat1 = Category.builder().id(1L).name("A").build();
        Category cat2 = Category.builder().id(2L).name("B").build();
        Page<Category> page = new PageImpl<>(List.of(cat1, cat2));

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<CategoryDto> result = categoryService.getAllCategories(0, 10);

        assertEquals(2, result.size());
    }
}
