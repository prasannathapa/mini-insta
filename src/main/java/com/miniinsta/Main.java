package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.app.InstagramService;
import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.Post;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 04 - SERVICES &amp; FACADE. All behaviour now sits behind
 * {@link InstagramService}. This demo drives the whole app through that one
 * facade: register, follow, post, like, comment - each call quietly
 * orchestrating the user, graph and post contexts.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 04 (services + facade) ===\n");

        InstagramService app = AppContext.get().instagram();

        // Alice registers (which logs her in) and posts.
        app.register("alice", "Alice Anderson");
        PhotoPost sunset = app.postPhoto("Sunset at the beach", "beach.jpg", "clarendon");
        app.postText("Good morning!");

        // Bob registers, follows Alice, and engages with her post.
        app.register("bob", "Bob Brown");
        app.follow("alice");
        app.like(sunset.getId());
        app.comment(sunset.getId(), "Gorgeous shot!");

        System.out.println("bob follows: " + app.following());
        System.out.println("bob follows alice? " + app.isFollowing("alice"));

        Post reloaded = app.post(sunset.getId()).orElseThrow();
        System.out.printf("%n\"%s\" now has %d like(s) and %d comment(s)%n",
                reloaded.getCaption(), reloaded.getLikeCount(), reloaded.getCommentCount());
    }
}
