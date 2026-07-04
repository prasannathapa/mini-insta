package com.miniinsta.notification;

import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PostRequest;
import com.miniinsta.post.PostService;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wires the real bus, post service and notification observer together (all on
 * in-memory adapters) and verifies the Observer behaviour: a new post notifies
 * followers and only followers.
 */
class NotificationServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void newPostNotifiesFollowersOnly() {
        InProcessEventBus bus = new InProcessEventBus();
        UserService users = new UserService(new InMemoryUserRepository(), clock);
        GraphService graph = new GraphService(new InMemoryFollowRepository(), clock);
        NotificationService notifications =
                new NotificationService(new InMemoryNotificationRepository(), graph, users, clock);
        bus.subscribe(PostCreated.class, notifications::onPostCreated);
        PostService posts = new PostService(new InMemoryPostRepository(), bus, clock);

        User alice = users.register("alice", "Alice");
        User bob = users.register("bob", "Bob");
        User carol = users.register("carol", "Carol");
        graph.follow(bob.getId(), alice.getId()); // only Bob follows Alice

        posts.create(PostRequest.text(alice.getId(), "hello world"));

        assertEquals(1, notifications.inbox(bob.getId()).size(), "follower is notified");
        assertEquals(0, notifications.inbox(carol.getId()).size(), "non-follower is not");

        Notification n = notifications.inbox(bob.getId()).get(0);
        assertEquals(NotificationType.NEW_POST, n.type());
        assertTrue(n.message().contains("@alice"));
    }
}
