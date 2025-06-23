package ru.practicum.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.HitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.event.dto.*;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.ParticipationRequestRepository;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final ViewTracker viewTracker;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;


    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, EventMapper eventMapper, ViewTracker viewTracker, ParticipationRequestRepository participationRequestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
        this.viewTracker = viewTracker;
        this.participationRequestRepository = participationRequestRepository;
        this.statsClient = statsClient;
    }

    @Override
    public EventDto createEvent(Long userId, EventCreateRequest request) {
        Event event = eventMapper.toEntity(request);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        event.setInitiator(initiator);
        event.setState(EventStatus.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }

    @Override
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id = " + id));
        EventDto dto = eventMapper.toDto(event);

        int confirmedRequests = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);

        return dto;
    }

    @Override
    public EventDto updateEvent(Long userId, Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id = " + eventId));

        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit a published event");
        }

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventDate cannot be in the past");
        }

        if (!event.getInitiator().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to update this event");
        }

        if ("SEND_TO_REVIEW".equalsIgnoreCase(request.getStateAction())) {
            event.setState(EventStatus.PENDING);
        } else if ("CANCEL_REVIEW".equalsIgnoreCase(request.getStateAction())) {
            event.setState(EventStatus.CANCELED);
        }

        eventMapper.updateEntity(event, request);
        eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventDate cannot be in the past");
        }

        eventMapper.updateEntity(event, request);
        eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    public List<EventDto> getEventsByUser(Long userId, int from, int size) {
        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> e.getInitiator().getId().equals(userId))
                .collect(Collectors.toList());

        int toIndex = Math.min(from + size, events.size());
        if (from > toIndex) {
            return Collections.emptyList();
        }

        return events.subList(from, toIndex).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getPublicEventById(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id = " + eventId));

        if (!event.isPublished()) {
            throw new NotFoundException("Event not found");
        }

        HitRequestDto hitDto = new HitRequestDto();
        hitDto.setApp("main-service");
        hitDto.setUri("/events/" + eventId);
        hitDto.setIp(ip);
        hitDto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));  // динамическое время

        statsClient.recordHit(hitDto);

        EventDto dto = eventMapper.toDto(event);

        dto.setViews(viewTracker.getViewCount(eventId));

        int confirmedRequests = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);

        return dto;
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               int from,
                                               int size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rangeStart cannot be after rangeEnd");
        }

        List<Event> events = eventRepository.findAll();

        events = events.stream()
                .filter(Event::isPublished)
                .collect(Collectors.toList());

        if (text != null && !text.isBlank()) {
            String lowerText = text.toLowerCase();
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(lowerText) ||
                            (e.getAnnotation() != null && e.getAnnotation().toLowerCase().contains(lowerText)))
                    .collect(Collectors.toList());
        }

        if (categories != null && !categories.isEmpty() && !(categories.size() == 1 && categories.get(0) == 0L)) {
            events = events.stream()
                    .filter(e -> e.getCategory() != null && categories.contains(e.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        if (paid != null) {
            events = events.stream()
                    .filter(e -> paid.equals(e.getPaid()))
                    .collect(Collectors.toList());
        }

        if (rangeStart != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isBefore(rangeStart))
                    .collect(Collectors.toList());
        }

        if (rangeEnd != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isAfter(rangeEnd))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    int confirmed = participationRequestRepository
                            .countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                    dto.setConfirmedRequests(confirmed);
                    dto.setViews(viewTracker.getViewCount(event.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        if (onlyAvailable != null && onlyAvailable) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getParticipantLimit() == null || dto.getParticipantLimit() == 0
                            || dto.getConfirmedRequests() < dto.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            dtos.sort(Comparator.comparing(EventShortDto::getEventDate));
        } else if ("VIEWS".equalsIgnoreCase(sort)) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        int toIndex = Math.min(from + size, dtos.size());
        if (from > toIndex) {
            return Collections.emptyList();
        }

        return dtos.subList(from, toIndex);
    }

    @Override
    public EventDto publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id = " + eventId));

        if (!event.getState().equals(EventStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending events can be published. Current state: " + event.getState());
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot publish event with date in the past");
        }

        event.setState(EventStatus.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());

        return eventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public EventDto rejectEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id = " + eventId));

        if (!event.getState().equals(EventStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending events can be rejected. Current state: " + event.getState());
        }

        event.setState(EventStatus.REJECTED);
        return eventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public List<EventDto> searchEvents(List<Long> users,
                                       List<String> states,
                                       List<Long> categories,
                                       LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd,
                                       int from,
                                       int size) {

        List<Event> events = eventRepository.findAll();

        if (users != null && !users.isEmpty() && !(users.size() == 1 && users.get(0) == 0L)) {
            events = events.stream()
                    .filter(e -> users.contains(e.getInitiator().getId()))
                    .collect(Collectors.toList());
        }

        if (categories != null && !categories.isEmpty() && !(categories.size() == 1 && categories.get(0) == 0L)) {
            events = events.stream()
                    .filter(e -> e.getCategory() != null && categories.contains(e.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        if (states != null && !states.isEmpty()) {
            List<EventStatus> statuses = states.stream()
                    .map(EventStatus::valueOf)
                    .collect(Collectors.toList());
            events = events.stream()
                    .filter(e -> statuses.contains(e.getState()))
                    .collect(Collectors.toList());
        }

        if (rangeStart != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isBefore(rangeStart))
                    .collect(Collectors.toList());
        }

        if (rangeEnd != null) {
            events = events.stream()
                    .filter(e -> !e.getEventDate().isAfter(rangeEnd))
                    .collect(Collectors.toList());
        }

        System.out.println("After filtering, events count = " + events.size());
        for (Event e : events) {
            System.out.println("Event id=" + e.getId() + ", initiator=" + e.getInitiator().getId() +
                    ", category=" + e.getCategory() + ", status=" + e.getState());
        }

        int toIndex = Math.min(from + size, events.size());
        if (from > toIndex) {
            return Collections.emptyList();
        }

        List<Event> pagedEvents = events.subList(from, toIndex);

        return pagedEvents.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toDto(event);
                    int confirmed = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                    dto.setConfirmedRequests(confirmed);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventDto processEventUpdate(Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        eventMapper.updateEntity(event, request);

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date cannot be in the past");
        }

        String action = request.getStateAction();
        if ("PUBLISH_EVENT".equalsIgnoreCase(action)) {
            if (!event.getState().equals(EventStatus.PENDING)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Only pending events can be published. Current state: " + event.getState());
            }

            if (event.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot publish event with date in the past");
            }

            event.setState(EventStatus.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equalsIgnoreCase(action)) {
            if (!event.getState().equals(EventStatus.PENDING)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Only pending events can be rejected. Current state: " + event.getState());
            }

            event.setState(EventStatus.REJECTED);
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }
}