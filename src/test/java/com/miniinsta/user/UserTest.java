package com.miniinsta.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The specification for the user identity aggregate. It pins down how a
 * username is normalized, how the profile fields default, and the rule that
 * matters most: a {@link User} is equal by id, not by name.
 */
@DisplayName("User: an identity aggregate, equal by id, with a normalized username")
class UserTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Nested
    @DisplayName("username is normalized on creation")
    class Username {

        @Test
        @DisplayName("it is trimmed and lower-cased")
        void trimmedAndLowerCased() {
            assertEquals("alice", new User("  Alice  ", "Alice", T).getUsername());
        }

        @Test
        @DisplayName("a null username is rejected")
        void nullRejected() {
            assertThrows(NullPointerException.class, () -> new User(null, "x", T));
        }
    }

    @Nested
    @DisplayName("profile fields and their defaults")
    class Profile {

        @Test
        @DisplayName("a blank full name defaults to the username")
        void fullNameDefaultsToUsername() {
            assertEquals("alice", new User("alice", "   ", T).getFullName());
        }

        @Test
        @DisplayName("bio starts empty, and clearing it with null keeps it empty")
        void bioDefaultsAndClears() {
            User user = new User("alice", "Alice", T);
            assertEquals("", user.getBio());
            user.setBio("photographer");
            user.setBio(null);
            assertEquals("", user.getBio());
        }

        @Test
        @DisplayName("setFullName ignores blank input, keeping the previous name")
        void setFullNameIgnoresBlank() {
            User user = new User("alice", "Alice", T);
            user.setFullName("   ");
            assertEquals("Alice", user.getFullName());
            user.setFullName("Alice Anderson");
            assertEquals("Alice Anderson", user.getFullName());
        }
    }

    @Nested
    @DisplayName("identity is the id, not the username")
    class Identity {

        @Test
        @DisplayName("two users with different ids are never equal")
        void differentIdsAreNotEqual() {
            User alice = new User("alice", "Alice", T);
            User bob = new User("bob", "Bob", T);
            assertNotEquals(alice, bob);
            assertNotEquals(alice.getId(), bob.getId());
        }

        @Test
        @DisplayName("a user equals itself and hashes by its id")
        void equalsItselfAndHashesById() {
            User alice = new User("alice", "Alice", T);
            assertEquals(alice, alice);
            assertEquals(Long.hashCode(alice.getId()), alice.hashCode());
        }

        @Test
        @DisplayName("toString is the @handle")
        void toStringIsHandle() {
            assertEquals("@alice", new User("Alice", "Alice", T).toString());
        }
    }
}
