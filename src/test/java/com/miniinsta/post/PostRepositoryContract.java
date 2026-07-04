package com.miniinsta.post;

import com.miniinsta.user.User;
import com.miniinsta.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour every {@link PostRepository} must honour. Run against both the
 * in-memory and SQLite adapters (see the two subclasses). Because SQLite
 * enforces foreign keys, the tests create the referenced users first - so the
 * "world" handed in bundles a {@link UserRepository} over the same backend.
 */
abstract class PostRepositoryContract {

    protected record World(UserRepository users, PostRepository posts) {
    }

    protected abstract World newWorld();

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 0, 0);

    private long user(World world, String name) {
        return world.users().save(new User(name, name, T)).getId();
    }

    @Test
    void savesAndFindsById() {
        World world = newWorld();
        long alice = user(world, "alice");
        Post saved = world.posts().save(new TextPost(alice, "hello", T));
        assertEquals("hello", world.posts().findById(saved.getId()).orElseThrow().getCaption());
    }

    @Test
    void findsPostsByAuthor() {
        World world = newWorld();
        long alice = user(world, "alice");
        long bob = user(world, "bob");
        world.posts().save(new TextPost(alice, "a1", T));
        world.posts().save(new TextPost(alice, "a2", T));
        world.posts().save(new TextPost(bob, "b1", T));
        assertEquals(2, world.posts().findByAuthor(alice).size());
    }

    @Test
    void likeIsIdempotent() {
        World world = newWorld();
        long alice = user(world, "alice");
        long bob = user(world, "bob");
        Post post = world.posts().save(new TextPost(alice, "x", T));
        assertTrue(world.posts().addLike(post.getId(), bob, T));
        assertFalse(world.posts().addLike(post.getId(), bob, T), "same user liking again is a no-op");
        assertTrue(world.posts().isLikedBy(post.getId(), bob));
    }

    @Test
    void unlikeRemovesTheLike() {
        World world = newWorld();
        long alice = user(world, "alice");
        long bob = user(world, "bob");
        Post post = world.posts().save(new TextPost(alice, "x", T));
        world.posts().addLike(post.getId(), bob, T);
        assertTrue(world.posts().removeLike(post.getId(), bob));
        assertFalse(world.posts().isLikedBy(post.getId(), bob));
    }

    @Test
    void storesAndReturnsComments() {
        World world = newWorld();
        long alice = user(world, "alice");
        long bob = user(world, "bob");
        Post post = world.posts().save(new TextPost(alice, "x", T));
        world.posts().addComment(Comment.create(post.getId(), bob, "nice", T));
        assertEquals(1, world.posts().commentsOf(post.getId()).size());
    }

    @Test
    void roundTripsTheSealedSubtypeAndItsFields() {
        World world = newWorld();
        long alice = user(world, "alice");
        Post saved = world.posts().save(new VideoPost(alice, "clip", T, "v.mp4", 30));
        Post loaded = world.posts().findById(saved.getId()).orElseThrow();
        assertInstanceOf(VideoPost.class, loaded);
        assertEquals(30, ((VideoPost) loaded).getDurationSeconds());
    }
}
