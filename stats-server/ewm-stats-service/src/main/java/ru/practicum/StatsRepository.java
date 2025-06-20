package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointStats, Long> {
    List<EndpointStats> findByDateBetween(LocalDateTime start, LocalDateTime end);

    List<EndpointStats> findByUriInAndDateBetween(List<String> uris, LocalDateTime start, LocalDateTime end);
}
