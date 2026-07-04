package com.miniinsta.platform.events;

/**
 * Marker for something that happened in the domain and may interest other
 * contexts (e.g. a post was created). Events carry ids, not object graphs, so a
 * subscriber in another context can look up whatever detail it needs.
 */
public interface DomainEvent {
}
