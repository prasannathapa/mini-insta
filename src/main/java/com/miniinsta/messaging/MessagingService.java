package com.miniinsta.messaging;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Use cases for direct messaging: send a message and read a conversation. */
public class MessagingService {

    private final MessageRepository messages;
    private final Clock clock;

    public MessagingService(MessageRepository messages, Clock clock) {
        this.messages = messages;
        this.clock = clock;
    }

    public DirectMessage send(long fromId, long toId, String text) {
        DirectMessage message = DirectMessage.create(fromId, text, LocalDateTime.now(clock));
        return messages.save(fromId, toId, message);
    }

    public List<DirectMessage> conversation(long userA, long userB) {
        return messages.between(userA, userB);
    }
}
