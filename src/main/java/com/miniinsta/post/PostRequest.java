package com.miniinsta.post;

/**
 * A request to create a post, independent of which concrete {@link Post}
 * subtype it will become. The console builds one of these from the user's menu
 * choice and hands it to the factory - the caller never says {@code new
 * PhotoPost(...)}.
 */
public record PostRequest(PostType type, long authorId, String caption,
                          String mediaUrl, String filter, int durationSeconds) {

    public static PostRequest photo(long authorId, String caption, String imageUrl, String filter) {
        return new PostRequest(PostType.PHOTO, authorId, caption, imageUrl, filter, 0);
    }

    public static PostRequest video(long authorId, String caption, String videoUrl, int durationSeconds) {
        return new PostRequest(PostType.VIDEO, authorId, caption, videoUrl, null, durationSeconds);
    }

    public static PostRequest text(long authorId, String caption) {
        return new PostRequest(PostType.TEXT, authorId, caption, null, null, 0);
    }
}
