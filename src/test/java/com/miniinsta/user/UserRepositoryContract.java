package com.miniinsta.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A <b>contract test</b>: the behaviour every {@link UserRepository} must honour,
 * regardless of how it stores data. Concrete subclasses supply an adapter via
 * {@link #newRepository()}. Today only the in-memory adapter runs it; in step 9
 * the SQLite adapter subclasses the very same contract, proving the two are
 * truly substitutable (the Liskov Substitution Principle at the storage seam).
 */
abstract class UserRepositoryContract {

    protected abstract UserRepository newRepository();

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void savedUserIsFoundById() {
        UserRepository repo = newRepository();
        User saved = repo.save(new User("alice", "Alice", T));
        assertEquals(Optional.of("alice"), repo.findById(saved.getId()).map(User::getUsername));
    }

    @Test
    void findByUsernameIsCaseInsensitive() {
        UserRepository repo = newRepository();
        repo.save(new User("alice", "Alice", T));
        assertTrue(repo.findByUsername("ALICE").isPresent(), "lookup should ignore case");
    }

    @Test
    void missingUserYieldsEmpty() {
        assertTrue(newRepository().findById(999L).isEmpty());
    }

    @Test
    void findAllReturnsEverythingSaved() {
        UserRepository repo = newRepository();
        repo.save(new User("a", "A", T));
        repo.save(new User("b", "B", T));
        assertEquals(2, repo.findAll().size());
    }
}
