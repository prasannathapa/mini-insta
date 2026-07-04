package com.miniinsta.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The specification for the post model. Read top to bottom, this test says
 * everything a {@link Post} and its three subtypes must do - it is the class
 * you are about to write, described in assertions.
 */
@DisplayName("Post: a sealed family of three, carrying denormalized engagement counters")
class PostTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Nested
    @DisplayName("shared fields live on the base class")
    class SharedFields {

        @Test
        @DisplayName("a post remembers its author, caption and creation time")
        void keepsSharedFields() {
            Post post = new TextPost(7L, "hello", T);
            assertEquals(7L, post.getAuthorId());
            assertEquals("hello", post.getCaption());
            assertEquals(T, post.getCreatedAt());
        }

        @Test
        @DisplayName("every post is handed a unique, positive id")
        void assignsUniqueIds() {
            Post a = new TextPost(1L, "a", T);
            Post b = new TextPost(1L, "b", T);
            assertTrue(a.getId() > 0, "ids start at 1");
            assertNotEquals(a.getId(), b.getId(), "each post gets its own id");
        }

        @Test
        @DisplayName("a null caption is stored as empty, never null")
        void defaultsNullCaption() {
            assertEquals("", new TextPost(1L, null, T).getCaption());
        }

        @Test
        @DisplayName("creation time is required")
        void requiresCreatedAt() {
            assertThrows(NullPointerException.class, () -> new TextPost(1L, "x", null));
        }
    }

    @Nested
    @DisplayName("denormalized engagement counters")
    class Counters {

        @Test
        @DisplayName("a new post starts with zero likes and comments")
        void startAtZero() {
            Post post = new TextPost(1L, "hi", T);
            assertEquals(0, post.getLikeCount());
            assertEquals(0, post.getCommentCount());
        }

        @Test
        @DisplayName("likes and comments move independently, up and down")
        void incrementAndDecrement() {
            Post post = new TextPost(1L, "hi", T);
            post.incrementLikes();
            post.incrementLikes();
            post.incrementComments();
            post.decrementLikes();
            assertEquals(1, post.getLikeCount());
            assertEquals(1, post.getCommentCount());
        }

        @Test
        @DisplayName("neither counter can be driven below zero")
        void neverGoesNegative() {
            Post post = new TextPost(1L, "hi", T);
            post.decrementLikes();    // already zero
            post.decrementComments(); // already zero
            assertEquals(0, post.getLikeCount());
            assertEquals(0, post.getCommentCount());
        }
    }

    @Nested
    @DisplayName("each subtype knows its own type and describes its own media")
    class Subtypes {

        @Test
        @DisplayName("PhotoPost is a PHOTO and surfaces its image and filter")
        void photo() {
            PhotoPost photo = new PhotoPost(1L, "c", T, "cat.jpg", "clarendon");
            assertEquals(PostType.PHOTO, photo.getType());
            assertEquals("cat.jpg", photo.getImageUrl());
            assertEquals("clarendon", photo.getFilter());
            assertTrue(photo.mediaDescription().contains("cat.jpg"));
        }

        @Test
        @DisplayName("a blank photo filter falls back to \"none\"")
        void photoFilterDefaults() {
            assertEquals("none", new PhotoPost(1L, "c", T, "u.jpg", "   ").getFilter());
        }

        @Test
        @DisplayName("VideoPost is a VIDEO and clamps a negative duration to zero")
        void video() {
            VideoPost video = new VideoPost(1L, "c", T, "v.mp4", -5);
            assertEquals(PostType.VIDEO, video.getType());
            assertEquals("v.mp4", video.getVideoUrl());
            assertEquals(0, video.getDurationSeconds());
        }

        @Test
        @DisplayName("TextPost is a TEXT update carrying no media")
        void text() {
            TextPost text = new TextPost(1L, "c", T);
            assertEquals(PostType.TEXT, text.getType());
            assertEquals("text update", text.mediaDescription());
        }
    }

    @Nested
    @DisplayName("sealing the hierarchy enables exhaustive matching")
    class Sealed {

        @Test
        @DisplayName("a switch over the three permitted subtypes needs no default branch")
        void exhaustiveSwitchNeedsNoDefault() {
            List<Post> everyKind = List.of(
                    new PhotoPost(1L, "c", T, "u.jpg", "none"),
                    new VideoPost(1L, "c", T, "v.mp4", 10),
                    new TextPost(1L, "c", T));

            for (Post post : everyKind) {
                // No default branch: this only compiles because Post is sealed
                // to exactly these three subtypes. Add a fourth and it breaks here.
                String kind = switch (post) {
                    case PhotoPost p -> "photo";
                    case VideoPost v -> "video";
                    case TextPost t -> "text";
                };
                assertNotNull(kind);
            }
        }
    }
}
