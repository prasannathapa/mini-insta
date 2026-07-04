package com.miniinsta.user;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use cases for the user context: registration and profile lookup/update. It is
 * stateless (all state lives in the repository) which is what lets many
 * instances run behind a load balancer - the horizontal-scaling story.
 */
public class UserService {

    private final UserRepository users;
    private final Clock clock;

    public UserService(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    /**
     * Registers a new account. Enforces the one real invariant here: usernames
     * are unique.
     */
    public User register(String username, String fullName) {
        String normalized = username == null ? "" : username.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (users.findByUsername(normalized).isPresent()) {
            throw new UsernameTakenException(normalized);
        }
        return users.save(new User(normalized, fullName, LocalDateTime.now(clock)));
    }

    public Optional<User> findByUsername(String username) {
        return users.findByUsername(username);
    }

    public Optional<User> findById(long id) {
        return users.findById(id);
    }

    public List<User> all() {
        return users.findAll();
    }

    public void updateProfile(long id, String fullName, String bio) {
        users.findById(id).ifPresent(user -> {
            user.setFullName(fullName);
            user.setBio(bio);
            users.save(user);
        });
    }
}
