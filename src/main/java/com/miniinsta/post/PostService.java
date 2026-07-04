package com.miniinsta.post;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Use cases for the post context: publishing posts and recording engagement.
 *
 * <p>Every like/comment does two things - writes the normalized row (via the
 * repository's like/comment tables) and bumps the post's <b>denormalized</b>
 * counter. Keeping those two in step is this service's job, and is the concrete
 * cost of denormalization we discuss in the HLD section.</p>
 */
public class PostService {

    private final PostRepository posts;
    private final Clock clock;

    public PostService(PostRepository posts, Clock clock) {
        this.posts = posts;
        this.clock = clock;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    /**
     * The single creation path. {@link PostFactory} decides the concrete class
     * from {@link PostRequest#type()}; we stamp the time and persist. The typed
     * helpers below all funnel through here, so there is exactly one place posts
     * are born.
     */
    public Post create(PostRequest request) {
        return posts.save(PostFactory.create(request, now()));
    }

    public PhotoPost postPhoto(long authorId, String caption, String imageUrl, String filter) {
        return (PhotoPost) create(PostRequest.photo(authorId, caption, imageUrl, filter));
    }

    public VideoPost postVideo(long authorId, String caption, String videoUrl, int durationSeconds) {
        return (VideoPost) create(PostRequest.video(authorId, caption, videoUrl, durationSeconds));
    }

    public TextPost postText(long authorId, String caption) {
        return (TextPost) create(PostRequest.text(authorId, caption));
    }

    /** Likes a post. Idempotent: liking twice leaves the count at one. */
    public boolean like(long postId, long userId) {
        Post post = require(postId);
        if (posts.addLike(postId, userId, now())) {
            post.incrementLikes();
            posts.save(post);
            return true;
        }
        return false;
    }

    public boolean unlike(long postId, long userId) {
        Post post = require(postId);
        if (posts.removeLike(postId, userId)) {
            post.decrementLikes();
            posts.save(post);
            return true;
        }
        return false;
    }

    public Comment comment(long postId, long authorId, String text) {
        Post post = require(postId);
        Comment saved = posts.addComment(Comment.create(postId, authorId, text, now()));
        post.incrementComments();
        posts.save(post);
        return saved;
    }

    public List<Comment> commentsOf(long postId) {
        return posts.commentsOf(postId);
    }

    public Optional<Post> find(long postId) {
        return posts.findById(postId);
    }

    public List<Post> byAuthor(long authorId) {
        return posts.findByAuthor(authorId);
    }

    private Post require(long postId) {
        return posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("no post with id " + postId));
    }
}
