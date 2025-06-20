package ru.practicum.event;

import ru.practicum.event.dto.EventCreateRequest;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventDto createEvent(Long userId, EventCreateRequest request);

    EventDto getEventById(Long id);

    EventDto updateEvent(Long userId, Long eventId, EventUpdateRequest request);

    EventDto updateEventByAdmin(Long eventId, EventUpdateRequest request);

    List<EventDto> getEventsByUser(Long userId, int from, int size);

    EventDto getPublicEventById(Long eventId, String ip);

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Boolean onlyAvailable,
                                        String sort,
                                        int from,
                                        int size);

    EventDto publishEvent(Long eventId);

    EventDto rejectEvent(Long eventId);

    List<EventDto> searchEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                int from,
                                int size);

    EventDto processEventUpdate(Long eventId, EventUpdateRequest request);

}
