package com.miniinsta.notification.channel.external;

/**
 * Stands in for a third-party SMS gateway. Again, its own API -
 * {@code push(number, text)} - does not match ours, so it needs an adapter.
 */
public class SmsGateway {

    public String lastNumber;
    public String lastText;

    public void push(String number, String text) {
        this.lastNumber = number;
        this.lastText = text;
        System.out.printf("      (SmsGateway) sms -> %s | \"%s\"%n", number, text);
    }
}
