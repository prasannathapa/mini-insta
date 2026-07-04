package com.miniinsta.platform.events;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InProcessEventBusTest {

    private static final PostCreated EVENT = new PostCreated(10L, 42L, LocalDateTime.of(2026, 1, 1, 0, 0));

    @Test
    void deliversToEverySubscriber() {
        InProcessEventBus bus = new InProcessEventBus();
        AtomicInteger delivered = new AtomicInteger();
        bus.subscribe(PostCreated.class, e -> delivered.incrementAndGet());
        bus.subscribe(PostCreated.class, e -> delivered.incrementAndGet());

        bus.publish(EVENT);

        assertEquals(2, delivered.get());
    }

    @Test
    void publishingWithNoSubscribersIsHarmless() {
        InProcessEventBus bus = new InProcessEventBus();
        assertDoesNotThrow(() -> bus.publish(EVENT));
    }

    @Test
    void handlerReceivesTheEventPayload() {
        InProcessEventBus bus = new InProcessEventBus();
        AtomicLong seenAuthor = new AtomicLong();
        bus.subscribe(PostCreated.class, e -> seenAuthor.set(e.authorId()));

        bus.publish(EVENT);

        assertEquals(42L, seenAuthor.get());
    }
}
