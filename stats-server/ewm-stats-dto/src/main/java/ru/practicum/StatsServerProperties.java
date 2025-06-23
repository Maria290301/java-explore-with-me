package ru.practicum;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stats-server")
@Getter
@Setter
public class StatsServerProperties {
    private String url;
}
