package com.miniinsta.graph;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private GraphService service() {
        return new GraphService(new InMemoryFollowRepository(), clock);
    }

    @Test
    void followIsIdempotent() {
        GraphService graph = service();
        assertTrue(graph.follow(1L, 2L), "first follow should take");
        assertFalse(graph.follow(1L, 2L), "following again is a no-op");
        assertEquals(1L, graph.followerCount(2L));
    }

    @Test
    void cannotFollowYourself() {
        assertFalse(service().follow(1L, 1L));
    }

    @Test
    void unfollowRemovesTheEdge() {
        GraphService graph = service();
        graph.follow(1L, 2L);
        assertTrue(graph.unfollow(1L, 2L));
        assertFalse(graph.isFollowing(1L, 2L));
    }

    @Test
    void tracksBothDirections() {
        GraphService graph = service();
        graph.follow(1L, 2L);
        graph.follow(1L, 3L);
        graph.follow(4L, 2L);
        assertEquals(2, graph.followeesOf(1L).size());
        assertEquals(2, graph.followersOf(2L).size());
    }
}
