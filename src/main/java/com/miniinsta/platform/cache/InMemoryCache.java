package com.miniinsta.platform.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A trivial in-memory {@link Cache} that also counts hits and misses so a demo
 * can show the cache actually working. A real deployment swaps this for Redis.
 */
public class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, V> store = new ConcurrentHashMap<>();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    @Override
    public Optional<V> get(K key) {
        V value = store.get(key);
        (value != null ? hits : misses).incrementAndGet();
        return Optional.ofNullable(value);
    }

    @Override
    public void put(K key, V value) {
        store.put(key, value);
    }

    @Override
    public void invalidate(K key) {
        store.remove(key);
    }

    @Override
    public void clear() {
        store.clear();
    }

    public long hits() {
        return hits.get();
    }

    public long misses() {
        return misses.get();
    }
}
