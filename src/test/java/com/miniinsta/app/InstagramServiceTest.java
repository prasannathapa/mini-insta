package com.miniinsta.app;

import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostService;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.UserService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the facade end to end, wiring the real services onto in-memory
 * adapters with a fixed clock - no AppContext, no database. This is the payoff
 * of Dependency Inversion: the whole app is testable in a few lines.
 */
class InstagramServiceTest {

    private InstagramService newApp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        return new InstagramService(
                new UserService(new InMemoryUserRepository(), clock),
                new GraphService(new InMemoryFollowRepository(), clock),
                new PostService(new InMemoryPostRepository(), clock));
    }

    @Test
    void followAndEngageAcrossContexts() {
        InstagramService app = newApp();
        app.register("alice", "Alice");
        PhotoPost post = app.postPhoto("hi", "a.jpg", "none");

        app.register("bob", "Bob"); // registering switches the session to Bob
        assertTrue(app.follow("alice"));
        assertTrue(app.isFollowing("alice"));
        assertTrue(app.like(post.getId()));
        app.comment(post.getId(), "nice");

        Post reloaded = app.post(post.getId()).orElseThrow();
        assertEquals(1, reloaded.getLikeCount());
        assertEquals(1, reloaded.getCommentCount());
    }

    @Test
    void actionsRequireLogin() {
        InstagramService app = newApp();
        assertThrows(IllegalStateException.class, () -> app.postText("x"));
    }

    @Test
    void followingAnUnknownUserThrows() {
        InstagramService app = newApp();
        app.register("alice", "Alice");
        assertThrows(NoSuchElementException.class, () -> app.follow("ghost"));
    }
}
