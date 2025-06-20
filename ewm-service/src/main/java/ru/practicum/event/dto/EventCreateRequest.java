package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

@Data
public class EventCreateRequest {

    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    @NotBlank(message = "Annotation is required")
    private String annotation;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Category is required")
    private Long category;

    @NotNull(message = "Location is required")
    @Valid
    private LocationDto location;

    @JsonProperty("paid")
    private Boolean paid = false;

    @JsonProperty("participantLimit")
    @Min(value = 0, message = "Participant limit cannot be negative")
    private Integer participantLimit = 0;

    @JsonProperty("requestModeration")
    private Boolean requestModeration = true;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
}