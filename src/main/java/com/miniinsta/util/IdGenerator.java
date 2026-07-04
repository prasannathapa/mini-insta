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
}
