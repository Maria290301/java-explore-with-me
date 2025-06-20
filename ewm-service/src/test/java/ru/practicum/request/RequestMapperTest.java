package ru.practicum.request;

import org.junit.jupiter.api.Test;
import ru.practicum.event.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestMapperTest {

    private final RequestMapper mapper = new RequestMapper();

    @Test
    void toDto_shouldMapCorrectly() {
        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        ParticipationRequest request = new ParticipationRequest();
        request.setId(3L);
        request.setStatus(RequestStatus.PENDING);
        request.setCreated(LocalDateTime.of(2023, 1, 1, 12, 0));
        request.setRequester(user);
        request.setEvent(event);

        ParticipationRequestDto dto = mapper.toDto(request);

        assertEquals(3L, dto.getId());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(1L, dto.getRequester());
        assertEquals(2L, dto.getEvent());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), dto.getCreated());
    }
}
