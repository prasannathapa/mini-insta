package com.miniinsta.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hands out monotonically increasing ids per named sequence, so every entity
 * gets a small readable id (user #1, post #1, comment #1, ...).
 *
 * <p>In production the database assigns primary keys; when we add SQLite the
 * repositories will use the DB's ids instead. This keeps console output tidy
 * while everything still lives in memory.</p>
 */
public final class IdGenerator {

    private static final Map<String, AtomicLong> SEQUENCES = new ConcurrentHashMap<>();

    private IdGenerator() {
    }

    /** Returns the next id for the given sequence, starting at 1. */
    public static long next(String sequence) {
        return SEQUENCES.computeIfAbsent(sequence, key -> new AtomicLong()).incrementAndGet();
    }

    /**
     * Advances a sequence so the next id is above {@code value}. Used at start-up
     * to resume from the maximum id already stored in the database, so
     * app-generated ids never collide with persisted rows.
     */
    public static void seed(String sequence, long value) {
        SEQUENCES.computeIfAbsent(sequence, key -> new AtomicLong())
                .updateAndGet(current -> Math.max(current, value));
    }
}
