package ru.practicum.request;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestsUpdateResultDto;
import ru.practicum.request.dto.RequestsUpdateStatusRequest;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId);

    ParticipationRequestDto confirmRequest(Long userId, Long eventId, Long reqId);

    ParticipationRequestDto rejectRequest(Long userId, Long eventId, Long reqId);

    RequestsUpdateResultDto updateRequestStatuses(Long userId, Long eventId, RequestsUpdateStatusRequest request);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId);

}
