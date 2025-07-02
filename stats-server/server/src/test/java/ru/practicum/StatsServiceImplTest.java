package ru.practicum;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceImplTest {

    @Mock
    private StatsRepository statRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    public StatsServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveHit_ShouldCallRepository() {
        EndpointHit hit = EndpointHit.builder()
                .app("testApp")
                .ip("127.0.0.1")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();

        statsService.saveHit(hit);

        verify(statRepository, times(1)).saveHit(hit);
    }

    @Test
    void getViewStatsList_ShouldReturnUniqueStats_WhenUniqueTrue() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .unique(true)
                .build();

        List<ViewStats> expected = List.of(new ViewStats("app", "/uri", 10L));
        when(statRepository.getUniqueStats(request)).thenReturn(expected);

        List<ViewStats> actual = statsService.getViewStatsList(request);

        assertEquals(expected, actual);
        verify(statRepository, times(1)).getUniqueStats(request);
        verify(statRepository, never()).getStats(any());
    }

    @Test
    void getViewStatsList_ShouldReturnStats_WhenUniqueFalse() {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .unique(false)
                .build();

        List<ViewStats> expected = List.of(new ViewStats("app", "/uri", 5L));
        when(statRepository.getStats(request)).thenReturn(expected);

        List<ViewStats> actual = statsService.getViewStatsList(request);

        assertEquals(expected, actual);
        verify(statRepository, times(1)).getStats(request);
        verify(statRepository, never()).getUniqueStats(any());
    }
}
