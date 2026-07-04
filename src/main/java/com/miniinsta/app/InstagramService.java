package com.miniinsta.app;

import com.miniinsta.feed.FeedRankingStrategy;
import com.miniinsta.feed.FeedService;
import com.miniinsta.graph.GraphService;
import com.miniinsta.messaging.DirectMessage;
import com.miniinsta.messaging.MessagingService;
import com.miniinsta.notification.Notification;
import com.miniinsta.notification.NotificationService;
import com.miniinsta.post.Comment;
import com.miniinsta.search.SearchService;
import com.miniinsta.story.Story;
import com.miniinsta.story.StoryService;
import com.miniinsta.post.PhotoPost;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostService;
import com.miniinsta.post.TextPost;
import com.miniinsta.post.VideoPost;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * The FACADE. The console (and any future REST controller) talks only to this
 * one class; it hides the fact that a "like" or a "follow" actually fans out
 * across several bounded contexts. It also holds the console session (the
 * currently logged-in user) so callers pass usernames and post ids, not actor
 * ids, everywhere.
 *
 * <p>The facade is where <b>cross-context orchestration</b> lives: e.g.
 * {@link #follow(String)} resolves a username in the user context, then records
 * the edge in the graph context. Each individual service stays ignorant of the
 * others.</p>
 */
public class InstagramService {

    private final UserService users;
    private final GraphService graph;
    private final PostService posts;
    private final NotificationService notifications;
    private final FeedService feed;
    private final StoryService stories;
    private final SearchService search;
    private final MessagingService messaging;

    private User currentUser;

    public InstagramService(UserService users, GraphService graph, PostService posts,
                            NotificationService notifications, FeedService feed,
                            StoryService stories, SearchService search, MessagingService messaging) {
        this.users = users;
        this.graph = graph;
        this.posts = posts;
        this.notifications = notifications;
        this.feed = feed;
        this.stories = stories;
        this.search = search;
        this.messaging = messaging;
    }

    // --- session ------------------------------------------------------------

    public User register(String username, String fullName) {
        User user = users.register(username, fullName);
        currentUser = user;
        return user;
    }

    public Optional<User> login(String username) {
        Optional<User> found = users.findByUsername(username);
        found.ifPresent(user -> currentUser = user);
        return found;
    }

    public void logout() {
        currentUser = null;
    }

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    // --- social graph -------------------------------------------------------

    public boolean follow(String username) {
        return graph.follow(requireLogin(), resolve(username).getId());
    }

    public boolean unfollow(String username) {
        return graph.unfollow(requireLogin(), resolve(username).getId());
    }

    public boolean isFollowing(String username) {
        return graph.isFollowing(requireLogin(), resolve(username).getId());
    }

    public List<User> following() {
        return resolveAll(graph.followeesOf(requireLogin()));
    }

    public List<User> followers() {
        return resolveAll(graph.followersOf(requireLogin()));
    }

    // --- posting & engagement ----------------------------------------------

    public PhotoPost postPhoto(String caption, String imageUrl, String filter) {
        return posts.postPhoto(requireLogin(), caption, imageUrl, filter);
    }

    public VideoPost postVideo(String caption, String videoUrl, int durationSeconds) {
        return posts.postVideo(requireLogin(), caption, videoUrl, durationSeconds);
    }

    public TextPost postText(String caption) {
        return posts.postText(requireLogin(), caption);
    }

    public boolean like(long postId) {
        return posts.like(postId, requireLogin());
    }

    public boolean unlike(long postId) {
        return posts.unlike(postId, requireLogin());
    }

    public Comment comment(long postId, String text) {
        return posts.comment(postId, requireLogin(), text);
    }

    public List<Comment> commentsOf(long postId) {
        return posts.commentsOf(postId);
    }

    // --- feed ---------------------------------------------------------------

    /** The logged-in user's timeline, ordered by the current ranking strategy. */
    public List<Post> feed() {
        return feed.feed(requireLogin(), 50);
    }

    /** Swaps the feed ranking algorithm at runtime (Strategy). */
    public void setFeedRanking(FeedRankingStrategy strategy) {
        feed.setStrategy(strategy);
    }

    public String feedRanking() {
        return feed.strategyName();
    }

    // --- notifications ------------------------------------------------------

    /** The logged-in user's notification inbox, most recent first. */
    public List<Notification> notifications() {
        return notifications.inbox(requireLogin());
    }

    // --- stories ------------------------------------------------------------

    public Story postStory(String content) {
        return stories.post(requireLogin(), content);
    }

    /** Active stories from the people the logged-in user follows (plus their own). */
    public List<Story> stories() {
        return stories.timelineFor(requireLogin());
    }

    // --- search -------------------------------------------------------------

    public List<User> searchUsers(String query) {
        return search.usersMatching(query);
    }

    public List<Post> searchHashtag(String tag) {
        return search.byHashtag(tag);
    }

    // --- direct messages ----------------------------------------------------

    public DirectMessage sendMessage(String toUsername, String text) {
        return messaging.send(requireLogin(), resolve(toUsername).getId(), text);
    }

    public List<DirectMessage> conversationWith(String username) {
        return messaging.conversation(requireLogin(), resolve(username).getId());
    }

    public Optional<Post> post(long postId) {
        return posts.find(postId);
    }

    public List<Post> postsOf(String username) {
        return posts.byAuthor(resolve(username).getId());
    }

    // --- helpers used by the console to turn ids back into names ------------

    public Optional<User> user(long id) {
        return users.findById(id);
    }

    long requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("no user is logged in");
        }
        return currentUser.getId();
    }

    private User resolve(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("no such user: @" + username));
    }

    private List<User> resolveAll(List<Long> ids) {
        return ids.stream()
                .map(users::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
