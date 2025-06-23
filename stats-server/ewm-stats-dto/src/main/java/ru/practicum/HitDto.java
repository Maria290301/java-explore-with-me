package ru.practicum;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HitDto {
    private Integer id;
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}
