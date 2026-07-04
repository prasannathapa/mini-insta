package com.miniinsta.post;

import java.time.LocalDateTime;

/**
 * The FACTORY. It concentrates the "which subclass?" decision in one place, so
 * callers depend on {@link PostType} data rather than on concrete constructors.
 *
 * <p>The {@code switch} is exhaustive over the {@link PostType} enum; adding a
 * new post type is a compile error here until you handle it, which is exactly
 * the safety net you want. (A registry of {@code PostType -> builder} would make
 * this fully open/closed - noted in the patterns doc.)</p>
 */
public final class PostFactory {

    private PostFactory() {
    }

    public static Post create(PostRequest request, LocalDateTime createdAt) {
        return switch (request.type()) {
            case PHOTO -> new PhotoPost(request.authorId(), request.caption(), createdAt,
                    request.mediaUrl(), request.filter());
            case VIDEO -> new VideoPost(request.authorId(), request.caption(), createdAt,
                    request.mediaUrl(), request.durationSeconds());
            case TEXT -> new TextPost(request.authorId(), request.caption(), createdAt);
        };
    }
}
