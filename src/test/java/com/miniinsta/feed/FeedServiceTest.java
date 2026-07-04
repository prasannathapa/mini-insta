package com.miniinsta.feed;

import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.platform.cache.InMemoryCache;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostRequest;
import com.miniinsta.post.PostService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedServiceTest {

    /** A clock the test can advance, so posts get distinct timestamps. */
    private static final class Ticking extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private record Wiring(PostService posts, FeedService feed, InMemoryCache<Long, List<Post>> cache) {
    }

    private Wiring wire(Clock clock, FeedRankingStrategy strategy) {
        InProcessEventBus bus = new InProcessEventBus();
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        InMemoryPostRepository postRepo = new InMemoryPostRepository();
        PostService posts = new PostService(postRepo, bus, clock);
        InMemoryCache<Long, List<Post>> cache = new InMemoryCache<>();
        FeedService feed = new FeedService(new InMemoryFeedRepository(), postRepo, graph, cache, strategy, clock);
        bus.subscribe(PostCreated.class, feed::onPostCreated);
        return new Wiring(posts, feed, cache);
    }

    @Test
    void newPostFansOutToTheAuthorAndFollowersOnly() {
        Ticking clock = new Ticking();
        InProcessEventBus bus = new InProcessEventBus();
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        InMemoryPostRepository postRepo = new InMemoryPostRepository();
        PostService posts = new PostService(postRepo, bus, clock);
        FeedService feed = new FeedService(new InMemoryFeedRepository(), postRepo, graph,
                new InMemoryCache<>(), new ChronologicalFeedStrategy(), clock);
        bus.subscribe(PostCreated.class, feed::onPostCreated);

        long alice = 1L, bob = 2L, carol = 3L;
        graph.follow(bob, alice); // Bob follows Alice; Carol does not
        posts.create(PostRequest.text(alice, "hello"));

        assertEquals(1, feed.feed(alice, 10).size(), "author sees their own post");
        assertEquals(1, feed.feed(bob, 10).size(), "follower sees it");
        assertEquals(0, feed.feed(carol, 10).size(), "non-follower does not");
    }

    @Test
    void swappingStrategyReordersTheFeed() {
        Ticking clock = new Ticking();
        Wiring w = wire(clock, new ChronologicalFeedStrategy());

        long alice = 1L;
        Post first = w.posts().create(PostRequest.text(alice, "first"));
        clock.advance(Duration.ofHours(1));
        w.posts().create(PostRequest.text(alice, "second"));
        w.posts().like(first.getId(), 90L);
        w.posts().like(first.getId(), 91L);

        assertEquals("second", w.feed().feed(alice, 10).get(0).getCaption(), "chronological: newest first");

        w.feed().setStrategy(new EngagementFeedStrategy());
        assertEquals("first", w.feed().feed(alice, 10).get(0).getCaption(), "engagement: most-liked first");
    }

    @Test
    void feedIsCachedAndInvalidatedOnNewPost() {
        Wiring w = wire(new Ticking(), new ChronologicalFeedStrategy());
        long alice = 1L;
        w.posts().create(PostRequest.text(alice, "first"));

        w.feed().feed(alice, 10); // miss -> builds and caches
        w.feed().feed(alice, 10); // served from cache
        assertEquals(1, w.cache().misses());
        assertEquals(1, w.cache().hits());

        w.posts().create(PostRequest.text(alice, "second")); // invalidates alice's cached feed
        List<Post> refreshed = w.feed().feed(alice, 10); // miss again, recomputed
        assertEquals(2, refreshed.size());
        assertEquals(2, w.cache().misses());
    }
}
