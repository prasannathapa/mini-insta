package com.miniinsta;

import com.miniinsta.notification.Notification;
import com.miniinsta.notification.NotificationType;
import com.miniinsta.notification.channel.ConsoleNotificationChannel;
import com.miniinsta.notification.channel.EmailNotificationAdapter;
import com.miniinsta.notification.channel.NotificationChannel;
import com.miniinsta.notification.channel.SmsNotificationAdapter;
import com.miniinsta.notification.channel.external.LegacyEmailClient;
import com.miniinsta.notification.channel.external.SmsGateway;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 07 - ADAPTER. The same {@link NotificationChannel} call is delivered
 * three ways: straight to the console, and - via Adapters - through a
 * third-party email client and SMS gateway whose APIs don't match ours. The
 * caller loops over identical {@code deliver(...)} calls, oblivious to the
 * translation each adapter performs.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 07 (Adapter) ===\n");

        Notification sample = Notification.create(
                1L, NotificationType.NEW_POST, "@alice shared a new post", LocalDateTime.now());

        List<NotificationChannel> channels = List.of(
                new ConsoleNotificationChannel(),
                new EmailNotificationAdapter(new LegacyEmailClient(), "mini.gram"),
                new SmsNotificationAdapter(new SmsGateway()));

        System.out.println("Delivering the same notification through every channel:\n");
        for (NotificationChannel channel : channels) {
            System.out.println("  via " + channel.name() + ":");
            channel.deliver("bob", sample);
        }

        System.out.println("\nOne interface, three backends - adapters hide the mismatched APIs.");
    }
}
