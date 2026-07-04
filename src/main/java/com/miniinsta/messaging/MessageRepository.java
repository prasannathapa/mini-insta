package com.miniinsta.messaging;

import java.util.List;

/**
 * Port for direct messages. A conversation is identified by the pair of user
 * ids (order does not matter), so callers just say "messages between A and B".
 */
public interface MessageRepository {

    DirectMessage save(long userA, long userB, DirectMessage message);

    /** All messages exchanged between two users, oldest first. */
    List<DirectMessage> between(long userA, long userB);
}
