package com.miniinsta.post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory {@link PostRepository}: posts, likes and comments kept in maps. */
public class InMemoryPostRepository implements PostRepository {

    private final Map<Long, Post> postsById = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> likesByPost = new ConcurrentHashMap<>();
    private final Map<Long, List<Comment>> commentsByPost = new ConcurrentHashMap<>();

    @Override
    public <T extends Post> T save(T post) {
        postsById.put(post.getId(), post);
        return post;
    }

    @Override
    public java.util.Optional<Post> findById(long id) {
        return java.util.Optional.ofNullable(postsById.get(id));
    }

    @Override
    public List<Post> findByAuthor(long authorId) {
        return postsById.values().stream()
                .filter(post -> post.getAuthorId() == authorId)
                .toList();
    }

    @Override
    public List<Post> findAll() {
        return new ArrayList<>(postsById.values());
    }

    @Override
    public boolean addLike(long postId, long userId, LocalDateTime at) {
        return likesByPost.computeIfAbsent(postId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    @Override
    public boolean removeLike(long postId, long userId) {
        Set<Long> likers = likesByPost.get(postId);
        return likers != null && likers.remove(userId);
    }

    @Override
    public boolean isLikedBy(long postId, long userId) {
        Set<Long> likers = likesByPost.get(postId);
        return likers != null && likers.contains(userId);
    }

    @Override
    public Comment addComment(Comment comment) {
        commentsByPost.computeIfAbsent(comment.postId(), k -> new CopyOnWriteArrayList<>()).add(comment);
        return comment;
    }

    @Override
    public List<Comment> commentsOf(long postId) {
        return new ArrayList<>(commentsByPost.getOrDefault(postId, List.of()));
    }
}
