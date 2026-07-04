package com.miniinsta.platform.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCacheTest {

    @Test
    void missThenPutThenHit() {
        InMemoryCache<String, Integer> cache = new InMemoryCache<>();
        assertTrue(cache.get("a").isEmpty());
        cache.put("a", 1);
        assertEquals(1, cache.get("a").orElseThrow());
        assertEquals(1, cache.hits());
        assertEquals(1, cache.misses());
    }

    @Test
    void invalidateRemovesOneEntry() {
        InMemoryCache<String, Integer> cache = new InMemoryCache<>();
        cache.put("a", 1);
        cache.invalidate("a");
        assertTrue(cache.get("a").isEmpty());
    }

    @Test
    void clearEmptiesEverything() {
        InMemoryCache<String, Integer> cache = new InMemoryCache<>();
        cache.put("a", 1);
        cache.put("b", 2);
        cache.clear();
        assertTrue(cache.get("a").isEmpty());
        assertTrue(cache.get("b").isEmpty());
    }
}
