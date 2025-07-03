package ru.practicum;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StatsControllerTest {

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    private MockMvc mockMvc;

    public StatsControllerTest() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(statsController).build();
    }

    @Test
    void hit_ShouldReturnCreated() throws Exception {
        String json = "{\"app\":\"app\",\"uri\":\"/uri\",\"ip\":\"127.0.0.1\",\"timestamp\":\"2025-07-02 10:00:00\"}";

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(statsService, times(1)).saveHit(any());
    }

    @Test
    void getStats_ShouldReturnOk() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(statsService.getViewStatsList(any())).thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", start.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .param("end", end.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .param("unique", "false"))
                .andExpect(status().isOk());

        verify(statsService, times(1)).getViewStatsList(any());
    }
}
