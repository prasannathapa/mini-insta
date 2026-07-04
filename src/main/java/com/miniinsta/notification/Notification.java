package com.miniinsta.notification;

import com.miniinsta.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An in-app notification in a user's inbox. Immutable value; the recipient is
 * stored so the notification context owns everything it needs without reaching
 * into other contexts.
 */
public record Notification(long id, long recipientId, NotificationType type,
                           String message, LocalDateTime createdAt) {

    public Notification {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Notification create(long recipientId, NotificationType type, String message,
                                      LocalDateTime createdAt) {
        return new Notification(IdGenerator.next("notification"), recipientId, type, message, createdAt);
    }
}
