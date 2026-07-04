package com.miniinsta.app;

import com.miniinsta.feed.ChronologicalFeedStrategy;
import com.miniinsta.feed.FeedRepository;
import com.miniinsta.feed.FeedService;
import com.miniinsta.feed.InMemoryFeedRepository;
import com.miniinsta.feed.SqliteFeedRepository;
import com.miniinsta.graph.FollowRepository;
import com.miniinsta.graph.GraphService;
import com.miniinsta.graph.InMemoryFollowRepository;
import com.miniinsta.graph.SqliteFollowRepository;
import com.miniinsta.messaging.InMemoryMessageRepository;
import com.miniinsta.messaging.MessagingService;
import com.miniinsta.notification.InMemoryNotificationRepository;
import com.miniinsta.notification.NotificationRepository;
import com.miniinsta.notification.NotificationService;
import com.miniinsta.notification.SqliteNotificationRepository;
import com.miniinsta.notification.channel.ConsoleNotificationChannel;
import com.miniinsta.notification.channel.NotificationChannel;
import com.miniinsta.platform.cache.Cache;
import com.miniinsta.platform.cache.InMemoryCache;
import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;
import com.miniinsta.platform.events.EventBus;
import com.miniinsta.platform.events.InProcessEventBus;
import com.miniinsta.platform.events.PostCreated;
import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.Post;
import com.miniinsta.post.PostRepository;
import com.miniinsta.post.PostService;
import com.miniinsta.post.SqlitePostRepository;
import com.miniinsta.search.InMemoryHashtagIndex;
import com.miniinsta.search.SearchService;
import com.miniinsta.story.InMemoryStoryRepository;
import com.miniinsta.story.StoryService;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.SqliteUserRepository;
import com.miniinsta.user.UserRepository;
import com.miniinsta.user.UserService;
import com.miniinsta.util.IdGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.List;

/**
 * The application's composition root, implemented as a Singleton.
 *
 * <p>It picks the concrete adapters and wires repositories -&gt; services -&gt;
 * facade. Set {@code -Dmini.store=sqlite} to run the whole app on the SQLite
 * adapters instead of the in-memory ones - that swap is the payoff of depending
 * on ports: not one line of service code changes. The default is in-memory, so
 * a class demo starts from a clean slate every run.</p>
 */
public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final InstagramService instagram;

    private AppContext() {
        Clock clock = Clock.systemDefaultZone();
        EventBus eventBus = new InProcessEventBus();

        boolean useSqlite = "sqlite".equalsIgnoreCase(System.getProperty("mini.store", "memory"));

        UserRepository userRepository;
        PostRepository postRepository;
        FollowRepository followRepository;
        NotificationRepository notificationRepository;
        FeedRepository feedRepository;

        if (useSqlite) {
            Database database = openDatabase();
            resumeIdSequences(database); // continue ids past what's already stored
            userRepository = new SqliteUserRepository(database);
            postRepository = new SqlitePostRepository(database);
            followRepository = new SqliteFollowRepository(database);
            notificationRepository = new SqliteNotificationRepository(database);
            feedRepository = new SqliteFeedRepository(database);
        } else {
            userRepository = new InMemoryUserRepository();
            postRepository = new InMemoryPostRepository();
            followRepository = new InMemoryFollowRepository();
            notificationRepository = new InMemoryNotificationRepository();
            feedRepository = new InMemoryFeedRepository();
        }

        NotificationChannel notificationChannel = new ConsoleNotificationChannel();

        UserService userService = new UserService(userRepository, clock);
        GraphService graphService = new GraphService(followRepository, clock);
        PostService postService = new PostService(postRepository, eventBus, clock);
        NotificationService notificationService = new NotificationService(
                notificationRepository, graphService, userService, notificationChannel, clock);
        Cache<Long, List<Post>> feedCache = new InMemoryCache<>();
        FeedService feedService = new FeedService(feedRepository, postRepository, graphService,
                feedCache, new ChronologicalFeedStrategy(), clock);

        // Stories, search and DMs use in-memory adapters; their SQLite versions
        // would follow the same pattern as the five repositories above.
        StoryService storyService = new StoryService(new InMemoryStoryRepository(), graphService, clock);
        SearchService searchService = new SearchService(new InMemoryHashtagIndex(), postRepository, userService);
        MessagingService messagingService = new MessagingService(new InMemoryMessageRepository(), clock);

        // Three independent consumers all react to the same PostCreated event.
        eventBus.subscribe(PostCreated.class, feedService::onPostCreated);
        eventBus.subscribe(PostCreated.class, notificationService::onPostCreated);
        eventBus.subscribe(PostCreated.class, searchService::onPostCreated);

        this.instagram = new InstagramService(userService, graphService, postService,
                notificationService, feedService, storyService, searchService, messagingService);
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public InstagramService instagram() {
        return instagram;
    }

    private static Database openDatabase() {
        String path = System.getProperty("mini.db", "data/mini-instagram.db");
        try {
            Path parent = Path.of(path).toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new DataAccessException("create data directory for " + path, e);
        }
        return Database.file(path);
    }

    private static void resumeIdSequences(Database database) {
        resume(database, "user", "users");
        resume(database, "post", "posts");
        resume(database, "comment", "comments");
        resume(database, "notification", "notifications");
    }

    private static void resume(Database database, String sequence, String table) {
        try (Statement st = database.connection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + table)) {
            if (rs.next()) {
                IdGenerator.seed(sequence, rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException("resume id sequence from " + table, e);
        }
    }
}
