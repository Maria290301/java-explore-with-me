package ru.practicum.event;

import org.springframework.stereotype.Component;
import ru.practicum.category.Category;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;

@Component
public class EventMapper {

    public Event toEntity(EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setAnnotation(request.getAnnotation());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());

        Category category = new Category();
        category.setId(request.getCategory());
        event.setCategory(category);

        event.setParticipantLimit(request.getParticipantLimit());
        event.setPaid(request.getPaid());
        event.setRequestModeration(request.getRequestModeration());
        event.setLat(request.getLocation().getLat());
        event.setLon(request.getLocation().getLon());

        return event;
    }

    public EventDto toDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setStatus(event.getState());
        dto.setInitiator(event.getInitiator().getId());
        dto.setCategory(new CategoryDto(event.getCategory().getId(), ""));
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPaid(event.getPaid());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setLocation(new LocationDto(event.getLat(), event.getLon()));
        dto.setViews(0);
        dto.setConfirmedRequests(0);

        return dto;
    }

    public void updateEntity(Event event, EventUpdateRequest request) {
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            event.setCategory(category);
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getLat() != null) {
            event.setLat(request.getLat());
        }
        if (request.getLon() != null) {
            event.setLon(request.getLon());
        }
    }

    public EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(new CategoryDto(event.getCategory().getId(), ""));
        dto.setPaid(event.getPaid());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(event.getInitiator().getId());
        dto.setViews(0);
        dto.setConfirmedRequests(0);

        return dto;
    }
}
