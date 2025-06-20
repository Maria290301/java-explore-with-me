package ru.practicum.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.adminapi.AdminEventController;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventStatus;
import ru.practicum.event.dto.EventUpdateRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @Test
    void publishEvent_shouldReturnEventDto() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setId(1L);
        eventDto.setStatus(EventStatus.PUBLISHED);

        Mockito.when(eventService.publishEvent(1L)).thenReturn(eventDto);

        mockMvc.perform(patch("/admin/events/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    void rejectEvent_shouldReturnEventDto() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setId(2L);
        eventDto.setStatus(EventStatus.REJECTED);

        Mockito.when(eventService.rejectEvent(2L)).thenReturn(eventDto);

        mockMvc.perform(patch("/admin/events/2/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.state").value("REJECTED"));
    }

    @Test
    void updateEvent_shouldReturnUpdatedEventDto() throws Exception {
        EventUpdateRequest request = new EventUpdateRequest();
        request.setTitle("Updated Title");

        EventDto eventDto = new EventDto();
        eventDto.setId(3L);
        eventDto.setTitle("Updated Title");

        Mockito.when(eventService.updateEventByAdmin(eq(3L), any(EventUpdateRequest.class)))
                .thenReturn(eventDto);

        mockMvc.perform(patch("/admin/events/3/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void getCategories_shouldReturnCategoryList() throws Exception {
        CategoryDto cat1 = new CategoryDto();
        cat1.setId(1L);
        cat1.setName("Category1");

        Mockito.when(categoryService.getAllCategories(0, 10))
                .thenReturn(List.of(cat1));

        mockMvc.perform(get("/admin/events/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Category1"));
    }
}
