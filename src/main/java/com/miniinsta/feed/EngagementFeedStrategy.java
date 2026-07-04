package com.miniinsta.feed;

import com.miniinsta.post.Post;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * A simple "top posts" ranking: engagement (likes and comments) plus a recency
 * boost that fades over the first two days. This is the same shape as a real
 * ranked feed - a score function you can tune - just with obvious weights.
 */
public class EngagementFeedStrategy implements FeedRankingStrategy {

    private static final double LIKE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 5.0;

    @Override
    public List<Post> rank(List<Post> posts, LocalDateTime now) {
        return posts.stream()
                .sorted(Comparator.comparingDouble((Post post) -> score(post, now)).reversed())
                .toList();
    }

    private double score(Post post, LocalDateTime now) {
        long hoursOld = Math.max(0, Duration.between(post.getCreatedAt(), now).toHours());
        double recencyBoost = Math.max(0, 48 - hoursOld) * 0.5;
        return LIKE_WEIGHT * post.getLikeCount()
                + COMMENT_WEIGHT * post.getCommentCount()
                + recencyBoost;
    }

    @Override
    public String name() {
        return "engagement";
    }
}
