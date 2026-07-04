package com.miniinsta.post;

import java.util.List;
import java.util.Optional;

/**
 * Port for storing and retrieving {@link Post}s. Like {@link com.miniinsta.user.UserRepository}
 * it is a seam the services depend on rather than any concrete store.
 *
 * <p>Engagement operations (likes, comments) are added in the next step when
 * the post service needs them - we grow the interface only as real callers
 * appear, rather than speculating.</p>
 */
public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(long id);

    List<Post> findByAuthor(long authorId);

    List<Post> findAll();
}
