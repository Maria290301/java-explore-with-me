package ru.practicum.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.*;
import ru.practicum.event.dto.EventCreateRequest;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventUpdateRequest;
import ru.practicum.request.RequestService;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestsUpdateResultDto;
import ru.practicum.request.dto.RequestsUpdateStatusRequest;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable("userId") Long userId,
                                                @PathVariable("eventId") Long eventId,
                                                @RequestBody @Valid EventUpdateRequest request) {
        EventDto updated = eventService.updateEvent(userId, eventId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@PathVariable("userId") Long userId,
                                                @RequestBody @Valid EventCreateRequest request) {
        EventDto created = eventService.createEvent(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getUserEvents(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        List<EventDto> events = eventService.getEventsByUser(userId, from, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable("userId") Long userId,
                                                 @PathVariable("eventId") Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<RequestsUpdateResultDto> updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody RequestsUpdateStatusRequest request) {

        RequestsUpdateResultDto result = requestService.updateRequestStatuses(userId, eventId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsForUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        List<ParticipationRequestDto> requests = requestService.getRequestsForUserEvent(userId, eventId);
        return ResponseEntity.ok(requests);
    }
}
