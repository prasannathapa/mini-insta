package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.TextPost;
import com.miniinsta.post.VideoPost;
import com.miniinsta.user.User;

import java.time.LocalDateTime;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 03 - PORTS, ADAPTERS &amp; THE COMPOSITION ROOT. Entities are now
 * stored behind repository <em>ports</em>. {@link AppContext} is the single
 * place that picks the concrete (in-memory) adapters; this demo saves data
 * through those ports and reads it back, proving the seam works before any
 * business logic exists.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 03 (ports + adapters + AppContext) ===\n");

        AppContext ctx = AppContext.get();
        LocalDateTime now = LocalDateTime.now();

        User alice = ctx.users().save(new User("alice", "Alice Anderson", now));
        User bob = ctx.users().save(new User("bob", "Bob Brown", now));

        ctx.posts().save(new PhotoPost(alice.getId(), "Sunset at the beach", now, "beach.jpg", "clarendon"));
        ctx.posts().save(new TextPost(alice.getId(), "Good morning!", now));
        ctx.posts().save(new VideoPost(bob.getId(), "My morning run", now, "run.mp4", 42));

        System.out.println("Lookup by username (case-insensitive): "
                + ctx.users().findByUsername("ALICE").map(User::getFullName).orElse("<none>"));

        System.out.println("\nAlice's posts (via PostRepository.findByAuthor):");
        ctx.posts().findByAuthor(alice.getId())
                .forEach(post -> System.out.println("  [" + post.getType() + "] " + post.getCaption()));

        System.out.printf("%nStored: %d users, %d posts%n",
                ctx.users().findAll().size(), ctx.posts().findAll().size());
    }
}
