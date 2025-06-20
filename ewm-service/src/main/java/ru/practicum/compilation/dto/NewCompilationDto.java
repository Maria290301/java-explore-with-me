package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NewCompilationDto {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 50, message = "Title must be at most 50 characters")
    private String title;

    private boolean pinned = false;

    private List<Long> events = new ArrayList<>();
}
