package com.miniinsta.platform.events;

import java.time.LocalDateTime;

/** Published when a post is created. Feed and Notification both react to it. */
public record PostCreated(long postId, long authorId, LocalDateTime createdAt) implements DomainEvent {
}
