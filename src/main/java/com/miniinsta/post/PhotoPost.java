package com.miniinsta.post;

import java.time.LocalDateTime;

/** A post backed by an image, optionally with a filter applied. */
public final class PhotoPost extends Post {

    private final String imageUrl;
    private final String filter;

    public PhotoPost(long authorId, String caption, LocalDateTime createdAt, String imageUrl, String filter) {
        super(authorId, caption, createdAt);
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.filter = (filter == null || filter.isBlank()) ? "none" : filter;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public PostType getType() {
        return PostType.PHOTO;
    }

    @Override
    public String mediaDescription() {
        return "photo " + imageUrl + " (filter: " + filter + ")";
    }
}
