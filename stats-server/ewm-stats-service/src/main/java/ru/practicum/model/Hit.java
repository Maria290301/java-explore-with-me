package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_stats")
@Getter
@Setter
@NoArgsConstructor
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String app;
    private String uri;
    private String ip;

    @Column(name = "date")
    private LocalDateTime timestamp;
}
