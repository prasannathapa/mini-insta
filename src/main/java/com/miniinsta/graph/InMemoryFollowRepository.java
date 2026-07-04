package com.miniinsta.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory social graph. Keeps two indexes - followees-of and followers-of -
 * in sync so both "who do I follow" and "who follows me" are O(1) lookups. That
 * mirrors what a real graph store does, and both directions matter: the follow
 * list drives feed fan-out, the follower list drives notifications.
 */
public class InMemoryFollowRepository implements FollowRepository {

    private final Map<Long, Set<Long>> followees = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> followers = new ConcurrentHashMap<>();

    @Override
    public boolean add(Follow follow) {
        boolean added = followees
                .computeIfAbsent(follow.followerId(), k -> ConcurrentHashMap.newKeySet())
                .add(follow.followeeId());
        if (added) {
            followers.computeIfAbsent(follow.followeeId(), k -> ConcurrentHashMap.newKeySet())
                    .add(follow.followerId());
        }
        return added;
    }

    @Override
    public boolean remove(long followerId, long followeeId) {
        Set<Long> f = followees.get(followerId);
        boolean removed = f != null && f.remove(followeeId);
        if (removed) {
            Set<Long> r = followers.get(followeeId);
            if (r != null) {
                r.remove(followerId);
            }
        }
        return removed;
    }

    @Override
    public boolean exists(long followerId, long followeeId) {
        Set<Long> f = followees.get(followerId);
        return f != null && f.contains(followeeId);
    }

    @Override
    public List<Long> followeesOf(long userId) {
        return new ArrayList<>(followees.getOrDefault(userId, Set.of()));
    }

    @Override
    public List<Long> followersOf(long userId) {
        return new ArrayList<>(followers.getOrDefault(userId, Set.of()));
    }

    @Override
    public long followerCount(long userId) {
        return followers.getOrDefault(userId, Set.of()).size();
    }

    @Override
    public long followeeCount(long userId) {
        return followees.getOrDefault(userId, Set.of()).size();
    }
}
