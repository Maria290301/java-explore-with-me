package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.model.Hit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final StatsRepository statsRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void recordRequest(HitRequestDto requestDto) {
        Hit hit = new Hit();
        hit.setApp(requestDto.getApp());

        hit.setUri(requestDto.getUri());

        hit.setIp(requestDto.getIp());
        hit.setTimestamp(LocalDateTime.parse(requestDto.getTimestamp(), FORMATTER));
        statsRepository.save(hit);

        log.info("Сохранена статистика: app={}, uri={}, ip={}", hit.getApp(), hit.getUri(), hit.getIp());
    }

    public ResponseEntity<List<StatsResponseDto>> getStats(String start, String end, List<String> uris, boolean unique) {
        try {
            LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

            List<StatsResponseDto> stats;

            if (unique) {
                stats = statsRepository.getStatsUnique(startDate, endDate, uris);
            } else {
                stats = statsRepository.getStats(startDate, endDate, uris);
            }

            return ResponseEntity.ok(stats);

        } catch (DateTimeParseException e) {
            log.error("Ошибка парсинга даты в getStats", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
