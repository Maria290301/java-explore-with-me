package ru.practicum.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.Category;
import ru.practicum.event.dto.EventStatus;
import ru.practicum.location.Location;
import ru.practicum.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "create_date")
    private LocalDateTime createdDate;
    @Column(name = "description", length = 7000)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    private Location location;
    @Column(name = "paid")
    private boolean paid;
    @Column(name = "participant_limit")
    private int participantLimit;
    @Column(name = "published_date")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus eventStatus;
    @Column(name = "title", nullable = false, length = 120)
    private String title;
}