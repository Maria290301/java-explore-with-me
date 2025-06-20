package ru.practicum.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.server.ResponseStatusException;
import ru.practicum.event.dto.*;

import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ParticipationRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;


import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    private EventRepository eventRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private EventMapper mapper;
    @Mock
    private ViewTracker viewTracker;
    @Mock
    private ParticipationRequestRepository partRepo;

    @InjectMocks
    private EventServiceImpl service;

    private User user;
    private Event event;
    private EventDto dto;
    private EventCreateRequest createReq;

    @BeforeEach
    void beforeAll() {
        user = new User();
        user.setId(1L);

        event = new Event();
        event.setId(10L);
        event.setInitiator(user);
        event.setState(EventStatus.PENDING);
        event.setEventDate(LocalDateTime.now().plusDays(1));

        dto = new EventDto();
        dto.setId(10L);
        dto.setInitiator(1L);

        createReq = new EventCreateRequest();
        createReq.setAnnotation("abcdefghijklmnopqrstuvwxyz");
        createReq.setDescription("This is a valid description with enough length");
        createReq.setEventDate(LocalDateTime.now().plusDays(2));
        createReq.setCategory(5L);
        createReq.setLocation(new LocationDto(10.0, 20.0));
        createReq.setPaid(true);
        createReq.setParticipantLimit(100);
        createReq.setRequestModeration(false);
        createReq.setTitle("Good Title");
    }

    @Test
    void createEvent_success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEntity(createReq)).thenReturn(event);
        when(eventRepo.save(event)).thenReturn(event);
        when(mapper.toDto(event)).thenReturn(dto);

        EventDto result = service.createEvent(1L, createReq);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(eventRepo).save(event);
    }

    @Test
    void createEvent_userNotFound() {
        when(userRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.createEvent(2L, createReq));
    }

    @Test
    void getEventById_success() {
        event.setState(EventStatus.PUBLISHED);
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        when(mapper.toDto(event)).thenReturn(dto);
        when(partRepo.countByEventIdAndStatus(eq(10L), any())).thenReturn(5);
        when(viewTracker.getViewCount(10L)).thenReturn(3);

        EventDto result = service.getPublicEventById(10L, "127.0.0.1");

        assertEquals(5, result.getConfirmedRequests());
        assertEquals(3, result.getViews());
        verify(viewTracker).isUniqueView(10L, "127.0.0.1");
    }

    @Test
    void getEventById_notPublished() {
        event.setState(EventStatus.PENDING);
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        assertThrows(NotFoundException.class, () -> service.getPublicEventById(10L, "127.0.0.1"));
    }

    @Test
    void publishEvent_conflict() {
        event.setState(EventStatus.PUBLISHED);
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        assertThrows(ResponseStatusException.class, () -> service.publishEvent(10L));
    }

    @Test
    void publishEvent_success() {
        event.setState(EventStatus.PENDING);
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepo.save(event)).thenReturn(event);
        when(mapper.toDto(event)).thenReturn(dto);

        EventDto result = service.publishEvent(10L);
        assertEquals(EventStatus.PUBLISHED, event.getState());
        assertNotNull(event.getPublishedOn());
    }

    @Test
    void updateEvent_notOwner() {
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        assertThrows(RuntimeException.class, () -> service.updateEvent(99L, 10L, new EventUpdateRequest()));
    }

    @Test
    void updateEvent_dateInPast() {
        event.setInitiator(user);
        EventUpdateRequest req = new EventUpdateRequest();
        req.setEventDate(LocalDateTime.now().minusDays(1));
        when(eventRepo.findById(10L)).thenReturn(Optional.of(event));
        assertThrows(ResponseStatusException.class, () -> service.updateEvent(1L, 10L, req));
    }
}
