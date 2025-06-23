package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatsClient extends BaseClient {

    private static final String API_PREFIX = "";

    @Autowired
    public StatsClient(StatsServerProperties props, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(props.getUrl() + API_PREFIX))
                .build());
    }

    public ResponseEntity<Object> recordHit(HitRequestDto hitDto) {
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start);
        parameters.put("end", end);

        if (uris != null && !uris.isEmpty()) {
            List<String> encodedUris = uris.stream()
                    .map(uri -> URLEncoder.encode(uri, StandardCharsets.UTF_8))
                    .collect(Collectors.toList());
            parameters.put("uris", encodedUris);
        }

        parameters.put("unique", unique);

        return get("/stats", parameters);
    }
}
