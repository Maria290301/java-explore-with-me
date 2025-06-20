package ru.practicum.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.privateapi.PrivateRequestController;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PrivateRequestController.class)
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        Long userId = 1L;
        Long eventId = 2L;

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(100L);
        dto.setEvent(eventId);
        dto.setRequester(userId);
        dto.setStatus("PENDING");
        dto.setCreated(LocalDateTime.now());

        when(requestService.createRequest(userId, eventId)).thenReturn(dto);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.event").value(dto.getEvent()))
                .andExpect(jsonPath("$.requester").value(dto.getRequester()))
                .andExpect(jsonPath("$.status").value(dto.getStatus()));
    }

    @Test
    void getUserRequests_shouldReturnListOfRequests() throws Exception {
        Long userId = 1L;

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(101L);
        dto.setEvent(2L);
        dto.setRequester(userId);
        dto.setStatus("CONFIRMED");
        dto.setCreated(LocalDateTime.now());

        when(requestService.getUserRequests(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].event").value(dto.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(dto.getRequester()))
                .andExpect(jsonPath("$[0].status").value(dto.getStatus()));
    }

    @Test
    void cancelRequest_shouldReturnCanceledDto() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(requestId);
        dto.setEvent(3L);
        dto.setRequester(userId);
        dto.setStatus("CANCELED");
        dto.setCreated(LocalDateTime.now());

        when(requestService.cancelRequest(userId, requestId)).thenReturn(dto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.requester").value(dto.getRequester()));
    }
}
