package com.miniinsta.post;

import com.miniinsta.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A feed post. A {@code sealed} hierarchy whose only permitted subtypes are
 * {@link PhotoPost}, {@link VideoPost} and {@link TextPost}. Sealing lets
 * callers pattern-match with an exhaustive {@code switch} - no {@code default}
 * branch, and the compiler fails the build if a new post type is ever added
 * without handling it.
 *
 * <p>{@code likeCount} and {@code commentCount} are <b>denormalized counters</b>.
 * The authoritative list of likes/comments lives in its own table (added with
 * SQLite); these counters exist so rendering a feed never has to run
 * {@code COUNT(*)}. The trade-off - they must be kept in step on every
 * like/unlike - is exactly the normalization-vs-denormalization lesson.</p>
 */
public sealed abstract class Post permits PhotoPost, VideoPost, TextPost {

    private final long id;
    private final long authorId;
    private final String caption;
    private final LocalDateTime createdAt;

    private int likeCount;
    private int commentCount;

    protected Post(long authorId, String caption, LocalDateTime createdAt) {
        this.id = IdGenerator.next("post");
        this.authorId = authorId;
        this.caption = caption == null ? "" : caption;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** The concrete kind of post - implemented by each subclass. */
    public abstract PostType getType();

    /** A short, human-readable description of the media this post carries. */
    public abstract String mediaDescription();

    // --- denormalized engagement counters -----------------------------------

    public void incrementLikes() {
        likeCount++;
    }

    public void decrementLikes() {
        if (likeCount > 0) {
            likeCount--;
        }
    }

    public void incrementComments() {
        commentCount++;
    }

    public void decrementComments() {
        if (commentCount > 0) {
            commentCount--;
        }
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    // --- accessors ----------------------------------------------------------

    public long getId() {
        return id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
