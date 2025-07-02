package ru.practicum.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Music")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Music")
                .build();

        newCategoryDto = NewCategoryDto.builder()
                .name("Music")
                .build();
    }

    @Test
    void shouldAddNewCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.addNewCategory(newCategoryDto);

        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
    }

    @Test
    void shouldGetCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void shouldDeleteCategoryIfNoEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findByCategory(category)).thenReturn(List.of());

        categoryService.deleteCategoryById(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowConflictExceptionWhenDeletingCategoryWithEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findByCategory(category)).thenReturn(List.of(new Event()));

        assertThrows(ConflictException.class, () -> categoryService.deleteCategoryById(1L));
    }
}

