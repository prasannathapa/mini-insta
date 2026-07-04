package com.miniinsta.user;

/** Runs the {@link UserRepositoryContract} against the in-memory adapter. */
class InMemoryUserRepositoryTest extends UserRepositoryContract {

    @Override
    protected UserRepository newRepository() {
        return new InMemoryUserRepository();
    }
}
