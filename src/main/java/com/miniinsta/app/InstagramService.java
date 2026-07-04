package com.miniinsta.app;

import com.miniinsta.graph.GraphService;
import com.miniinsta.post.Comment;
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

    private User currentUser;

    public InstagramService(UserService users, GraphService graph, PostService posts) {
        this.users = users;
        this.graph = graph;
        this.posts = posts;
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
