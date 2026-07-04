package com.miniinsta.graph;

import java.util.List;

/** Port for the social graph (the {@code follows} edge table). */
public interface FollowRepository {

    /** Adds the edge; returns {@code true} only if it was not already present. */
    boolean add(Follow follow);

    /** Removes the edge; returns {@code true} if one was removed. */
    boolean remove(long followerId, long followeeId);

    boolean exists(long followerId, long followeeId);

    /** Ids this user follows. */
    List<Long> followeesOf(long userId);

    /** Ids that follow this user. */
    List<Long> followersOf(long userId);

    long followerCount(long userId);

    long followeeCount(long userId);
}
