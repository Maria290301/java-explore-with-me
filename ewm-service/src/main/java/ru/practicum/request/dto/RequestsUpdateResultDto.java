package ru.practicum.request.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestsUpdateResultDto {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}
