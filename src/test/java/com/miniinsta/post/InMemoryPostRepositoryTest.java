package com.miniinsta.post;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryPostRepositoryTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void findsPostsByAuthor() {
        PostRepository repo = new InMemoryPostRepository();
        repo.save(new TextPost(1L, "a", T));
        repo.save(new TextPost(1L, "b", T));
        repo.save(new TextPost(2L, "c", T));

        assertEquals(2, repo.findByAuthor(1L).size());
        assertEquals(3, repo.findAll().size());
    }
}
