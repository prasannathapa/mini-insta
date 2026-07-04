package com.miniinsta.notification.channel;

import com.miniinsta.notification.Notification;
import com.miniinsta.notification.channel.external.LegacyEmailClient;

/**
 * ADAPTER: makes the third-party {@link LegacyEmailClient} usable through our
 * {@link NotificationChannel} interface. It translates a {@code deliver(handle,
 * notification)} call into the client's {@code sendEmail(address, subject,
 * body)} shape - deriving an address from the handle and a subject from the
 * notification type.
 */
public class EmailNotificationAdapter implements NotificationChannel {

    private final LegacyEmailClient client;
    private final String domain;

    public EmailNotificationAdapter(LegacyEmailClient client, String domain) {
        this.client = client;
        this.domain = domain;
    }

    @Override
    public void deliver(String recipientHandle, Notification notification) {
        String address = recipientHandle + "@" + domain;
        String subject = "Mini Instagram - " + notification.type();
        client.sendEmail(address, subject, notification.message());
    }

    @Override
    public String name() {
        return "email";
    }
}
