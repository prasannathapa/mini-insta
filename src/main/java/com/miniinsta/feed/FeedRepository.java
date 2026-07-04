package com.miniinsta.feed;

import java.util.List;

/**
 * Port for the materialized per-user timeline (the denormalized {@code feed}
 * table). This is the read-optimized copy that makes fan-out-on-write worth it.
 */
public interface FeedRepository {

    /** Appends an entry to a user's timeline (the fan-out write). */
    void addToTimeline(long userId, FeedEntry entry);

    /** A user's most recent timeline entries, newest first, up to {@code limit}. */
    List<FeedEntry> timeline(long userId, int limit);
}
