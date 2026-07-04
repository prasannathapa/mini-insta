package com.miniinsta.platform.events;

import java.util.function.Consumer;

/**
 * Port for publish/subscribe messaging between contexts. A publisher does not
 * know who (if anyone) is listening - that decoupling is the whole point, and
 * it is the seam where a real message broker (Kafka, RabbitMQ) would slot in.
 */
public interface EventBus {

    <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler);

    void publish(DomainEvent event);
}
