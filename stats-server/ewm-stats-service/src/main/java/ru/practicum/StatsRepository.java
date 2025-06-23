package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Integer> {

    @Query("SELECT new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(h)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<StatsResponseDto> getStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.StatsResponseDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<StatsResponseDto> getStatsUnique(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);
}
