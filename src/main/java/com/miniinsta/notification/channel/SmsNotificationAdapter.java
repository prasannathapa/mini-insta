package com.miniinsta.notification.channel;

import com.miniinsta.notification.Notification;
import com.miniinsta.notification.channel.external.SmsGateway;

/**
 * ADAPTER: bridges our {@link NotificationChannel} to the third-party
 * {@link SmsGateway}, translating a handle into a phone number and forwarding
 * the message text.
 */
public class SmsNotificationAdapter implements NotificationChannel {

    private final SmsGateway gateway;

    public SmsNotificationAdapter(SmsGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void deliver(String recipientHandle, Notification notification) {
        String number = "+1-555-" + String.format("%04d", Math.floorMod(recipientHandle.hashCode(), 10000));
        gateway.push(number, notification.message());
    }

    @Override
    public String name() {
        return "sms";
    }
}
