package ru.practicum;

import java.util.List;

public interface StatsService {
    void saveHit(EndpointHit hit);

    List<ViewStats> getViewStatsList(ViewsStatsRequest request);
}