package com.miniinsta.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory notification store, keyed by recipient. */
public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<Long, List<Notification>> byRecipient = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        byRecipient.computeIfAbsent(notification.recipientId(), k -> new CopyOnWriteArrayList<>())
                .add(notification);
        return notification;
    }

    @Override
    public List<Notification> findByRecipient(long recipientId) {
        List<Notification> inbox = new ArrayList<>(byRecipient.getOrDefault(recipientId, List.of()));
        inbox.sort((a, b) -> b.createdAt().compareTo(a.createdAt())); // most recent first
        return inbox;
    }
}
