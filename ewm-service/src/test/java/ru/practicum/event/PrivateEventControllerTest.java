package ru.practicum.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventCreateRequest;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventUpdateRequest;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.privateapi.PrivateEventController;
import ru.practicum.request.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PrivateEventController.class)
class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updateEvent_shouldReturnUpdatedEventDto() throws Exception {
        Long userId = 1L;
        Long eventId = 2L;

        EventUpdateRequest request = new EventUpdateRequest();
        request.setTitle("New Title");

        EventDto eventDto = new EventDto();
        eventDto.setId(eventId);
        eventDto.setTitle("New Title");

        Mockito.when(eventService.updateEvent(eq(userId), eq(eventId), any(EventUpdateRequest.class)))
                .thenReturn(eventDto);

        mockMvc.perform(patch("/users/1/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void createEvent_shouldReturnCreatedEventDto() throws Exception {
        EventCreateRequest request = new EventCreateRequest();
        request.setTitle("Created Event");
        request.setAnnotation("This is a sufficiently long annotation text.");
        request.setDescription("This is a sufficiently long description text.");
        request.setCategory(2L);
        request.setLocation(new LocationDto(55.7558, 37.6176));
        request.setEventDate(LocalDateTime.of(2025, 7, 1, 12, 0));

        EventDto mockResponse = new EventDto();
        mockResponse.setId(5L);
        mockResponse.setTitle("Created Event");
        mockResponse.setAnnotation(request.getAnnotation());
        mockResponse.setDescription(request.getDescription());
        mockResponse.setEventDate(request.getEventDate());
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(2L);
        categoryDto.setName("Concerts");
        mockResponse.setCategory(categoryDto);
        mockResponse.setLocation(request.getLocation());

        Mockito.when(eventService.createEvent(eq(1L), any(EventCreateRequest.class)))
                .thenReturn(mockResponse);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("Created Event"));
    }

    @Test
    void getUserEvents_shouldReturnEventList() throws Exception {
        Long userId = 1L;
        EventDto eventDto = new EventDto();
        eventDto.setId(10L);
        eventDto.setTitle("User Event");

        Mockito.when(eventService.getEventsByUser(userId, 0, 10))
                .thenReturn(List.of(eventDto));

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].title").value("User Event"));
    }
}
