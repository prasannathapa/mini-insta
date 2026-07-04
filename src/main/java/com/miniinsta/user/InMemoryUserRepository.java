package com.miniinsta.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link UserRepository}. Thread-safe (services are stateless and may
 * run on many threads), fast, and needs no setup - ideal for tests and for the
 * early steps before we introduce a real database.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> byId = new ConcurrentHashMap<>();
    private final Map<String, Long> idByUsername = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        byId.put(user.getId(), user);
        idByUsername.put(user.getUsername(), user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        Long id = idByUsername.get(username.trim().toLowerCase());
        return id == null ? Optional.empty() : findById(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(byId.values());
    }
}
