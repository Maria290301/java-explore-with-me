package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void shouldRecordHit() throws Exception {
        HitRequestDto dto = new HitRequestDto();
        dto.setApp("main-service");
        dto.setUri("/events");
        dto.setIp("192.168.0.1");
        dto.setTimestamp("2024-01-01 12:00:00");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(statsService).recordRequest(any());
    }

    @Test
    void shouldGetStats() throws Exception {
        StatsResponseDto dto = new StatsResponseDto("main-service", "/events", 3);
        when(statsService.getStats(anyString(), anyString(), any(), eq(false)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/stats")
                        .param("start", "2024-01-01 00:00:00")
                        .param("end", "2024-12-31 23:59:59")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events"))
                .andExpect(jsonPath("$[0].hits").value(3));
    }
}
