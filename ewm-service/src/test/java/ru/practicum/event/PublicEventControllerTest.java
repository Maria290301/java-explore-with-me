package ru.practicum.event;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.publicapi.PublicEventController;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void getPublicEvents_shouldReturnList() throws Exception {
        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        dto.setTitle("Public Event");
        dto.setAnnotation("Annotation");
        dto.setPaid(true);

        Mockito.when(eventService.getPublicEvents(
                anyString(),
                ArgumentMatchers.<List>nullable(List.class),
                ArgumentMatchers.<Boolean>nullable(Boolean.class),
                ArgumentMatchers.<LocalDateTime>nullable(LocalDateTime.class),
                ArgumentMatchers.<LocalDateTime>nullable(LocalDateTime.class),
                anyBoolean(),
                ArgumentMatchers.<String>nullable(String.class),
                anyInt(),
                anyInt()
        )).thenReturn(List.of(dto));

        mockMvc.perform(get("/events")
                        .param("text", "public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Public Event"));
    }

    @Test
    void getPublicEventById_shouldReturnEventDto() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setId(2L);
        eventDto.setTitle("Public Detail Event");

        Mockito.when(eventService.getPublicEventById(eq(2L), anyString()))
                .thenReturn(eventDto);

        mockMvc.perform(get("/events/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.title").value("Public Detail Event"));
    }
}
