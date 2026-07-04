package com.miniinsta.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The specification for direct messages. A conversation is keyed by the
 * <em>unordered</em> pair of users, so (1,2) and (2,1) are the same thread.
 */
@DisplayName("MessagingService: conversations keyed by the unordered pair of users")
class MessagingServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private MessagingService service() {
        return new MessagingService(new InMemoryMessageRepository(), clock);
    }

    @Test
    @DisplayName("a conversation holds both sides, in the order they were sent")
    void keepsBothSidesInOrder() {
        MessagingService messaging = service();
        messaging.send(1L, 2L, "hi");
        messaging.send(2L, 1L, "hey");
        assertEquals(2, messaging.conversation(1L, 2L).size());
        assertEquals("hi", messaging.conversation(1L, 2L).get(0).text());
        assertEquals("hey", messaging.conversation(1L, 2L).get(1).text());
    }

    @Test
    @DisplayName("the pair is unordered: reading (2,1) sees the same thread as (1,2)")
    void pairIsUnordered() {
        MessagingService messaging = service();
        messaging.send(1L, 2L, "hi");
        messaging.send(2L, 1L, "hey");
        assertEquals(messaging.conversation(1L, 2L).size(), messaging.conversation(2L, 1L).size());
    }

    @Test
    @DisplayName("different pairs are separate conversations that do not mix")
    void separatePairsDoNotMix() {
        MessagingService messaging = service();
        messaging.send(1L, 2L, "to bob");
        messaging.send(1L, 3L, "to carol");
        assertEquals(1, messaging.conversation(1L, 2L).size());
        assertEquals(1, messaging.conversation(1L, 3L).size());
        assertTrue(messaging.conversation(2L, 3L).isEmpty(), "two users who never spoke have no thread");
    }
}
