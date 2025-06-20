package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.event.dto.EventDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private String title;
    private boolean pinned;
    private List<EventDto> events;
}
