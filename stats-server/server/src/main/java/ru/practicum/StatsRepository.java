package ru.practicum;

import java.util.List;

public interface StatsRepository {
    void saveHit(EndpointHit hit);

    List<ViewStats> getStats(ViewsStatsRequest request);

    List<ViewStats> getUniqueStats(ViewsStatsRequest request);
}