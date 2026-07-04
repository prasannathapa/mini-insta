package com.miniinsta;

/**
 * Entry point for Mini Instagram - a console application built to teach OOP,
 * SOLID, the Gang-of-Four design patterns and the high-level-design concepts
 * from the course (persistence, events, caching, sharding, scaling).
 *
 * <p>STEP 01 - SCAFFOLD. The project builds with Maven, runs on Java 25 and has
 * a passing test. Notice that {@link #banner()} is a separate method rather than
 * code buried inside {@code main}: pulling logic out of {@code main} is the
 * smallest possible example of "write it so you can test it".</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(banner());
    }

    /** The welcome banner. Returned (not printed) so a test can assert on it. */
    public static String banner() {
        return """
                ========================================
                        Mini Instagram (console)
                ========================================
                Step 01: Maven scaffold builds, runs and tests green.
                We'll grow this into a full app, one concept at a time.""";
    }
}
