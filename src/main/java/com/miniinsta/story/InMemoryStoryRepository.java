package com.miniinsta.story;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory {@link StoryRepository}, keyed by author. */
public class InMemoryStoryRepository implements StoryRepository {

    private final Map<Long, List<Story>> byAuthor = new ConcurrentHashMap<>();

    @Override
    public Story save(Story story) {
        byAuthor.computeIfAbsent(story.getAuthorId(), k -> new CopyOnWriteArrayList<>()).add(story);
        return story;
    }

    @Override
    public List<Story> byAuthors(Collection<Long> authorIds) {
        List<Story> result = new ArrayList<>();
        for (long authorId : authorIds) {
            result.addAll(byAuthor.getOrDefault(authorId, List.of()));
        }
        return result;
    }
}
