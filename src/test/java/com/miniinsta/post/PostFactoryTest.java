package com.miniinsta.post;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PostFactoryTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void buildsTheRightSubtypeForEachPostType() {
        assertInstanceOf(PhotoPost.class, PostFactory.create(PostRequest.photo(1L, "c", "u.jpg", "clarendon"), T));
        assertInstanceOf(VideoPost.class, PostFactory.create(PostRequest.video(1L, "c", "v.mp4", 30), T));
        assertInstanceOf(TextPost.class, PostFactory.create(PostRequest.text(1L, "hi"), T));
    }

    @Test
    void carriesTypeSpecificFields() {
        Post video = PostFactory.create(PostRequest.video(1L, "c", "v.mp4", 30), T);
        assertEquals(30, ((VideoPost) video).getDurationSeconds());
    }

    @Test
    void setsSharedFields() {
        Post post = PostFactory.create(PostRequest.photo(7L, "cap", "u.jpg", "none"), T);
        assertEquals(7L, post.getAuthorId());
        assertEquals("cap", post.getCaption());
    }
}
