package ru.practicum.request.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestsUpdateStatusRequest {
    private List<Long> requestIds;
    private String status;
}
