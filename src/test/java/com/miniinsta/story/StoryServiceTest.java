package com.miniinsta.story;

import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoryServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final LocalDateTime t = LocalDateTime.now(clock);

    @Test
    void storyIsActiveForTwentyFourHoursThenExpires() {
        Story story = new Story(1L, "hi", t);
        assertTrue(story.isActiveAt(t.plusHours(23)));
        assertTrue(story.isExpiredAt(t.plusHours(25)));
    }

    @Test
    void timelineShowsActiveStoriesFromFollowees() {
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        StoryService stories = new StoryService(new InMemoryStoryRepository(), graph, clock);
        long alice = 1L;
        long bob = 2L;
        graph.follow(bob, alice);

        stories.post(alice, "alice's story");

        assertEquals(1, stories.timelineFor(bob).size(), "follower sees the story");
        assertEquals(0, stories.timelineFor(999L).size(), "someone following nobody sees none");
    }
}
