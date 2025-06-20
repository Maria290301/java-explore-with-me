package ru.practicum.event;

import org.junit.jupiter.api.Test;
import ru.practicum.category.Category;
import ru.practicum.event.dto.*;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    void toEntity_shouldMapEventCreateRequestToEntity() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        EventCreateRequest request = new EventCreateRequest();
        request.setTitle("Title");
        request.setAnnotation("Annotation");
        request.setDescription("Description");
        request.setEventDate(LocalDateTime.parse("2025-07-01 10:00:00", formatter));
        request.setCategory(5L);
        request.setParticipantLimit(100);
        request.setPaid(true);
        request.setRequestModeration(false);
        request.setLocation(new LocationDto(55.75, 37.61));

        Event event = mapper.toEntity(request);

        assertEquals("Title", event.getTitle());
        assertEquals("Annotation", event.getAnnotation());
        assertEquals("Description", event.getDescription());
        assertEquals(LocalDateTime.parse("2025-07-01 10:00:00", formatter), event.getEventDate());
        assertNotNull(event.getCategory());
        assertEquals(5L, event.getCategory().getId());
        assertEquals(100, event.getParticipantLimit());
        assertTrue(event.getPaid());
        assertFalse(event.getRequestModeration());
        assertEquals(55.75, event.getLat());
        assertEquals(37.61, event.getLon());
    }

    @Test
    void toDto_shouldMapEntityToDto() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Title");
        event.setAnnotation("Annotation");
        event.setDescription("Description");
        event.setEventDate(LocalDateTime.parse("2025-07-01 10:00:00", formatter));
        event.setState(EventStatus.PUBLISHED);
        User initiator = new User();
        initiator.setId(10L);
        event.setInitiator(initiator);
        Category category = new Category();
        category.setId(5L);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.parse("2025-06-01 09:00:00", formatter));
        event.setPublishedOn(LocalDateTime.parse("2025-06-15 09:00:00", formatter));
        event.setParticipantLimit(50);
        event.setPaid(false);
        event.setRequestModeration(true);
        event.setLat(55.75);
        event.setLon(37.61);

        EventDto dto = mapper.toDto(event);

        assertEquals(1L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Annotation", dto.getAnnotation());
        assertEquals("Description", dto.getDescription());
        assertEquals(LocalDateTime.parse("2025-07-01 10:00:00", formatter), dto.getEventDate());
        assertEquals(EventStatus.PUBLISHED, dto.getStatus());
        assertEquals(10L, dto.getInitiator());
        assertNotNull(dto.getCategory());
        assertEquals(5L, dto.getCategory().getId());
        assertEquals(LocalDateTime.parse("2025-06-01 09:00:00", formatter), dto.getCreatedOn());
        assertEquals(LocalDateTime.parse("2025-06-15 09:00:00", formatter), dto.getPublishedOn());
        assertEquals(50, dto.getParticipantLimit());
        assertFalse(dto.getPaid());
        assertTrue(dto.getRequestModeration());
        assertNotNull(dto.getLocation());
        assertEquals(55.75, dto.getLocation().getLat());
        assertEquals(37.61, dto.getLocation().getLon());
        assertEquals(0, dto.getViews());
        assertEquals(0, dto.getConfirmedRequests());
    }

    @Test
    void updateEntity_shouldUpdateOnlyNonNullFields() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        Event event = new Event();
        event.setTitle("Old Title");
        event.setAnnotation("Old Annotation");
        event.setDescription("Old Description");
        event.setEventDate(LocalDateTime.parse("2025-06-01T10:00:00", formatter));
        Category category = new Category();
        category.setId(1L);
        event.setCategory(category);
        event.setParticipantLimit(10);
        event.setPaid(false);
        event.setRequestModeration(false);
        event.setLat(50.0);
        event.setLon(30.0);

        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setTitle("New Title");
        updateRequest.setAnnotation(null);
        updateRequest.setDescription("New Description");
        updateRequest.setEventDate(null);
        updateRequest.setCategoryId(2L);
        updateRequest.setParticipantLimit(null);
        updateRequest.setPaid(true);
        updateRequest.setRequestModeration(true);
        updateRequest.setLat(null);
        updateRequest.setLon(40.0);

        mapper.updateEntity(event, updateRequest);

        assertEquals("New Title", event.getTitle());
        assertEquals("Old Annotation", event.getAnnotation());
        assertEquals("New Description", event.getDescription());
        assertEquals(LocalDateTime.parse("2025-06-01T10:00:00", formatter), event.getEventDate());
        assertEquals(2L, event.getCategory().getId());
        assertEquals(10, event.getParticipantLimit());
        assertTrue(event.getPaid());
        assertTrue(event.getRequestModeration());
        assertEquals(50.0, event.getLat());
        assertEquals(40.0, event.getLon());
    }

    @Test
    void toShortDto_shouldMapEntityToShortDto() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Short Title");
        event.setAnnotation("Short Annotation");
        Category category = new Category();
        category.setId(5L);
        event.setCategory(category);
        event.setPaid(true);
        event.setEventDate(LocalDateTime.parse("2025-07-01T10:00:00", formatter));
        User initiator = new User();
        initiator.setId(10L);
        event.setInitiator(initiator);

        EventShortDto dto = mapper.toShortDto(event);

        assertEquals(1L, dto.getId());
        assertEquals("Short Title", dto.getTitle());
        assertEquals("Short Annotation", dto.getAnnotation());
        assertNotNull(dto.getCategory());
        assertEquals(5L, dto.getCategory().getId());
        assertTrue(dto.getPaid());
        assertEquals(LocalDateTime.parse("2025-07-01T10:00:00", formatter), dto.getEventDate());
        assertEquals(10L, dto.getInitiator());
        assertEquals(0, dto.getViews());
        assertEquals(0, dto.getConfirmedRequests());
    }
}
