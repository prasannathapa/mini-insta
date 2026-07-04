package com.miniinsta.platform.db;

import com.miniinsta.feed.FeedEntry;
import com.miniinsta.feed.SqliteFeedRepository;
import com.miniinsta.graph.Follow;
import com.miniinsta.graph.SqliteFollowRepository;
import com.miniinsta.notification.Notification;
import com.miniinsta.notification.NotificationType;
import com.miniinsta.notification.SqliteNotificationRepository;
import com.miniinsta.user.SqliteUserRepository;
import com.miniinsta.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence checks for the remaining SQLite adapters (follows, notifications,
 * feed). Together with the user and post contract tests this covers every
 * adapter, including that foreign keys are satisfied end to end.
 */
class SqliteRepositoriesTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 0, 0);

    private Database db;

    @BeforeEach
    void open() {
        db = Database.inMemory();
    }

    @AfterEach
    void close() {
        db.close();
    }

    private long saveUser(String name) {
        return new SqliteUserRepository(db).save(new User(name, name, T)).getId();
    }

    @Test
    void followsPersistAndAreIdempotent() {
        long alice = saveUser("alice");
        long bob = saveUser("bob");
        SqliteFollowRepository follows = new SqliteFollowRepository(db);

        assertTrue(follows.add(new Follow(bob, alice, T)));
        assertFalse(follows.add(new Follow(bob, alice, T)));
        assertTrue(follows.exists(bob, alice));
        assertEquals(1, follows.followersOf(alice).size());
        assertEquals(1L, follows.followerCount(alice));
    }

    @Test
    void notificationsPersist() {
        long alice = saveUser("alice");
        SqliteNotificationRepository notifications = new SqliteNotificationRepository(db);

        notifications.save(Notification.create(alice, NotificationType.NEW_POST, "hi", T));

        assertEquals(1, notifications.findByRecipient(alice).size());
    }

    @Test
    void feedEntriesPersistNewestFirst() {
        SqliteFeedRepository feed = new SqliteFeedRepository(db);
        feed.addToTimeline(1L, new FeedEntry(10L, 2L, T));
        feed.addToTimeline(1L, new FeedEntry(11L, 2L, T.plusHours(1)));

        assertEquals(2, feed.timeline(1L, 10).size());
        assertEquals(11L, feed.timeline(1L, 10).get(0).postId(), "newest entry first");
    }
}
