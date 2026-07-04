package com.miniinsta.post;

import com.miniinsta.platform.db.Database;
import com.miniinsta.user.SqliteUserRepository;
import org.junit.jupiter.api.AfterEach;

/**
 * Runs the SAME {@link PostRepositoryContract} against the SQLite adapters on a
 * throwaway in-memory database. Green here + green in the in-memory subclass is
 * the proof that the two are substitutable.
 */
class SqlitePostRepositoryTest extends PostRepositoryContract {

    private Database db;

    @Override
    protected World newWorld() {
        db = Database.inMemory();
        return new World(new SqliteUserRepository(db), new SqlitePostRepository(db));
    }

    @AfterEach
    void closeDatabase() {
        if (db != null) {
            db.close();
        }
    }
}
