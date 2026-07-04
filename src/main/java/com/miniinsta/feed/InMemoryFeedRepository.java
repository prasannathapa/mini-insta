package com.miniinsta.feed;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory materialized timelines, keyed by user. */
public class InMemoryFeedRepository implements FeedRepository {

    private final Map<Long, List<FeedEntry>> timelines = new ConcurrentHashMap<>();

    @Override
    public void addToTimeline(long userId, FeedEntry entry) {
        timelines.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(entry);
    }

    @Override
    public List<FeedEntry> timeline(long userId, int limit) {
        return timelines.getOrDefault(userId, List.of()).stream()
                .sorted(Comparator.comparing(FeedEntry::createdAt).reversed())
                .limit(limit)
                .toList();
    }
}
