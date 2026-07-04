package com.miniinsta.notification.channel.external;

/**
 * Stands in for a third-party email library we depend on but do not own. Note
 * that it speaks its OWN vocabulary - {@code sendEmail(address, subject, body)} -
 * which does not match our {@link com.miniinsta.notification.channel.NotificationChannel}.
 * That mismatch is exactly what an Adapter exists to bridge.
 *
 * <p>It records its last call so tests can assert on the translation.</p>
 */
public class LegacyEmailClient {

    public String lastAddress;
    public String lastSubject;
    public String lastBody;

    public void sendEmail(String address, String subject, String body) {
        this.lastAddress = address;
        this.lastSubject = subject;
        this.lastBody = body;
        System.out.printf("      (LegacyEmailClient) mail -> %s | subject=\"%s\"%n", address, subject);
    }
}
