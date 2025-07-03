package ru.practicum.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.adminapi.AdminCompilationController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.publicapi.PublicCompilationController;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminCompilationController.class, PublicCompilationController.class})
class CompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;
    private UpdateCompilationDto updateCompilationDto;

    @BeforeEach
    void setUp() {
        compilationDto = CompilationDto.builder()
                .id(1L)
                .pinned(true)
                .title("Summer Hits")
                .events(Set.of())
                .build();

        newCompilationDto = NewCompilationDto.builder()
                .pinned(true)
                .title("Summer Hits")
                .events(Set.of(1L, 2L))
                .build();

        updateCompilationDto = UpdateCompilationDto.builder()
                .id(1L)
                .pinned(false)
                .title("Winter Hits")
                .events(Set.of(3L))
                .build();
    }

    // Admin controller tests

    @Test
    void shouldCreateCompilation() throws Exception {
        when(compilationService.addCompilation(any(NewCompilationDto.class))).thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }

    @Test
    void shouldUpdateCompilation() throws Exception {
        when(compilationService.updateCompilation(eq(1L), any(UpdateCompilationDto.class))).thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }

    @Test
    void shouldDeleteCompilation() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());

        verify(compilationService).deleteCompilation(1L);
    }

    // Public controller tests

    @Test
    void shouldGetCompilations() throws Exception {
        when(compilationService.getCompilations(null, 0, 10))
                .thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(compilationDto.getId()))
                .andExpect(jsonPath("$[0].title").value(compilationDto.getTitle()))
                .andExpect(jsonPath("$[0].pinned").value(compilationDto.getPinned()));
    }

    @Test
    void shouldGetCompilationsWithPinnedFilter() throws Exception {
        when(compilationService.getCompilations(true, 0, 10))
                .thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldFindCompilationById() throws Exception {
        when(compilationService.findByIdCompilation(1L)).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }
}
