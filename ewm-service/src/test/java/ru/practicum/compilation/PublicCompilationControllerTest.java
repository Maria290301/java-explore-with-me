package ru.practicum.compilation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.publicapi.PublicCompilationController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCompilationController.class)
class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Test
    void shouldReturnListOfCompilations() throws Exception {
        CompilationDto dto = new CompilationDto();
        dto.setId(1L);
        dto.setTitle("Popular Events");
        dto.setPinned(true);
        dto.setEvents(List.of());

        Mockito.when(compilationService.getCompilations(true, 0, 10))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Popular Events"))
                .andExpect(jsonPath("$[0].pinned").value(true));
    }

    @Test
    void shouldReturnCompilationById() throws Exception {
        CompilationDto dto = new CompilationDto();
        dto.setId(1L);
        dto.setTitle("Compilation");
        dto.setPinned(false);
        dto.setEvents(List.of());

        Mockito.when(compilationService.getCompilationById(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/compilations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Compilation"));
    }
}
