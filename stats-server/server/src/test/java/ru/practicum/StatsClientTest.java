package ru.practicum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpRequest;

class StatsClientTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private StatsClient statsClient;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        statsClient = new StatsClient("testApp", "http://localhost:9090", objectMapper);

        var httpClientField = StatsClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(statsClient, httpClient);
    }


    @Test
    void hit_ShouldSendHttpRequest() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("127.0.0.1");
        when(req.getRequestURI()).thenReturn("/test");

        String jsonString = "{}";
        when(objectMapper.writeValueAsString(any(EndpointHit.class))).thenReturn(jsonString);

        @SuppressWarnings("unchecked")
        HttpResponse<Void> httpResponseVoid = (HttpResponse<Void>) mock(HttpResponse.class);

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<Void>>any()))
                .thenReturn(httpResponseVoid);

        statsClient.hit(req);

        verify(httpClient, times(1)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<Void>>any());
        verify(objectMapper, times(1)).writeValueAsString(any(EndpointHit.class));
    }

    @Test
    void getStats_ShouldReturnList() throws Exception {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now())
                .uris(Collections.emptyList())
                .unique(false)
                .build();

        String responseBody = "[{\"app\":\"app\",\"uri\":\"uri\",\"hits\":10}]";

        @SuppressWarnings("unchecked")
        HttpResponse<String> httpResponseString = (HttpResponse<String>) mock(HttpResponse.class);

        when(httpResponseString.statusCode()).thenReturn(200);
        when(httpResponseString.body()).thenReturn(responseBody);

        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseString);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class)))
                .thenReturn(List.of(new ViewStats("app", "uri", 10L)));

        List<ViewStats> stats = statsClient.getStats(request);

        assertNotNull(stats);
        assertEquals(1, stats.size());
        assertEquals("app", stats.get(0).getApp());
    }

    @Test
    void getStats_ShouldReturnEmptyList_WhenException() throws Exception {
        ViewsStatsRequest request = ViewsStatsRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .uris(Collections.emptyList())
                .unique(false)
                .build();

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("fail"));

        List<ViewStats> stats = statsClient.getStats(request);

        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}
