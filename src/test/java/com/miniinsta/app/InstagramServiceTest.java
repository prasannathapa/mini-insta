package com.miniinsta.app;

import com.miniinsta.feed.ChronologicalFeedStrategy;
import com.miniinsta.feed.FeedService;
import com.miniinsta.feed.InMemoryFeedRepository;
import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.messaging.InMemoryMessageRepository;
import com.miniinsta.messaging.MessagingService;
import com.miniinsta.notification.InMemoryNotificationRepository;
import com.miniinsta.notification.NotificationService;
import com.miniinsta.notification.channel.ConsoleNotificationChannel;
import com.miniinsta.platform.events.EventBus;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostService;
import com.miniinsta.search.InMemoryHashtagIndex;
import com.miniinsta.search.SearchService;
import com.miniinsta.story.InMemoryStoryRepository;
import com.miniinsta.story.StoryService;
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
        EventBus bus = new InProcessEventBus();
        InMemoryPostRepository postRepository = new InMemoryPostRepository();
        UserService users = new UserService(new InMemoryUserRepository(), clock);
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        PostService posts = new PostService(postRepository, bus, clock);
        NotificationService notifications = new NotificationService(
                new InMemoryNotificationRepository(), graph, users, new ConsoleNotificationChannel(), clock);
        FeedService feed = new FeedService(
                new InMemoryFeedRepository(), postRepository, graph, new ChronologicalFeedStrategy(), clock);
        StoryService stories = new StoryService(new InMemoryStoryRepository(), graph, clock);
        SearchService search = new SearchService(new InMemoryHashtagIndex(), postRepository, users);
        MessagingService messaging = new MessagingService(new InMemoryMessageRepository(), clock);
        bus.subscribe(PostCreated.class, feed::onPostCreated);
        bus.subscribe(PostCreated.class, notifications::onPostCreated);
        bus.subscribe(PostCreated.class, search::onPostCreated);
        return new InstagramService(users, graph, posts, notifications, feed, stories, search, messaging);
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
