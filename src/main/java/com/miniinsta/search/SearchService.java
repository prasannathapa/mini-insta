package com.miniinsta.search;

import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostRepository;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Search over users and hashtags. Like the other consumers it OBSERVES
 * PostCreated - on a new post it reads the caption and updates the inverted
 * index, so indexing is decoupled from posting (and could run in its own
 * service behind the event bus).
 */
public class SearchService {

    private final HashtagIndex index;
    private final PostRepository posts;
    private final UserService users;

    public SearchService(HashtagIndex index, PostRepository posts, UserService users) {
        this.index = index;
        this.posts = posts;
        this.users = users;
    }

    public void onPostCreated(PostCreated event) {
        posts.findById(event.postId())
                .ifPresent(post -> index.index(post.getId(), Hashtags.extract(post.getCaption())));
    }

    public List<Post> byHashtag(String tag) {
        String normalized = (tag.startsWith("#") ? tag.substring(1) : tag).toLowerCase();
        return index.postsFor(normalized).stream()
                .map(posts::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> usersMatching(String query) {
        String q = query.toLowerCase();
        return users.all().stream()
                .filter(user -> user.getUsername().contains(q) || user.getFullName().toLowerCase().contains(q))
                .toList();
    }
}
