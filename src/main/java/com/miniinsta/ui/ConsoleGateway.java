package com.miniinsta.ui;

import com.miniinsta.app.InstagramService;
import com.miniinsta.feed.ChronologicalFeedStrategy;
import com.miniinsta.feed.EngagementFeedStrategy;
import com.miniinsta.messaging.DirectMessage;
import com.miniinsta.notification.Notification;
import com.miniinsta.post.Post;
import com.miniinsta.story.Story;
import com.miniinsta.user.User;
import com.miniinsta.user.UsernameTakenException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * The console front end - the one and only caller of {@link InstagramService}.
 * In a web app this class would be the REST controllers sitting behind a load
 * balancer; here it is a text menu. It reads a command, asks the facade to do
 * the work, and prints the result. It contains no business logic itself.
 */
public class ConsoleGateway {

    private final InstagramService app;
    private final Scanner in;
    private boolean chronological = true;

    public ConsoleGateway(InstagramService app, Scanner in) {
        this.app = app;
        this.in = in;
    }

    public void run() {
        System.out.println("""
                ============================================
                        Welcome to Mini Instagram
                ============================================
                Try signing in as @alice, @bob or @carol.""");
        boolean running = true;
        while (running) {
            Optional<User> me = app.currentUser();
            System.out.println();
            System.out.println(menu(me.orElse(null)));
            Optional<String> choice = prompt("> ");
            if (choice.isEmpty()) {
                break; // end of input
            }
            running = me.isEmpty()
                    ? handleLoggedOut(choice.get())
                    : handleLoggedIn(choice.get());
        }
        System.out.println("\nBye!");
    }

    private String menu(User me) {
        if (me == null) {
            return """
                    [ not signed in ]
                      1) Register     2) Login     0) Exit""";
        }
        return "[ @" + me.getUsername() + " ]  feed ranking: " + app.feedRanking() + "\n"
                + """
                  1) View feed          2) Create post
                  3) Post a story       4) View stories
                  5) Search             6) Follow / unfollow
                  7) Like a post        8) Comment on a post
                  9) Direct messages   10) Notifications
                 11) My profile        12) Toggle feed ranking
                  0) Log out""";
    }

    private boolean handleLoggedOut(String choice) {
        switch (choice) {
            case "1" -> register();
            case "2" -> login();
            case "0" -> {
                return false;
            }
            default -> System.out.println("  (pick 1, 2 or 0)");
        }
        return true;
    }

    private boolean handleLoggedIn(String choice) {
        try {
            switch (choice) {
                case "1" -> viewFeed();
                case "2" -> createPost();
                case "3" -> postStory();
                case "4" -> viewStories();
                case "5" -> search();
                case "6" -> followOrUnfollow();
                case "7" -> likePost();
                case "8" -> commentOnPost();
                case "9" -> directMessages();
                case "10" -> notifications();
                case "11" -> profile();
                case "12" -> toggleRanking();
                case "0" -> {
                    app.logout();
                    System.out.println("  logged out");
                }
                default -> System.out.println("  (unknown option)");
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
        return true;
    }

    // --- actions ------------------------------------------------------------

    private void register() {
        String username = prompt("  username: ").orElse("");
        String fullName = prompt("  full name: ").orElse("");
        try {
            User user = app.register(username, fullName);
            System.out.println("  registered and signed in as @" + user.getUsername());
        } catch (UsernameTakenException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    private void login() {
        String username = prompt("  username: ").orElse("");
        if (app.login(username).isPresent()) {
            System.out.println("  signed in as @" + username.trim().toLowerCase());
        } else {
            System.out.println("  ! no such user");
        }
    }

    private void viewFeed() {
        List<Post> feed = app.feed();
        if (feed.isEmpty()) {
            System.out.println("  your feed is empty - follow someone or post something");
            return;
        }
        System.out.println("  --- your feed (" + app.feedRanking() + ") ---");
        feed.forEach(this::printPost);
    }

    private void createPost() {
        String type = prompt("  type - 1) photo 2) video 3) text: ").orElse("3");
        String caption = prompt("  caption (use #tags): ").orElse("");
        switch (type) {
            case "1" -> app.postPhoto(caption, prompt("  image url: ").orElse("image.jpg"),
                    prompt("  filter: ").orElse("none"));
            case "2" -> app.postVideo(caption, prompt("  video url: ").orElse("video.mp4"),
                    parseInt(prompt("  duration (s): ").orElse("30"), 30));
            default -> app.postText(caption);
        }
        System.out.println("  posted!");
    }

    private void postStory() {
        app.postStory(prompt("  story text: ").orElse(""));
        System.out.println("  story posted (expires in 24h)");
    }

    private void viewStories() {
        List<Story> stories = app.stories();
        if (stories.isEmpty()) {
            System.out.println("  no active stories from people you follow");
            return;
        }
        stories.forEach(story -> System.out.println("  " + author(story.getAuthorId()) + ": " + story.getContent()));
    }

    private void search() {
        String query = prompt("  search (users, or #hashtag): ").orElse("");
        if (query.startsWith("#")) {
            List<Post> hits = app.searchHashtag(query);
            System.out.println("  " + hits.size() + " post(s) tagged " + query + ":");
            hits.forEach(this::printPost);
        } else {
            List<User> users = app.searchUsers(query);
            System.out.println("  users: " + users);
        }
    }

    private void followOrUnfollow() {
        String username = prompt("  username to follow/unfollow: ").orElse("");
        if (app.isFollowing(username)) {
            app.unfollow(username);
            System.out.println("  unfollowed @" + username);
        } else {
            app.follow(username);
            System.out.println("  now following @" + username);
        }
    }

    private void likePost() {
        long id = parseLong(prompt("  post id: ").orElse(""), -1);
        System.out.println(app.like(id) ? "  liked post #" + id : "  (already liked)");
    }

    private void commentOnPost() {
        long id = parseLong(prompt("  post id: ").orElse(""), -1);
        String text = prompt("  comment: ").orElse("");
        app.comment(id, text);
        System.out.println("  comment added");
    }

    private void directMessages() {
        String username = prompt("  conversation with: ").orElse("");
        List<DirectMessage> messages = app.conversationWith(username);
        if (messages.isEmpty()) {
            System.out.println("  no messages yet");
        } else {
            messages.forEach(m -> System.out.println("    " + author(m.senderId()) + ": " + m.text()));
        }
        prompt("  reply (blank to skip): ").filter(text -> !text.isBlank())
                .ifPresent(text -> {
                    app.sendMessage(username, text);
                    System.out.println("  sent");
                });
    }

    private void notifications() {
        List<Notification> inbox = app.notifications();
        if (inbox.isEmpty()) {
            System.out.println("  no notifications");
        } else {
            inbox.forEach(n -> System.out.println("  " + n.type() + ": " + n.message()));
        }
    }

    private void profile() {
        User me = app.currentUser().orElseThrow();
        System.out.printf("  @%s (%s)%n", me.getUsername(), me.getFullName());
        System.out.printf("  following %d, followers %d%n", app.following().size(), app.followers().size());
    }

    private void toggleRanking() {
        chronological = !chronological;
        app.setFeedRanking(chronological ? new ChronologicalFeedStrategy() : new EngagementFeedStrategy());
        System.out.println("  feed ranking is now: " + app.feedRanking());
    }

    // --- helpers ------------------------------------------------------------

    private void printPost(Post post) {
        System.out.printf("  #%d %s [%s] \"%s\"  (%d likes, %d comments)%n",
                post.getId(), author(post.getAuthorId()), post.getType(),
                post.getCaption(), post.getLikeCount(), post.getCommentCount());
    }

    private String author(long userId) {
        return app.user(userId).map(u -> "@" + u.getUsername()).orElse("@?");
    }

    private Optional<String> prompt(String label) {
        System.out.print(label);
        if (!in.hasNextLine()) {
            return Optional.empty();
        }
        return Optional.of(in.nextLine().trim());
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
