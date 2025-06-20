package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatus;
import ru.practicum.request.dto.RequestsUpdateResultDto;
import ru.practicum.request.dto.RequestsUpdateStatusRequest;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("You cannot request to participate in your own event");
        }

        if (!event.isPublished()) {
            throw new ConflictException("Event is not published yet");
        }

        boolean alreadyRequested = requestRepository.findByEventId(eventId)
                .stream()
                .anyMatch(req -> req.getRequester().getId().equals(userId)
                        && req.getStatus() != RequestStatus.CANCELED
                        && req.getStatus() != RequestStatus.REJECTED);

        if (alreadyRequested) {
            throw new ConflictException("Request already exists");
        }

        int confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit has been reached");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        boolean autoConfirm = event.getParticipantLimit() == 0 || Boolean.FALSE.equals(event.getRequestModeration());

        if (autoConfirm) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("You cannot cancel someone else's request");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only event initiator can view requests");
        }

        return requestRepository.findByEventId(eventId)
                .stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto confirmRequest(Long userId, Long eventId, Long reqId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only event initiator can confirm requests");
        }

        ParticipationRequest request = requestRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Request does not belong to this event");
        }

        request.setStatus(RequestStatus.CONFIRMED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto rejectRequest(Long userId, Long eventId, Long reqId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only event initiator can reject requests");
        }

        ParticipationRequest request = requestRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Request does not belong to this event");
        }

        request.setStatus(RequestStatus.REJECTED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public RequestsUpdateResultDto updateRequestStatuses(Long userId, Long eventId, RequestsUpdateStatusRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only event initiator can update requests");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        int confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (ParticipationRequest req : requests) {
            if (!req.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request does not belong to this event");
            }

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Only pending requests can be updated. Current status: " + req.getStatus());
            }

            if ("CONFIRMED".equalsIgnoreCase(request.getStatus())) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Participant limit has been reached");
                }

                req.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmed.add(requestMapper.toDto(requestRepository.save(req)));

            } else if ("REJECTED".equalsIgnoreCase(request.getStatus())) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(requestMapper.toDto(requestRepository.save(req)));
            } else {
                throw new RuntimeException("Unsupported status: " + request.getStatus());
            }
        }

        RequestsUpdateResultDto result = new RequestsUpdateResultDto();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id = " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the initiator of this event");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(request -> {
                    ParticipationRequestDto dto = new ParticipationRequestDto();
                    dto.setId(request.getId());
                    dto.setEvent(request.getEvent().getId());
                    dto.setRequester(request.getRequester().getId());
                    dto.setCreated(request.getCreated());
                    dto.setStatus(request.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
