package com.miniinsta.post;

import java.time.LocalDateTime;

/** A plain text status update - caption only, no media. */
public final class TextPost extends Post {

    public TextPost(long authorId, String caption, LocalDateTime createdAt) {
        super(authorId, caption, createdAt);
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
