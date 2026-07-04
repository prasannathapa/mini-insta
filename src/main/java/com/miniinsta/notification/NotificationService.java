package com.miniinsta.notification;

import com.miniinsta.graph.GraphService;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The OBSERVER. It reacts to domain events rather than being called directly:
 * {@link #onPostCreated(PostCreated)} is subscribed to the {@link
 * com.miniinsta.platform.events.EventBus} in the composition root, so the post
 * context has no idea notifications even exist.
 *
 * <p>On a new post it fans out one notification to each of the author's
 * followers - looking followers up in the graph context and names in the user
 * context, exactly as a standalone notification service would query its peers.</p>
 */
public class NotificationService {

    private final NotificationRepository notifications;
    private final GraphService graph;
    private final UserService users;
    private final Clock clock;

    public NotificationService(NotificationRepository notifications, GraphService graph,
                               UserService users, Clock clock) {
        this.notifications = notifications;
        this.graph = graph;
        this.users = users;
        this.clock = clock;
    }

    /** Event handler: notify every follower of the author that they posted. */
    public void onPostCreated(PostCreated event) {
        String author = users.findById(event.authorId())
                .map(User::getUsername)
                .orElse("someone");
        String message = "@" + author + " shared a new post";
        LocalDateTime at = LocalDateTime.now(clock);
        for (long followerId : graph.followersOf(event.authorId())) {
            notifications.save(Notification.create(followerId, NotificationType.NEW_POST, message, at));
        }
    }

    public List<Notification> inbox(long userId) {
        return notifications.findByRecipient(userId);
    }
}
