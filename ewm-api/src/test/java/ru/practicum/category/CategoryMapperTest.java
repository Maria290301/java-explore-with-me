package ru.practicum.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.category.dto.CategoryDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

    @Test
    void toDto_shouldMapCategoryToDto() {
        Category category = Category.builder()
                .id(1L)
                .name("Music")
                .build();

        CategoryDto dto = CategoryMapper.toDto(category);

        assertEquals(1L, dto.getId());
        assertEquals("Music", dto.getName());
    }

    @Test
    void toEntity_shouldMapDtoToCategory() {
        CategoryDto dto = new CategoryDto(2L, "Art");

        Category entity = CategoryMapper.toEntity(dto);

        assertEquals(2L, entity.getId());
        assertEquals("Art", entity.getName());
    }
}
