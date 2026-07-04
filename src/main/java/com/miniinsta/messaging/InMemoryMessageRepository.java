package com.miniinsta.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory messages, keyed by a canonical (min,max) pair of user ids. */
public class InMemoryMessageRepository implements MessageRepository {

    private final Map<String, List<DirectMessage>> byConversation = new ConcurrentHashMap<>();

    @Override
    public DirectMessage save(long userA, long userB, DirectMessage message) {
        byConversation.computeIfAbsent(key(userA, userB), k -> new CopyOnWriteArrayList<>()).add(message);
        return message;
    }

    @Override
    public List<DirectMessage> between(long userA, long userB) {
        return new ArrayList<>(byConversation.getOrDefault(key(userA, userB), List.of()));
    }

    private static String key(long a, long b) {
        return Math.min(a, b) + ":" + Math.max(a, b);
    }
}
