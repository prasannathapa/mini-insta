package com.miniinsta.notification;

import java.util.List;

/** Port for storing notifications and reading a user's inbox. */
public interface NotificationRepository {

    Notification save(Notification notification);

    /** A user's inbox, most recent first. */
    List<Notification> findByRecipient(long recipientId);
}
