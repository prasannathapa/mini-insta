package com.miniinsta.graph;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the social-graph rules. It manages edges only; it deliberately does not
 * reach into the user store to check that ids exist - that cross-context
 * validation is the facade's job. Keeping this service free of the user context
 * is what would let the graph become its own service later.
 */
public class GraphService {

    private final FollowRepository follows;
    private final Clock clock;

    public GraphService(FollowRepository follows, Clock clock) {
        this.follows = follows;
        this.clock = clock;
    }

    /** Follows a user. No-op (returns false) for self-follows or duplicates. */
    public boolean follow(long followerId, long followeeId) {
        if (followerId == followeeId) {
            return false;
        }
        return follows.add(new Follow(followerId, followeeId, LocalDateTime.now(clock)));
    }

    public boolean unfollow(long followerId, long followeeId) {
        return follows.remove(followerId, followeeId);
    }

    public boolean isFollowing(long followerId, long followeeId) {
        return follows.exists(followerId, followeeId);
    }

    public List<Long> followeesOf(long userId) {
        return follows.followeesOf(userId);
    }

    public List<Long> followersOf(long userId) {
        return follows.followersOf(userId);
    }

    public long followerCount(long userId) {
        return follows.followerCount(userId);
    }
}
