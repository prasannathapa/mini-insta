package com.miniinsta.user;

import com.miniinsta.platform.db.Database;
import org.junit.jupiter.api.AfterEach;

/** Runs the {@link UserRepositoryContract} against the SQLite adapter. */
class SqliteUserRepositoryTest extends UserRepositoryContract {

    private Database db;

    @Override
    protected UserRepository newRepository() {
        db = Database.inMemory();
        return new SqliteUserRepository(db);
    }

    @AfterEach
    void closeDatabase() {
        if (db != null) {
            db.close();
        }
    }
}
