package ru.practicum.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventFullDtoForAdmin;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // для LocalDateTime
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void adminPrivatePublicControllers_ShouldRespondCorrectly() throws Exception {
        Long eventId = 1L;
        Long userId = 10L;

        // ----- Setup: mock EventService -----
        EventFullDto fullDto = EventFullDto.builder()
                .id(eventId)
                .title("Test Event")
                .annotation("Test annotation")
                .build();

        EventFullDtoForAdmin fullDtoForAdmin = EventFullDtoForAdmin.builder()
                .id(eventId)
                .title("Admin Event")
                .annotation("Admin annotation")
                .build();

        EventShortDto shortDto = EventShortDto.builder()
                .id(eventId)
                .title("Short Event")
                .annotation("Short annotation")
                .build();

        when(eventService.getAllEventFromAdmin(any())).thenReturn(List.of(fullDtoForAdmin));
        when(eventService.getEventById(eq(eventId), any())).thenReturn(fullDto);
        when(eventService.getEventsByUserId(eq(userId), anyInt(), anyInt())).thenReturn(List.of(shortDto));
        when(eventService.getAllEventFromPublic(any(), any())).thenReturn(List.of(shortDto));
        when(eventService.getEventByUserIdAndEventId(eq(userId), eq(eventId))).thenReturn(fullDto);

        // ----- Test AdminEventController -----
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].title").value("Admin Event"));

        // ----- Test PrivateEventController -----
        mockMvc.perform(get("/users/{userId}/events", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].title").value("Short Event"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Test Event"));

        // ----- Test PublicEventController -----
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId));

        mockMvc.perform(get("/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));
    }
}
