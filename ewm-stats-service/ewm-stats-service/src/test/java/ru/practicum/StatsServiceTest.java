package ru.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsService statsService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void recordRequest_shouldSaveHit() {
        HitRequestDto dto = new HitRequestDto();
        dto.setApp("main-service");
        dto.setUri("/events");
        dto.setIp("127.0.0.1");
        dto.setTimestamp("2024-01-01 12:00:00");

        statsService.recordRequest(dto);

        ArgumentCaptor<EndpointStats> captor = ArgumentCaptor.forClass(EndpointStats.class);
        verify(statsRepository).save(captor.capture());
        EndpointStats saved = captor.getValue();

        assertEquals("main-service", saved.getApp());
        assertEquals("/events", saved.getUri());
        assertEquals("127.0.0.1", saved.getIp());
        assertEquals(LocalDateTime.parse("2024-01-01 12:00:00", formatter), saved.getDate());
    }

    @Test
    void getStats_shouldReturnNonUniqueStats() {
        List<EndpointStats> mockData = List.of(
                createHit("app1", "/event", "1.1.1.1"),
                createHit("app1", "/event", "1.1.1.2"),
                createHit("app1", "/event", "1.1.1.1")
        );

        when(statsRepository.findByDateBetween(any(), any())).thenReturn(mockData);

        List<StatsResponseDto> result = statsService.getStats("2024-01-01 00:00:00", "2024-12-31 23:59:59", null, false);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getHits());
    }

    @Test
    void getStats_shouldReturnUniqueStats() {
        List<EndpointStats> mockData = List.of(
                createHit("app1", "/event", "1.1.1.1"),
                createHit("app1", "/event", "1.1.1.2"),
                createHit("app1", "/event", "1.1.1.1")
        );

        when(statsRepository.findByDateBetween(any(), any())).thenReturn(mockData);

        List<StatsResponseDto> result = statsService.getStats("2024-01-01 00:00:00", "2024-12-31 23:59:59", null, true);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getHits());
    }

    private EndpointStats createHit(String app, String uri, String ip) {
        EndpointStats hit = new EndpointStats();
        hit.setApp(app);
        hit.setUri(uri);
        hit.setIp(ip);
        hit.setDate(LocalDateTime.now());
        return hit;
    }
}
