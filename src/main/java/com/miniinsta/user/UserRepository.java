package com.miniinsta.user;

import java.util.List;
import java.util.Optional;

/**
 * Port for storing and retrieving {@link User}s. This interface is a
 * <b>seam</b>: the services depend only on it (Dependency Inversion), never on
 * a concrete store. Today we provide an in-memory adapter; in step 9 a SQLite
 * adapter implements the exact same interface and nothing above changes.
 *
 * <p>It is deliberately small (Interface Segregation) - just the four
 * operations the user context needs.</p>
 */
public interface UserRepository {

    /** Inserts or updates the user, returning it. */
    User save(User user);

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();
}
