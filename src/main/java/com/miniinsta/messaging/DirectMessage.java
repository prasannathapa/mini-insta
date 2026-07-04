package com.miniinsta.messaging;

import com.miniinsta.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/** A single direct message. Immutable value. */
public record DirectMessage(long id, long senderId, String text, LocalDateTime sentAt) {

    public DirectMessage {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(sentAt, "sentAt");
    }

    public static DirectMessage create(long senderId, String text, LocalDateTime sentAt) {
        return new DirectMessage(IdGenerator.next("dm"), senderId, text, sentAt);
    }
}
