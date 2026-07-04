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
