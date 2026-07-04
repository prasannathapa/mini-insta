package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.app.InstagramService;
import com.miniinsta.notification.Notification;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 06 - EVENTBUS &amp; OBSERVER. Creating a post now publishes a
 * {@code PostCreated} event. The notification context is subscribed to it and
 * fans a notification out to the author's followers - the post context has no
 * idea it exists. This demo shows followers getting notified and a non-follower
 * getting nothing.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 06 (EventBus + Observer) ===\n");

        InstagramService app = AppContext.get().instagram();

        app.register("alice", "Alice Anderson");
        app.register("bob", "Bob");
        app.follow("alice");
        app.register("carol", "Carol");
        app.follow("alice");

        System.out.println("alice posts -> followers are notified over the bus\n");
        app.login("alice");
        app.postPhoto("Sunset at the beach", "beach.jpg", "clarendon");

        app.login("bob");
        System.out.println("  bob's inbox:   " + describe(app.notifications()));
        app.login("carol");
        System.out.println("  carol's inbox: " + describe(app.notifications()));

        app.register("dave", "Dave"); // registers + logs in; dave follows nobody
        System.out.println("  dave's inbox:  " + describe(app.notifications()) + "   (dave doesn't follow alice)");
    }

    private static String describe(List<Notification> inbox) {
        if (inbox.isEmpty()) {
            return "(empty)";
        }
        return inbox.stream()
                .map(n -> n.type() + " \"" + n.message() + "\"")
                .collect(Collectors.joining(", "));
    }
}
