package ru.practicum;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndpointStatsDto {
    private String endpoint;
    private LocalDateTime date;
    private long requestsCount;

    public EndpointStatsDto(String endpoint, LocalDateTime date, long requestsCount) {
        this.endpoint = endpoint;
        this.date = date;
        this.requestsCount = requestsCount;
    }
}
