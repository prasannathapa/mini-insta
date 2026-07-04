package com.miniinsta.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Port for the post aggregate: the posts themselves plus their likes and
 * comments (which live in their own normalized tables once SQLite arrives).
 * {@code save} is generic so callers get their concrete post type back without
 * a cast.
 */
public interface PostRepository {

    <T extends Post> T save(T post);

    Optional<Post> findById(long id);

    List<Post> findByAuthor(long authorId);

    List<Post> findAll();

    // --- likes (the `likes` table) ------------------------------------------

    /** Records a like; returns {@code true} only if the user had not liked it. */
    boolean addLike(long postId, long userId, LocalDateTime at);

    /** Removes a like; returns {@code true} if one was removed. */
    boolean removeLike(long postId, long userId);

    boolean isLikedBy(long postId, long userId);

    // --- comments (the `comments` table) ------------------------------------

    Comment addComment(Comment comment);

    List<Comment> commentsOf(long postId);
}
