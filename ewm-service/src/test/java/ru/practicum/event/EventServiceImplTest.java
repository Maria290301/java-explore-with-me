package ru.practicum.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.StatsClient;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UncorrectedParametersException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.Request;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private StatsClient statsClient;
    @Mock private RequestRepository requestRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(eventService, "applicationName", "ewm-service");
    }

    @Test
    void addNewEvent_shouldReturnDto_whenValid() {
        Long userId = 1L;
        LocalDateTime future = LocalDateTime.now().plusDays(2);

        NewEventDto dto = NewEventDto.builder()
                .annotation("Valid annotation text longer than 20 chars")
                .category(10L)
                .description("Valid description longer than 20 chars")
                .eventDate(future)
                .location(LocationDto.builder().lat(1f).lon(2f).build())
                .paid(true)
                .participantLimit(5)
                .requestModeration(false)
                .title("Valid Title")
                .build();

        User user = new User();
        user.setId(userId);

        Category cat = new Category();
        cat.setId(10L);
        cat.setName("Test Category");

        Location loc = new Location();
        loc.setId(1L);
        loc.setLat(1f);
        loc.setLon(2f);

        Event saved = new Event();
        saved.setId(100L);
        saved.setCategory(cat);
        saved.setInitiator(user);
        saved.setCreatedDate(LocalDateTime.now());
        saved.setEventStatus(EventStatus.PENDING);
        saved.setLocation(loc);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));
        when(locationRepository.save(any())).thenReturn(loc);
        when(eventRepository.save(any())).thenReturn(saved);

        EventFullDto result = eventService.addNewEvent(userId, dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(0L, result.getViews());
        assertEquals(0, result.getConfirmedRequests());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEventFromAdmin_shouldApplyStateChange() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Test");

        Location loc = new Location();
        loc.setId(1L);
        loc.setLat(55.5f);
        loc.setLon(37.5f);

        Event ev = new Event();
        ev.setId(6L);
        ev.setEventStatus(EventStatus.PENDING);
        ev.setCategory(category);
        ev.setCreatedDate(LocalDateTime.now());
        ev.setInitiator(new User());
        ev.setLocation(loc);

        when(eventRepository.findById(6L)).thenReturn(Optional.of(ev));
        when(eventRepository.save(any())).thenReturn(ev);

        UpdateEventAdminRequest req = new UpdateEventAdminRequest();
        req.setStateAction(EventAdminState.PUBLISH_EVENT);

        EventFullDto out = eventService.updateEventFromAdmin(6L, req);

        assertEquals(EventStatus.PUBLISHED, ev.getEventStatus());
        assertNotNull(out);
    }

    @Test
    void getAllParticipationRequestsFromEventByOwner_shouldReturnList() {
        Long uid = 1L, eid = 2L;

        User user = new User();
        user.setId(uid);

        Event event = new Event();
        event.setId(eid);
        event.setInitiator(user);
        event.setCategory(new Category(10L, "Cat"));
        event.setCreatedDate(LocalDateTime.now());

        Request req = new Request();
        req.setId(11L);
        req.setStatus(RequestStatus.CONFIRMED);
        req.setEvent(event);

        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        when(eventRepository.findByInitiatorIdAndId(uid, eid)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByEventId(eid)).thenReturn(List.of(req));

        List<ParticipationRequestDto> list = eventService.getAllParticipationRequestsFromEventByOwner(uid, eid);

        assertEquals(1, list.size());
        assertEquals(11L, list.get(0).getId());
    }

    @Test
    void getEventById_shouldThrow_ifNotPublished() {
        Long eid = 10L;
        Event ev = new Event();
        ev.setId(eid);
        ev.setEventStatus(EventStatus.PENDING);
        when(eventRepository.findById(eid)).thenReturn(Optional.of(ev));

        assertThrows(NotFoundException.class, () ->
                eventService.getEventById(eid, mock(HttpServletRequest.class)));
    }

    @Test
    void getEventsByUserId_shouldThrow_ifUserMissing() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                eventService.getEventsByUserId(99L, 0, 10));
    }

    @Test
    void updateEventFromAdmin_shouldThrow_ifAlreadyPublished() {
        Event ev = new Event();
        ev.setId(5L);
        ev.setEventStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(5L)).thenReturn(Optional.of(ev));

        assertThrows(ConflictException.class, () ->
                eventService.updateEventFromAdmin(5L, new UpdateEventAdminRequest()));
    }

    @Test
    void getAllEventFromPublic_shouldThrow_ifInvalidDateRange() {
        SearchEventParams params = new SearchEventParams();
        params.setRangeStart(LocalDateTime.now().plusDays(2));
        params.setRangeEnd(LocalDateTime.now().plusDays(1));

        assertThrows(UncorrectedParametersException.class,
                () -> eventService.getAllEventFromPublic(params, mock(HttpServletRequest.class)));
    }
}
