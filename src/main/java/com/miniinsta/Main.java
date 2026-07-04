package com.miniinsta;

import com.miniinsta.platform.db.Database;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.post.PostService;
import com.miniinsta.post.SqlitePostRepository;
import com.miniinsta.user.SqliteUserRepository;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 09 - SQLITE PERSISTENCE. The same services now run on SQLite adapters.
 * This demo writes with one database connection, closes it, then reopens the
 * same file with a fresh connection and reads the data back - proving it landed
 * on disk. Only the adapter changed; the services and their ports are identical
 * to the in-memory steps.</p>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Mini Instagram :: step 09 (SQLite persistence) ===\n");

        Clock clock = Clock.systemDefaultZone();
        Path dbPath = Path.of("data", "demo.db");
        Files.createDirectories(dbPath.getParent());
        Files.deleteIfExists(dbPath); // start fresh so the demo is re-runnable

        // --- write, then close the connection ---
        try (Database db = Database.file(dbPath.toString())) {
            UserService users = new UserService(new SqliteUserRepository(db), clock);
            PostService posts = new PostService(new SqlitePostRepository(db), new InProcessEventBus(), clock);

            User alice = users.register("alice", "Alice Anderson");
            posts.postText(alice.getId(), "Stored in SQLite!");
            posts.postPhoto(alice.getId(), "On disk now", "beach.jpg", "clarendon");
            System.out.println("Wrote @alice + 2 posts to " + dbPath + ", then closed the connection.");
        }

        // --- reopen the SAME file with a brand-new connection ---
        try (Database db = Database.file(dbPath.toString())) {
            UserService users = new UserService(new SqliteUserRepository(db), clock);
            PostService posts = new PostService(new SqlitePostRepository(db), new InProcessEventBus(), clock);

            System.out.println("\nReopened the database:");
            System.out.println("  users on disk: " + users.all().size());
            users.findByUsername("alice").ifPresent(u ->
                    System.out.println("  found @" + u.getUsername()
                            + " with " + posts.byAuthor(u.getId()).size() + " post(s)"));
        }

        System.out.println("\nSame ports, same services - only the adapter changed. Data persisted to disk.");
        System.out.println("(Run the whole app on SQLite with:  mvnw exec:java -Dmini.store=sqlite)");
    }
}
