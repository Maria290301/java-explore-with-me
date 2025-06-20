package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;

import java.time.LocalDateTime;

@Data
public class EventDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonProperty("state")
    private EventStatus status;

    private Long initiator;
    private CategoryDto category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Integer participantLimit;
    private Boolean paid;
    private Boolean requestModeration;
    private LocationDto location;
    private Integer views;
    private Integer confirmedRequests;
}
