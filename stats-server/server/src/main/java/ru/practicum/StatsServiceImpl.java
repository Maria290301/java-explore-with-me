package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statRepository;

    @Override
    public void saveHit(EndpointHit hit) {
        statRepository.saveHit(hit);
    }

    @Override
    public List<ViewStats> getViewStatsList(ViewsStatsRequest request) {
        if (request.isUnique()) {
            return statRepository.getUniqueStats(request);
        }

        return statRepository.getStats(request);
    }
}