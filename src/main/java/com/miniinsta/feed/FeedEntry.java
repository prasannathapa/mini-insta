package com.miniinsta.feed;

import java.time.LocalDateTime;

/**
 * One row in a user's materialized timeline. Fan-out-on-write copies a
 * lightweight entry (post id + author + time) into every follower's timeline
 * when a post is created, so reading a feed is a cheap lookup rather than a join
 * across follows and posts.
 */
public record FeedEntry(long postId, long authorId, LocalDateTime createdAt) {
}
