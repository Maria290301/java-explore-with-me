package ru.practicum.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.adminapi.AdminCompilationController;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCompilationController.class)
class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateCompilation() throws Exception {
        NewCompilationDto request = new NewCompilationDto();
        request.setTitle("New Compilation");
        request.setPinned(true);
        request.setEvents(List.of());

        CompilationDto response = new CompilationDto();
        response.setId(1L);
        response.setTitle("New Compilation");
        response.setPinned(true);
        response.setEvents(List.of());

        Mockito.when(compilationService.createCompilation(any()))
                .thenReturn(response);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Compilation"));
    }

    @Test
    void shouldUpdateCompilation() throws Exception {
        UpdateCompilationDto request = new UpdateCompilationDto();
        request.setTitle("Updated Compilation");
        request.setPinned(false);

        CompilationDto response = new CompilationDto();
        response.setId(1L);
        response.setTitle("Updated Compilation");
        response.setPinned(false);
        response.setEvents(List.of());

        Mockito.when(compilationService.updateCompilation(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Compilation"));
    }

    @Test
    void shouldDeleteCompilation() throws Exception {
        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(compilationService, times(1)).deleteCompilation(1L);
    }
}
