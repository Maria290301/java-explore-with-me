package ru.practicum.event;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViewTracker {
    private final Map<Long, Set<String>> views = new ConcurrentHashMap<>();

    public boolean isUniqueView(Long eventId, String ip) {
        views.putIfAbsent(eventId, ConcurrentHashMap.newKeySet());
        return views.get(eventId).add(ip);
    }

    public int getViewCount(Long eventId) {
        return views.getOrDefault(eventId, Set.of()).size();
    }
}
