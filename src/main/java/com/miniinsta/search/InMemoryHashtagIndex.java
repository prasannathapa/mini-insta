package com.miniinsta.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory inverted index: tag -&gt; ordered list of post ids. */
public class InMemoryHashtagIndex implements HashtagIndex {

    private final Map<String, List<Long>> postsByTag = new ConcurrentHashMap<>();

    @Override
    public void index(long postId, Set<String> tags) {
        for (String tag : tags) {
            postsByTag.computeIfAbsent(tag, k -> new CopyOnWriteArrayList<>()).add(postId);
        }
    }

    @Override
    public List<Long> postsFor(String tag) {
        List<Long> ids = new ArrayList<>(postsByTag.getOrDefault(tag, List.of()));
        Collections.reverse(ids); // most recent first
        return ids;
    }
}
