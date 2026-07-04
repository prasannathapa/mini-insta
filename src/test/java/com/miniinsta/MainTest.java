package com.miniinsta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The very first test. It proves the whole test pipeline works (JUnit 5 is on
 * the classpath, Surefire runs it, Maven fails the build if it fails) and shows
 * why {@code banner()} was extracted from {@code main}: logic you can call is
 * logic you can assert on.
 */
class MainTest {

    @Test
    @DisplayName("banner names the application")
    void bannerNamesTheApp() {
        assertTrue(Main.banner().contains("Mini Instagram"),
                "welcome banner should name the app");
    }
}
