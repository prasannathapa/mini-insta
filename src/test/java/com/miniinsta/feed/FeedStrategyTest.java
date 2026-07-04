package com.miniinsta.feed;

import com.miniinsta.post.Post;
import com.miniinsta.post.TextPost;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedStrategyTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 2, 0, 0);

    private Post post(String caption, LocalDateTime createdAt, int likes, int comments) {
        Post post = new TextPost(1L, caption, createdAt);
        for (int i = 0; i < likes; i++) {
            post.incrementLikes();
        }
        for (int i = 0; i < comments; i++) {
            post.incrementComments();
        }
        return post;
    }

    @Test
    void chronologicalPutsNewestFirst() {
        Post older = post("older", now.minusHours(5), 100, 100);
        Post newer = post("newer", now.minusHours(1), 0, 0);
        List<Post> ranked = new ChronologicalFeedStrategy().rank(List.of(older, newer), now);
        assertEquals("newer", ranked.get(0).getCaption());
    }

    @Test
    void engagementPutsHighInteractionFirst() {
        Post popular = post("popular", now.minusHours(2), 10, 5);
        Post quiet = post("quiet", now.minusHours(1), 0, 0);
        List<Post> ranked = new EngagementFeedStrategy().rank(List.of(quiet, popular), now);
        assertEquals("popular", ranked.get(0).getCaption());
    }
}
