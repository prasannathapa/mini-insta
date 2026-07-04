package com.miniinsta.app;

import com.miniinsta.post.InMemoryPostRepository;
import com.miniinsta.post.PostRepository;
import com.miniinsta.user.InMemoryUserRepository;
import com.miniinsta.user.UserRepository;

/**
 * The application's composition root, implemented as a Singleton.
 *
 * <p>This is the <b>one and only place</b> that chooses concrete adapters. Every
 * other class receives its dependencies through its constructor and knows only
 * the port interfaces (Dependency Inversion), which is what keeps them unit
 * testable - a test wires a service with in-memory fakes and never touches this
 * class.</p>
 *
 * <p>Why a Singleton here and (almost) nowhere else? A composition root is
 * genuinely global and created exactly once at start-up, so the pattern fits.
 * Used carelessly elsewhere a Singleton becomes a hidden global variable and an
 * anti-pattern - a point worth making to the class.</p>
 */
public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private AppContext() {
        // Swap these two lines for the SQLite adapters in step 9 and nothing
        // above the ports has to change.
        this.userRepository = new InMemoryUserRepository();
        this.postRepository = new InMemoryPostRepository();
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public UserRepository users() {
        return userRepository;
    }

    public PostRepository posts() {
        return postRepository;
    }
}
