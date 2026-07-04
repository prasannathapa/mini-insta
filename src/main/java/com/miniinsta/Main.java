package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.app.InstagramService;
import com.miniinsta.feed.EngagementFeedStrategy;
import com.miniinsta.post.Post;

import java.util.List;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 08 - FEED (fan-out on write + Strategy). Posts are pushed into each
 * follower's timeline as they are created. Reading a feed then just loads that
 * timeline and orders it with the current {@code FeedRankingStrategy}. This demo
 * shows the same feed under two strategies - the order changes, nothing else
 * does.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 08 (feed: fan-out + Strategy) ===\n");

        InstagramService app = AppContext.get().instagram();

        app.register("alice", "Alice Anderson");
        app.register("bob", "Bob");
        app.follow("alice");
        app.register("carol", "Carol");

        // Alice posts three things (Bob already follows her, so they fan out to him).
        app.login("alice");
        Post first = app.postText("First post (will earn the most engagement)");
        app.postPhoto("Second post", "pic.jpg", "clarendon");
        Post third = app.postText("Third post (the newest)");

        // Give the first post real engagement from two users.
        app.login("bob");
        app.like(first.getId());
        app.comment(first.getId(), "love this");
        app.login("carol");
        app.like(first.getId());

        app.login("bob");
        System.out.println("Bob's feed, ranked '" + app.feedRanking() + "':");
        printFeed(app.feed());

        app.setFeedRanking(new EngagementFeedStrategy());
        System.out.println("\nSame feed, ranked '" + app.feedRanking() + "':");
        printFeed(app.feed());

        System.out.println("\nId of newest post: " + third.getId() + " - note how the ordering flips.");
    }

    private static void printFeed(List<Post> posts) {
        int rank = 1;
        for (Post post : posts) {
            System.out.printf("  %d. \"%s\"  (likes=%d, comments=%d)%n",
                    rank++, post.getCaption(), post.getLikeCount(), post.getCommentCount());
        }
    }
}
