package ru.practicum.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.publicapi.PublicCategoryController;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCategoryController.class)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnPagedCategories() throws Exception {
        List<CategoryDto> categories = List.of(
                new CategoryDto(1L, "Music"),
                new CategoryDto(2L, "Sports")
        );

        when(categoryService.getAllCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Music"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Sports"));
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        CategoryDto categoryDto = new CategoryDto(5L, "Science");

        when(categoryService.getCategoryById(5L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("Science"));
    }

    @Test
    void getCategoryById_shouldReturnNotFound_whenMissing() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with id = 999"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}
