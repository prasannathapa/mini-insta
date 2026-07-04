package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.app.InstagramService;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 10 - STORIES, SEARCH &amp; DMs. Three more contexts round out the app:
 * ephemeral stories (24h expiry), hashtag search backed by an inverted index
 * that is filled by observing PostCreated, and direct messages.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 10 (stories, search, DMs) ===\n");

        InstagramService app = AppContext.get().instagram();
        app.register("alice", "Alice Anderson");
        app.register("bob", "Bob Brown");
        app.follow("alice");

        app.login("alice");
        app.postText("Loving the weather today #sunny #travel");
        app.postPhoto("Beach day #travel", "beach.jpg", "clarendon");
        app.postStory("At the beach! (disappears in 24h)");

        System.out.println("Search #travel -> " + app.searchHashtag("travel").size() + " post(s)");
        System.out.println("Search users 'ander' -> " + app.searchUsers("ander"));

        app.login("bob");
        System.out.println("bob's active stories feed -> " + app.stories().size() + " story(ies)");

        app.sendMessage("alice", "hey, great beach pics!");
        app.login("alice");
        app.sendMessage("bob", "thanks! come next time");

        System.out.println("\nConversation alice <-> bob:");
        app.conversationWith("bob").forEach(message -> {
            String who = app.user(message.senderId()).map(u -> "@" + u.getUsername()).orElse("?");
            System.out.println("  " + who + ": " + message.text());
        });
    }
}
