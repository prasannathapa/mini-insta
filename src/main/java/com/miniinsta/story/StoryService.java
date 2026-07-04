package com.miniinsta.story;

import com.miniinsta.graph.GraphService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Use cases for stories: posting one, and assembling the active stories from the
 * people a viewer follows (plus their own). Expiry is evaluated against the
 * injected clock's "now".
 */
public class StoryService {

    private final StoryRepository stories;
    private final GraphService graph;
    private final Clock clock;

    public StoryService(StoryRepository stories, GraphService graph, Clock clock) {
        this.stories = stories;
        this.graph = graph;
        this.clock = clock;
    }

    public Story post(long authorId, String content) {
        return stories.save(new Story(authorId, content, LocalDateTime.now(clock)));
    }

    /** Active stories from everyone the viewer follows, plus the viewer, newest first. */
    public List<Story> timelineFor(long viewerId) {
        List<Long> authors = new ArrayList<>(graph.followeesOf(viewerId));
        authors.add(viewerId);
        LocalDateTime now = LocalDateTime.now(clock);
        return stories.byAuthors(authors).stream()
                .filter(story -> story.isActiveAt(now))
                .sorted(Comparator.comparing(Story::getCreatedAt).reversed())
                .toList();
    }
}
