package com.miniinsta.platform.cache;

import java.util.Optional;

/**
 * Port for a key/value cache. In-memory today; the obvious production swap is
 * Redis. Because callers depend on this interface, that swap is a one-line
 * change at the composition root.
 */
public interface Cache<K, V> {

    Optional<V> get(K key);

    void put(K key, V value);

    void invalidate(K key);

    void clear();
}
