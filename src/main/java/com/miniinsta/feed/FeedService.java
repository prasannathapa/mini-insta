package com.miniinsta.feed;

import com.miniinsta.graph.GraphService;
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
 * copies the new post into the author's own timeline and every follower's
 * timeline. Reads then become trivial. The cost is write amplification - a post
 * by someone with a million followers means a million writes - which is the
 * "celebrity problem" a real system solves with a hybrid pull for big accounts
 * (discussed in the HLD doc; not built here).</p>
 *
 * <p><b>Read path:</b> {@link #feed(long, int)} loads the viewer's timeline
 * entries, resolves the posts, and orders them with the current
 * {@link FeedRankingStrategy}, which can be swapped at runtime.</p>
 */
public class FeedService {

    private final FeedRepository feed;
    private final PostRepository posts;
    private final GraphService graph;
    private final Clock clock;
    private volatile FeedRankingStrategy strategy;

    public FeedService(FeedRepository feed, PostRepository posts, GraphService graph,
                       FeedRankingStrategy strategy, Clock clock) {
        this.feed = feed;
        this.posts = posts;
        this.graph = graph;
        this.strategy = strategy;
        this.clock = clock;
    }

    /** Event handler: fan the new post out to the author and their followers. */
    public void onPostCreated(PostCreated event) {
        FeedEntry entry = new FeedEntry(event.postId(), event.authorId(), event.createdAt());
        feed.addToTimeline(event.authorId(), entry); // you see your own posts
        for (long followerId : graph.followersOf(event.authorId())) {
            feed.addToTimeline(followerId, entry);
        }
    }

    public List<Post> feed(long viewerId, int limit) {
        List<Post> timeline = feed.timeline(viewerId, limit).stream()
                .map(entry -> posts.findById(entry.postId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return strategy.rank(timeline, LocalDateTime.now(clock));
    }

    public void setStrategy(FeedRankingStrategy strategy) {
        this.strategy = strategy;
    }

    public String strategyName() {
        return strategy.name();
    }
}
