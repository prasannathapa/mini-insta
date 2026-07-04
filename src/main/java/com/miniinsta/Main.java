package com.miniinsta;

import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.Post;
import com.miniinsta.post.TextPost;
import com.miniinsta.post.VideoPost;
import com.miniinsta.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 02 - DOMAIN MODEL. We now have the core entities of the {@code user}
 * and {@code post} contexts: {@link User} and a sealed {@link Post} hierarchy.
 * There is no storage or services yet, so this demo builds a few objects by
 * hand and renders them - just enough to show the model works and to exercise
 * the pattern-matching {@code switch}.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mini Instagram :: step 02 (domain model) ===\n");

        LocalDateTime now = LocalDateTime.now();
        var alice = new User("alice", "Alice Anderson", now);
        var bob = new User("bob", "Bob Brown", now);
        Map<Long, User> byId = Map.of(alice.getId(), alice, bob.getId(), bob);

        Post p1 = new PhotoPost(alice.getId(), "Sunset at the beach", now, "beach.jpg", "clarendon");
        Post p2 = new VideoPost(alice.getId(), "My morning run", now, "run.mp4", 42);
        Post p3 = new TextPost(bob.getId(), "Hello world - my first post!", now);

        // Engagement is tracked as denormalized counters on the post.
        p1.incrementLikes();
        p1.incrementComments();

        System.out.printf("Users: %s (%s), %s%n%n", alice, alice.getFullName(), bob);
        System.out.println("Posts:");
        for (Post post : List.of(p1, p2, p3)) {
            System.out.println("  " + render(post, byId));
        }
    }

    /**
     * Renders a post. The {@code switch} pattern-matches over the sealed
     * hierarchy; because {@link Post} is sealed the three cases are exhaustive
     * and no {@code default} branch is needed.
     */
    private static String render(Post post, Map<Long, User> byId) {
        String extra = switch (post) {
            case PhotoPost photo -> "filter=" + photo.getFilter();
            case VideoPost video -> video.getDurationSeconds() + "s";
            case TextPost ignored -> "text only";
        };
        User author = byId.get(post.getAuthorId());
        return "[%-5s] %s: \"%s\"  likes=%d comments=%d  {%s}".formatted(
                post.getType(), author, post.getCaption(),
                post.getLikeCount(), post.getCommentCount(), extra);
    }
}
