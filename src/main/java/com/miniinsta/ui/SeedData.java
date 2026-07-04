package com.miniinsta.ui;

import com.miniinsta.app.InstagramService;
import com.miniinsta.user.UsernameTakenException;

/**
 * Populates a handful of users, follows, posts and a story so the app has
 * something to show on first run.
 *
 * <p>Order matters: the follow graph is built <em>before</em> anyone posts, so
 * fan-out-on-write actually delivers those posts into followers' timelines
 * (following someone does not backfill their old posts - that is the honest
 * behaviour of fan-out on write). Idempotent: existing users are skipped, and
 * sample posts are only created on a genuinely fresh set of accounts.</p>
 */
public final class SeedData {

    private SeedData() {
    }

    public static void seed(InstagramService app) {
        boolean allNew = register(app, "alice", "Alice Anderson")
                & register(app, "bob", "Bob Brown")
                & register(app, "carol", "Carol King");

        // Follow graph first (idempotent).
        if (app.login("bob").isPresent()) {
            app.follow("alice");
        }
        if (app.login("carol").isPresent()) {
            app.follow("alice");
            app.follow("bob");
        }
        if (app.login("alice").isPresent()) {
            app.follow("bob");
        }

        // Sample content only on a fresh set, so re-running on a persistent
        // database does not pile up duplicate posts.
        if (allNew) {
            if (app.login("alice").isPresent()) {
                app.postPhoto("Sunset at the beach #beach #travel", "beach.jpg", "clarendon");
                app.postText("Morning coffee to start the day #coffee");
            }
            if (app.login("bob").isPresent()) {
                app.postVideo("Trail run this morning #fitness", "run.mp4", 95);
            }
            if (app.login("carol").isPresent()) {
                app.postPhoto("Latte art #coffee", "latte.jpg", "juno");
                app.postStory("At the cafe - come say hi!");
            }
        }
        app.logout();
    }

    private static boolean register(InstagramService app, String username, String fullName) {
        try {
            app.register(username, fullName);
            return true;
        } catch (UsernameTakenException alreadyExists) {
            return false;
        }
    }
}
