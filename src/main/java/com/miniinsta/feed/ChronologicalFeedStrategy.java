package com.miniinsta.feed;

import com.miniinsta.post.Post;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Newest first - the classic reverse-chronological timeline. */
public class ChronologicalFeedStrategy implements FeedRankingStrategy {

    @Override
    public List<Post> rank(List<Post> posts, LocalDateTime now) {
        // Newest first; break ties on id (higher id = created later) so ordering
        // is deterministic even when timestamps collide.
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt)
                        .thenComparingLong(Post::getId)
                        .reversed())
                .toList();
    }

    @Override
    public String name() {
        return "chronological";
    }
}
