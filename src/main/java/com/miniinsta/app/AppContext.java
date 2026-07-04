package com.miniinsta.app;

import com.miniinsta.graph.FollowRepository;
import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.notification.InMemoryNotificationRepository;
import com.miniinsta.notification.NotificationRepository;
import com.miniinsta.notification.NotificationService;
import com.miniinsta.notification.channel.ConsoleNotificationChannel;
import com.miniinsta.notification.channel.NotificationChannel;
import com.miniinsta.platform.events.EventBus;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
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

        // The message bus that decouples the contexts.
        EventBus eventBus = new InProcessEventBus();

        // (1) Choose adapters. Swap these for the SQLite versions in step 9.
        UserRepository userRepository = new InMemoryUserRepository();
        PostRepository postRepository = new InMemoryPostRepository();
        FollowRepository followRepository = new InMemoryFollowRepository();
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();

        // (2) Wire services onto the ports (constructor injection = DIP).
        // The notification delivery channel. Console today; swap for an email or
        // SMS adapter (or a composite of several) without touching the service.
        NotificationChannel notificationChannel = new ConsoleNotificationChannel();

        UserService userService = new UserService(userRepository, clock);
        GraphService graphService = new GraphService(followRepository, clock);
        PostService postService = new PostService(postRepository, eventBus, clock);
        NotificationService notificationService = new NotificationService(
                notificationRepository, graphService, userService, notificationChannel, clock);

        // (3) Wire the observers to the bus. This is where "who listens to what"
        // is declared - explicit and in one place.
        eventBus.subscribe(PostCreated.class, notificationService::onPostCreated);

        // (4) Expose one facade to the outside world.
        this.instagram = new InstagramService(userService, graphService, postService, notificationService);
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public InstagramService instagram() {
        return instagram;
    }
}
