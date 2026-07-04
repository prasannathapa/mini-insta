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

    private PhotoPost(long id, long authorId, String caption, LocalDateTime createdAt,
                      int likeCount, int commentCount, String imageUrl, String filter) {
        super(id, authorId, caption, createdAt, likeCount, commentCount);
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.filter = (filter == null || filter.isBlank()) ? "none" : filter;
    }

    public static PhotoPost fromStorage(long id, long authorId, String caption, LocalDateTime createdAt,
                                        int likeCount, int commentCount, String imageUrl, String filter) {
        return new PhotoPost(id, authorId, caption, createdAt, likeCount, commentCount, imageUrl, filter);
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
