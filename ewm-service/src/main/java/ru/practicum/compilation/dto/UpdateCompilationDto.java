package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationDto {
    @Size(max = 50, message = "Title must be at most 50 characters")
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
