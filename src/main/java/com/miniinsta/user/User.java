package com.miniinsta.user;

import com.miniinsta.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A Mini Instagram account - the identity/profile aggregate owned by the
 * {@code user} context.
 *
 * <p>Notice what is <em>not</em> here: the list of followers, the user's posts,
 * their notifications. Those belong to other bounded contexts (graph, post,
 * notification) and are referenced across contexts by {@code id}, never by
 * object graph. That decoupling is what lets each context become its own
 * service later.</p>
 */
public class User {

    private final long id;
    private final String username;
    private String fullName;
    private String bio;
    private final LocalDateTime createdAt;

    /** Creates a brand-new user with a freshly generated id. */
    public User(String username, String fullName, LocalDateTime createdAt) {
        this(IdGenerator.next("user"),
                normalize(username),
                (fullName == null || fullName.isBlank()) ? normalize(username) : fullName,
                "",
                createdAt);
    }

    // Full-field constructor used both for creation (above) and rehydration.
    private User(long id, String username, String fullName, String bio, LocalDateTime createdAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username");
        this.fullName = fullName;
        this.bio = bio == null ? "" : bio;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** Rebuilds a user from stored fields (data is already normalized). */
    public static User fromStorage(long id, String username, String fullName, String bio, LocalDateTime createdAt) {
        return new User(id, username, fullName, bio, createdAt);
    }

    private static String normalize(String username) {
        return Objects.requireNonNull(username, "username").trim().toLowerCase();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName;
        }
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio == null ? "" : bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Identity is the id: two references describe the same account iff ids match.
    @Override
    public boolean equals(Object o) {
        return o instanceof User other && other.id == this.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "@" + username;
    }
}
