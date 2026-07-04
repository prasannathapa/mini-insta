package com.miniinsta.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory {@link PostRepository}. */
public class InMemoryPostRepository implements PostRepository {

    private final Map<Long, Post> byId = new ConcurrentHashMap<>();

    @Override
    public Post save(Post post) {
        byId.put(post.getId(), post);
        return post;
    }

    @Override
    public Optional<Post> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Post> findByAuthor(long authorId) {
        return byId.values().stream()
                .filter(post -> post.getAuthorId() == authorId)
                .toList();
    }

    @Override
    public List<Post> findAll() {
        return new ArrayList<>(byId.values());
    }
}
