package ru.practicum;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDto {
    private String app;
    private String uri;
    private long hits;
}
