package ru.practicum.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private ParticipationRequestRepository requestRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final Long userId = 1L;
    private final Long eventId = 10L;

    @Test
    void createRequest_shouldThrowIfUserIsInitiator() {
        User user = new User();
        user.setId(userId);
        Event event = new Event();
        event.setInitiator(user);
        event.setState(EventStatus.PUBLISHED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                requestService.createRequest(userId, eventId));
    }

    @Test
    void createRequest_shouldThrowIfEventNotPublished() {
        Long userId = 1L;
        Long eventId = 2L;

        User user = new User();
        user.setId(userId);

        User initiator = new User();
        initiator.setId(99L);

        Event event = new Event();
        event.setInitiator(initiator);
        event.setState(EventStatus.PENDING);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class, () ->
                requestService.createRequest(userId, eventId));
    }

    @Test
    void createRequest_shouldThrowIfAlreadyRequested() {
        User user = new User();
        user.setId(userId);
        User initiator = new User();
        initiator.setId(99L);
        Event event = new Event();
        event.setInitiator(initiator);
        event.setState(EventStatus.PUBLISHED);

        ParticipationRequest existingRequest = new ParticipationRequest();
        existingRequest.setRequester(user);
        existingRequest.setStatus(RequestStatus.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByEventId(eventId)).thenReturn(List.of(existingRequest));

        assertThrows(ConflictException.class, () ->
                requestService.createRequest(userId, eventId));
    }

    @Test
    void createRequest_shouldThrowIfLimitReached() {
        User user = new User();
        user.setId(userId);
        User initiator = new User();
        initiator.setId(99L);
        Event event = new Event();
        event.setInitiator(initiator);
        event.setState(EventStatus.PUBLISHED);
        event.setParticipantLimit(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByEventId(eventId)).thenReturn(List.of());
        when(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(1);

        assertThrows(ConflictException.class, () ->
                requestService.createRequest(userId, eventId));
    }

    @Test
    void createRequest_shouldCreateAutoConfirmedRequest() {
        User user = new User();
        user.setId(userId);
        User initiator = new User();
        initiator.setId(99L);
        Event event = new Event();
        event.setInitiator(initiator);
        event.setState(EventStatus.PUBLISHED);
        event.setParticipantLimit(0);
        event.setRequestModeration(false);

        ParticipationRequest saved = new ParticipationRequest();
        saved.setId(123L);
        saved.setStatus(RequestStatus.CONFIRMED);
        saved.setRequester(user);
        saved.setEvent(event);
        saved.setCreated(LocalDateTime.now());

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(saved.getId());
        dto.setStatus("CONFIRMED");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findByEventId(eventId)).thenReturn(List.of());
        when(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(0);
        when(requestRepository.save(any())).thenReturn(saved);
        when(requestMapper.toDto(saved)).thenReturn(dto);

        ParticipationRequestDto result = requestService.createRequest(userId, eventId);

        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void cancelRequest_shouldWorkIfValid() {
        User user = new User();
        user.setId(userId);
        ParticipationRequest request = new ParticipationRequest();
        request.setId(123L);
        request.setRequester(user);
        request.setStatus(RequestStatus.PENDING);

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(123L);
        dto.setStatus("CANCELED");

        when(requestRepository.findById(123L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenReturn(request);
        when(requestMapper.toDto(request)).thenReturn(dto);

        ParticipationRequestDto result = requestService.cancelRequest(userId, 123L);

        assertEquals("CANCELED", result.getStatus());
        verify(requestRepository).save(request);
    }

    @Test
    void cancelRequest_shouldThrowIfRequesterMismatch() {
        User user = new User();
        user.setId(2L);
        ParticipationRequest request = new ParticipationRequest();
        request.setId(123L);
        request.setRequester(user);

        when(requestRepository.findById(123L)).thenReturn(Optional.of(request));

        assertThrows(ConflictException.class, () -> requestService.cancelRequest(99L, 123L));
    }

    @Test
    void confirmRequest_shouldWorkIfValid() {
        Event event = new Event();
        event.setId(eventId);
        User initiator = new User();
        initiator.setId(userId);
        event.setInitiator(initiator);

        ParticipationRequest req = new ParticipationRequest();
        req.setId(111L);
        req.setEvent(event);

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(111L);
        dto.setStatus("CONFIRMED");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findById(111L)).thenReturn(Optional.of(req));
        when(requestRepository.save(req)).thenReturn(req);
        when(requestMapper.toDto(req)).thenReturn(dto);

        ParticipationRequestDto result = requestService.confirmRequest(userId, eventId, 111L);

        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void rejectRequest_shouldWorkIfValid() {
        Event event = new Event();
        event.setId(eventId);
        User initiator = new User();
        initiator.setId(userId);
        event.setInitiator(initiator);

        ParticipationRequest req = new ParticipationRequest();
        req.setId(222L);
        req.setEvent(event);

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(222L);
        dto.setStatus("REJECTED");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findById(222L)).thenReturn(Optional.of(req));
        when(requestRepository.save(req)).thenReturn(req);
        when(requestMapper.toDto(req)).thenReturn(dto);

        ParticipationRequestDto result = requestService.rejectRequest(userId, eventId, 222L);

        assertEquals("REJECTED", result.getStatus());
    }
}
