package com.miniinsta.notification.channel;

import com.miniinsta.notification.Notification;

/**
 * Native channel: it already speaks our {@link NotificationChannel} language, so
 * it needs no adapter. It just prints to the console.
 */
public class ConsoleNotificationChannel implements NotificationChannel {

    @Override
    public void deliver(String recipientHandle, Notification notification) {
        System.out.printf("      [console -> @%s] %s: %s%n",
                recipientHandle, notification.type(), notification.message());
    }

    @Override
    public String name() {
        return "console";
    }
}
