package ru.practicum;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatsRequestDto {
    private String endpoint;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean unique;
}