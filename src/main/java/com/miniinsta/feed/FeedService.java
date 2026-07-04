package com.miniinsta.feed;

import com.miniinsta.graph.GraphService;
import com.miniinsta.platform.cache.Cache;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Builds and serves timelines.
 *
 * <p><b>Write path (fan-out on write):</b> {@link #onPostCreated(PostCreated)}
 * copies the new post into the author's and every follower's timeline, then
 * invalidates their cached feeds. The cost is write amplification - the
 * "celebrity problem" a real system solves with a hybrid pull for huge accounts
 * (discussed in the HLD doc; not built here).</p>
 *
 * <p><b>Read path (read-through cache):</b> {@link #feed(long, int)} returns a
 * cached ranked feed when present, otherwise it builds one and caches it. This
 * is the cache seam - swap the in-memory cache for Redis and nothing here
 * changes.</p>
 */
public class FeedService {

    private final FeedRepository feed;
    private final PostRepository posts;
    private final GraphService graph;
    private final Cache<Long, List<Post>> cache;
    private final Clock clock;
    private volatile FeedRankingStrategy strategy;

    public FeedService(FeedRepository feed, PostRepository posts, GraphService graph,
                       Cache<Long, List<Post>> cache, FeedRankingStrategy strategy, Clock clock) {
        this.feed = feed;
        this.posts = posts;
        this.graph = graph;
        this.cache = cache;
        this.strategy = strategy;
        this.clock = clock;
    }

    /** Event handler: fan the new post out, then drop the affected cached feeds. */
    public void onPostCreated(PostCreated event) {
        FeedEntry entry = new FeedEntry(event.postId(), event.authorId(), event.createdAt());
        feed.addToTimeline(event.authorId(), entry); // you see your own posts
        cache.invalidate(event.authorId());
        for (long followerId : graph.followersOf(event.authorId())) {
            feed.addToTimeline(followerId, entry);
            cache.invalidate(followerId);
        }
    }

    public List<Post> feed(long viewerId, int limit) {
        // Read-through: serve from cache, or build and cache. (Keyed by viewer;
        // the app always asks for the same limit.)
        Optional<List<Post>> cached = cache.get(viewerId);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<Post> ranked = build(viewerId, limit);
        cache.put(viewerId, ranked);
        return ranked;
    }

    private List<Post> build(long viewerId, int limit) {
        List<Post> timeline = feed.timeline(viewerId, limit).stream()
                .map(entry -> posts.findById(entry.postId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return strategy.rank(timeline, LocalDateTime.now(clock));
    }

    public void setStrategy(FeedRankingStrategy strategy) {
        this.strategy = strategy;
        cache.clear(); // ranking changed - every cached feed is now stale
    }

    public String strategyName() {
        return strategy.name();
    }
}
