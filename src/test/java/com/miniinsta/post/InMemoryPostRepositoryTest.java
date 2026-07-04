package com.miniinsta.post;

import com.miniinsta.user.InMemoryUserRepository;

/** Runs the {@link PostRepositoryContract} against the in-memory adapters. */
class InMemoryPostRepositoryTest extends PostRepositoryContract {

    @Override
    protected World newWorld() {
        return new World(new InMemoryUserRepository(), new InMemoryPostRepository());
    }
}
