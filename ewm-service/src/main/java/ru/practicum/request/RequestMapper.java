package ru.practicum.request;

import org.springframework.stereotype.Component;
import ru.practicum.request.dto.ParticipationRequestDto;

@Component
public class RequestMapper {
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setStatus(request.getStatus().name());
        dto.setCreated(request.getCreated());
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        return dto;
    }
}
