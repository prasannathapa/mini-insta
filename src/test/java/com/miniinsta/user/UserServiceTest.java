package com.miniinsta.user;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private UserService service() {
        return new UserService(new InMemoryUserRepository(), clock);
    }

    @Test
    void registersAndFindsUser() {
        UserService users = service();
        User alice = users.register("alice", "Alice");
        assertEquals("alice", alice.getUsername());
        assertTrue(users.findByUsername("alice").isPresent());
    }

    @Test
    void usernameMustBeUniqueIgnoringCase() {
        UserService users = service();
        users.register("alice", "Alice");
        assertThrows(UsernameTakenException.class, () -> users.register("ALICE", "Impostor"));
    }

    @Test
    void blankUsernameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service().register("   ", "x"));
    }
}
