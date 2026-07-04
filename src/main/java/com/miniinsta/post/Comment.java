package com.miniinsta.post;

import com.miniinsta.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A comment on a post. An immutable value, so it is a {@code record}. It
 * references the post and its author by id (cross-aggregate references are
 * always by id).
 */
public record Comment(long id, long postId, long authorId, String text, LocalDateTime createdAt) {

    public Comment {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    /** Creates a new comment with a freshly generated id. */
    public static Comment create(long postId, long authorId, String text, LocalDateTime createdAt) {
        return new Comment(IdGenerator.next("comment"), postId, authorId, text, createdAt);
    }
}
