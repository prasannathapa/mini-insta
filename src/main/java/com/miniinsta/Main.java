package com.miniinsta;

import com.miniinsta.post.Post;
import com.miniinsta.post.PostFactory;
import com.miniinsta.post.PostRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 05 - FACTORY. Post creation is centralized in {@link PostFactory}: a
 * {@link PostRequest} carries a {@code PostType} plus fields, and the factory
 * returns the matching subclass. Every posting path in the app now funnels
 * through it, so there is one place to change when a new post type appears.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 05 (Factory) ===\n");
        System.out.println("PostFactory turns a PostType + fields into the right Post subclass:\n");

        LocalDateTime now = LocalDateTime.now();
        List<PostRequest> drafts = List.of(
                PostRequest.photo(1L, "Sunset at the beach", "beach.jpg", "clarendon"),
                PostRequest.video(1L, "My morning run", "run.mp4", 42),
                PostRequest.text(1L, "Hello world!"));

        for (PostRequest draft : drafts) {
            Post post = PostFactory.create(draft, now);
            System.out.printf("  %-5s -> %-9s | %s%n",
                    draft.type(), post.getClass().getSimpleName(), post.mediaDescription());
        }

        System.out.println("\nAll posting in the app now flows through this one factory.");
    }
}
