package ru.practicum.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.privateapi.PrivateRequestController;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PrivateRequestController.class)
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ParticipationRequestDto dto;

    @BeforeEach
    void setup() {
        dto = ParticipationRequestDto.builder()
                .id(1L)
                .event(2L)
                .requester(3L)
                .status(RequestStatus.CONFIRMED)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        objectMapper.findAndRegisterModules(); // Для LocalDateTime
    }

    @Test
    void addRequest_shouldReturn201AndDto() throws Exception {
        when(requestService.addNewRequest(3L, 2L)).thenReturn(dto);

        mockMvc.perform(post("/users/3/requests")
                        .param("eventId", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.event").value(dto.getEvent()))
                .andExpect(jsonPath("$.requester").value(dto.getRequester()))
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        when(requestService.getRequestsByUserId(3L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/3/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    void cancelRequest_shouldReturnUpdatedDto() throws Exception {
        dto.setStatus(RequestStatus.CANCELED);
        when(requestService.cancelRequest(3L, 1L)).thenReturn(dto);

        mockMvc.perform(patch("/users/3/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}
