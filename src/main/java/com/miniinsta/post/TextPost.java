package com.miniinsta.post;

import java.time.LocalDateTime;

/** A plain text status update - caption only, no media. */
public final class TextPost extends Post {

    public TextPost(long authorId, String caption, LocalDateTime createdAt) {
        super(authorId, caption, createdAt);
    }

    private TextPost(long id, long authorId, String caption, LocalDateTime createdAt,
                     int likeCount, int commentCount) {
        super(id, authorId, caption, createdAt, likeCount, commentCount);
    }

    public static TextPost fromStorage(long id, long authorId, String caption, LocalDateTime createdAt,
                                       int likeCount, int commentCount) {
        return new TextPost(id, authorId, caption, createdAt, likeCount, commentCount);
    }

    @Override
    public PostType getType() {
        return PostType.TEXT;
    }

    @Override
    public String mediaDescription() {
        return "text update";
    }
}
