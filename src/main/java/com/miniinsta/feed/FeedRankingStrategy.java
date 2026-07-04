package com.miniinsta.feed;

import com.miniinsta.post.Post;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The STRATEGY: how to order the posts in a feed. Swapping the algorithm is a
 * runtime concern - the feed service holds one of these and can be handed a
 * different one without any change to how feeds are built or read.
 */
public interface FeedRankingStrategy {

    List<Post> rank(List<Post> posts, LocalDateTime now);

    String name();
}
