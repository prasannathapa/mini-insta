package com.miniinsta;

import com.miniinsta.feed.ChronologicalFeedStrategy;
import com.miniinsta.feed.FeedService;
import com.miniinsta.feed.InMemoryFeedRepository;
import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.platform.cache.InMemoryCache;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostRequest;
import com.miniinsta.post.PostService;

import java.time.Clock;
import java.util.List;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 11 - READ-THROUGH CACHE. Feed reads now go through a cache. This demo
 * wires a feed with its own cache so it can print hit/miss counts: repeated
 * reads are served from cache, and a new post invalidates the affected feed so
 * the next read is recomputed.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 11 (read-through cache) ===\n");

        Clock clock = Clock.systemDefaultZone();
        InProcessEventBus bus = new InProcessEventBus();
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        InMemoryPostRepository postRepo = new InMemoryPostRepository();
        PostService posts = new PostService(postRepo, bus, clock);
        InMemoryCache<Long, List<Post>> cache = new InMemoryCache<>();
        FeedService feed = new FeedService(
                new InMemoryFeedRepository(), postRepo, graph, cache, new ChronologicalFeedStrategy(), clock);
        bus.subscribe(com.miniinsta.platform.events.PostCreated.class, feed::onPostCreated);

        long alice = 1L;
        posts.create(PostRequest.text(alice, "hello"));

        feed.feed(alice, 10); // miss - builds and caches
        feed.feed(alice, 10); // hit
        feed.feed(alice, 10); // hit
        System.out.printf("After 3 reads:            %d hit, %d miss%n", cache.hits(), cache.misses());

        posts.create(PostRequest.text(alice, "another")); // invalidates alice's cached feed
        feed.feed(alice, 10); // miss again - recomputed with the new post
        System.out.printf("After a new post + 1 read: %d hit, %d miss  <- the post invalidated the cache%n",
                cache.hits(), cache.misses());

        System.out.println("\nSwap InMemoryCache for a Redis adapter and the feed service is unchanged.");
    }
}
