package com.miniinsta.notification.channel;

import com.miniinsta.notification.Notification;
import com.miniinsta.notification.NotificationType;
import com.miniinsta.notification.channel.external.LegacyEmailClient;
import com.miniinsta.notification.channel.external.SmsGateway;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationChannelAdapterTest {

    private static final Notification N =
            Notification.create(1L, NotificationType.NEW_POST, "hello", LocalDateTime.of(2026, 1, 1, 0, 0));

    @Test
    void emailAdapterTranslatesToTheLegacyClient() {
        LegacyEmailClient client = new LegacyEmailClient();
        NotificationChannel channel = new EmailNotificationAdapter(client, "mini.gram");

        channel.deliver("bob", N);

        assertEquals("bob@mini.gram", client.lastAddress);
        assertEquals("hello", client.lastBody);
        assertTrue(client.lastSubject.contains("NEW_POST"));
    }

    @Test
    void smsAdapterTranslatesToTheGateway() {
        SmsGateway gateway = new SmsGateway();
        NotificationChannel channel = new SmsNotificationAdapter(gateway);

        channel.deliver("bob", N);

        assertEquals("hello", gateway.lastText);
        assertTrue(gateway.lastNumber.startsWith("+1-555-"));
    }
}
