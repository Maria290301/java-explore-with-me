package ru.practicum.request;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addNewRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}