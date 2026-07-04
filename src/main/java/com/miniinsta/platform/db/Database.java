package com.miniinsta.platform.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns a single SQLite {@link Connection} and runs the schema on open.
 *
 * <p>SQLite serializes writes itself, so one shared connection is the simplest
 * correct choice for this app. That single-writer model is also SQLite's ceiling
 * - the honest limit we discuss in the HLD notes: the repository <em>port</em>
 * is what lets you swap in a client-server database that scales writes across
 * machines without touching a line of service code.</p>
 */
public class Database implements AutoCloseable {

    private final Connection connection;

    private Database(String url) {
        try {
            this.connection = DriverManager.getConnection(url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            migrate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to open database at " + url, e);
        }
    }

    /** A file-backed database that persists across runs. */
    public static Database file(String path) {
        return new Database("jdbc:sqlite:" + path);
    }

    /** A throwaway in-memory database - ideal for tests. */
    public static Database inMemory() {
        return new Database("jdbc:sqlite::memory:");
    }

    public Connection connection() {
        return connection;
    }

    private void migrate() throws SQLException {
        for (String statement : loadSchema().split(";")) {
            String ddl = statement.strip();
            if (!ddl.isEmpty()) {
                try (Statement st = connection.createStatement()) {
                    st.execute(ddl);
                }
            }
        }
    }

    private static String loadSchema() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new DataAccessException("schema.sql not found on the classpath", null);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("failed to read schema.sql", e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("failed to close database", e);
        }
    }
}
