package com.miniinsta.story;

import com.miniinsta.util.IdGenerator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An ephemeral story that expires 24 hours after posting. Whether it is still
 * visible is decided against a caller-supplied "now" ({@link #isActiveAt}) - the
 * time comes from an injected {@link java.time.Clock} in the service, so the
 * 24-hour rule is testable instantly instead of by waiting a day.
 */
public final class Story {

    private static final Duration LIFESPAN = Duration.ofHours(24);

    private final long id;
    private final long authorId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public Story(long authorId, String content, LocalDateTime createdAt) {
        this.id = IdGenerator.next("story");
        this.authorId = authorId;
        this.content = content == null ? "" : content;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.expiresAt = createdAt.plus(LIFESPAN);
    }

    public boolean isActiveAt(LocalDateTime now) {
        return now.isBefore(expiresAt);
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return !isActiveAt(now);
    }

    public long getId() {
        return id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
