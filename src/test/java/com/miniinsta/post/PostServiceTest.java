package com.miniinsta.post;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private PostService service() {
        return new PostService(new InMemoryPostRepository(), clock);
    }

    @Test
    void likeIsIdempotentAndKeepsTheCounterInStep() {
        PostService posts = service();
        Post post = posts.postText(1L, "hi");
        assertTrue(posts.like(post.getId(), 2L), "first like counts");
        assertFalse(posts.like(post.getId(), 2L), "same user liking again does not");
        assertEquals(1, posts.find(post.getId()).orElseThrow().getLikeCount());
    }

    @Test
    void unlikeDecrementsTheCounter() {
        PostService posts = service();
        Post post = posts.postText(1L, "hi");
        posts.like(post.getId(), 2L);
        posts.unlike(post.getId(), 2L);
        assertEquals(0, posts.find(post.getId()).orElseThrow().getLikeCount());
    }

    @Test
    void commentIsStoredAndCounted() {
        PostService posts = service();
        Post post = posts.postText(1L, "hi");
        posts.comment(post.getId(), 2L, "nice");
        assertEquals(1, posts.commentsOf(post.getId()).size());
        assertEquals(1, posts.find(post.getId()).orElseThrow().getCommentCount());
    }

    @Test
    void engagingWithAMissingPostThrows() {
        assertThrows(NoSuchElementException.class, () -> service().like(999L, 1L));
    }
}
