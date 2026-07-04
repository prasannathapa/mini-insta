package com.miniinsta.app;

import com.miniinsta.graph.FollowRepository;
import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PostRepository;
import com.miniinsta.post.PostService;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.UserRepository;
import com.miniinsta.user.UserService;

import java.time.Clock;

/**
 * The application's composition root, implemented as a Singleton.
 *
 * <p>This is the one place that (1) picks concrete adapters and (2) wires the
 * dependency graph: repositories -&gt; services -&gt; facade. Everything else
 * receives its collaborators through its constructor and depends only on
 * interfaces, so tests wire their own graph with fakes and never touch this
 * class.</p>
 *
 * <p>A Singleton fits a composition root - it is genuinely created once at
 * start-up. Elsewhere a Singleton is usually a hidden global and an
 * anti-pattern.</p>
 */
public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final InstagramService instagram;

    private AppContext() {
        // A real clock in production; tests inject Clock.fixed(...) for
        // deterministic time. This is the only place the system clock is read.
        Clock clock = Clock.systemDefaultZone();

        // (1) Choose adapters. Swap these for the SQLite versions in step 9.
        UserRepository userRepository = new InMemoryUserRepository();
        PostRepository postRepository = new InMemoryPostRepository();
        FollowRepository followRepository = new InMemoryFollowRepository();

        // (2) Wire services onto the ports (constructor injection = DIP).
        UserService userService = new UserService(userRepository, clock);
        GraphService graphService = new GraphService(followRepository, clock);
        PostService postService = new PostService(postRepository, clock);

        // (3) Expose one facade to the outside world.
        this.instagram = new InstagramService(userService, graphService, postService);
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public InstagramService instagram() {
        return instagram;
    }
}
