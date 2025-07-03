package ru.practicum.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventStatus;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.event.Event;
import ru.practicum.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private Event event;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        User initiator = new User();
        initiator.setId(99L);

        event = new Event();
        event.setId(2L);
        event.setInitiator(initiator);
        event.setEventStatus(EventStatus.PUBLISHED);
        event.setParticipantLimit(10);
    }

    @Test
    void addNewRequest_shouldReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(2L, RequestStatus.CONFIRMED)).thenReturn(0);
        when(requestRepository.existsByEventIdAndRequesterId(2L, 1L)).thenReturn(false);

        when(requestRepository.save(any())).thenAnswer(invocation -> {
            Request req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        ParticipationRequestDto dto = requestService.addNewRequest(1L, 2L);

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getRequester());
    }

    @Test
    void getRequestsByUserId_shouldReturnList() {
        Request request = new Request();
        request.setId(1L);
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());
        request.setStatus(RequestStatus.CONFIRMED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getRequestsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void cancelRequest_shouldUpdateStatus() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.CONFIRMED);
        request.setRequester(user);
        request.setEvent(event);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findByIdAndRequesterId(1L, 1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenReturn(request);

        ParticipationRequestDto dto = requestService.cancelRequest(1L, 1L);

        assertEquals(RequestStatus.CANCELED, dto.getStatus());
    }
}
