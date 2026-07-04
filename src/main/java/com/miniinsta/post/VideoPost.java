package com.miniinsta.post;

import java.time.LocalDateTime;

/** A post backed by a video clip of a given length. */
public final class VideoPost extends Post {

    private final String videoUrl;
    private final int durationSeconds;

    public VideoPost(long authorId, String caption, LocalDateTime createdAt, String videoUrl, int durationSeconds) {
        super(authorId, caption, createdAt);
        this.videoUrl = videoUrl == null ? "" : videoUrl;
        this.durationSeconds = Math.max(0, durationSeconds);
    }

    private VideoPost(long id, long authorId, String caption, LocalDateTime createdAt,
                      int likeCount, int commentCount, String videoUrl, int durationSeconds) {
        super(id, authorId, caption, createdAt, likeCount, commentCount);
        this.videoUrl = videoUrl == null ? "" : videoUrl;
        this.durationSeconds = Math.max(0, durationSeconds);
    }

    public static VideoPost fromStorage(long id, long authorId, String caption, LocalDateTime createdAt,
                                        int likeCount, int commentCount, String videoUrl, int durationSeconds) {
        return new VideoPost(id, authorId, caption, createdAt, likeCount, commentCount, videoUrl, durationSeconds);
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public PostType getType() {
        return PostType.VIDEO;
    }

    @Override
    public String mediaDescription() {
        return "video " + videoUrl + " (" + durationSeconds + "s)";
    }
}
