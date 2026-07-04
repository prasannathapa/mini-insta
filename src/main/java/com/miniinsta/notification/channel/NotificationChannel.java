package com.miniinsta.notification.channel;

import com.miniinsta.notification.Notification;

/**
 * The "target" interface our app wants to talk to when delivering a
 * notification somewhere external. The console implements it directly; email and
 * SMS reach it through <b>Adapters</b> that wrap third-party APIs we do not
 * control and cannot change.
 */
public interface NotificationChannel {

    void deliver(String recipientHandle, Notification notification);

    /** Short name for logging/menus, e.g. "console", "email", "sms". */
    String name();
}
