package com.miniinsta.platform.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A synchronous, in-process {@link EventBus}: {@code publish} calls each
 * subscriber on the caller's thread and returns when they are done.
 *
 * <p>That is the right default for a single-process app and makes behaviour easy
 * to reason about and test. To scale out you would replace this one class with
 * an adapter over a real broker so publishers and subscribers run in different
 * services - and nothing that publishes or subscribes would have to change.</p>
 */
public class InProcessEventBus implements EventBus {

    private final Map<Class<?>, List<Consumer<?>>> handlers = new ConcurrentHashMap<>();

    @Override
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(DomainEvent event) {
        List<Consumer<?>> subscribers = handlers.get(event.getClass());
        if (subscribers == null) {
            return;
        }
        for (Consumer<?> subscriber : subscribers) {
            ((Consumer<DomainEvent>) subscriber).accept(event);
        }
    }
}
