package com.miniinsta.graph;

import java.time.LocalDateTime;

/**
 * A directed edge in the social graph: {@code followerId} follows
 * {@code followeeId}. Immutable value, so a record. This maps one-to-one onto a
 * row in the normalized {@code follows} table.
 */
public record Follow(long followerId, long followeeId, LocalDateTime createdAt) {
}
