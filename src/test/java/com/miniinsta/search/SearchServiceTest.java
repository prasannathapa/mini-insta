package com.miniinsta.search;

import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PostRequest;
import com.miniinsta.post.PostService;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.User;
import com.miniinsta.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The specification for search. Hashtags are indexed by a subscriber to
 * {@link PostCreated} (the fourth reaction to a new post), and users are found
 * by a fragment of their handle or name.
 */
@DisplayName("SearchService: an inverted index fed by PostCreated, plus user lookup")
class SearchServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    /** The minimal wiring: a bus with search subscribed, and a post service that publishes. */
    private record Wiring(SearchService search, PostService posts, UserService users) {
    }

    private Wiring wire() {
        InProcessEventBus bus = new InProcessEventBus();
        InMemoryPostRepository postRepo = new InMemoryPostRepository();
        UserService users = new UserService(new InMemoryUserRepository(), clock);
        SearchService search = new SearchService(new InMemoryHashtagIndex(), postRepo, users);
        bus.subscribe(PostCreated.class, search::onPostCreated);
        PostService posts = new PostService(postRepo, bus, clock);
        return new Wiring(search, posts, users);
    }

    @Test
    @DisplayName("a hashtag returns every post that used it")
    void indexesHashtagsAcrossPosts() {
        Wiring w = wire();
        User alice = w.users().register("alice", "Alice Anderson");
        w.posts().create(PostRequest.text(alice.getId(), "Beach day #travel #sunny"));
        w.posts().create(PostRequest.text(alice.getId(), "Mountains #travel"));
        assertEquals(2, w.search().byHashtag("travel").size());
        assertEquals(1, w.search().byHashtag("sunny").size());
    }

    @Test
    @DisplayName("a leading # in the query is tolerated")
    void toleratesLeadingHashInQuery() {
        Wiring w = wire();
        User alice = w.users().register("alice", "Alice Anderson");
        w.posts().create(PostRequest.text(alice.getId(), "Beach day #sunny"));
        assertEquals(1, w.search().byHashtag("#sunny").size());
    }

    @Test
    @DisplayName("an unused hashtag returns nothing")
    void unknownHashtagIsEmpty() {
        assertTrue(wire().search().byHashtag("nope").isEmpty());
    }

    @Test
    @DisplayName("users are found by a fragment of their name")
    void findsUsersByNameFragment() {
        Wiring w = wire();
        w.users().register("alice", "Alice Anderson");
        assertEquals(1, w.search().usersMatching("anderson").size());
    }
}
