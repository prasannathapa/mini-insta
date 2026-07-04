package com.miniinsta.feed;

import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** SQLite-backed {@link FeedRepository} over the materialized {@code feed} table. */
public class SqliteFeedRepository implements FeedRepository {

    private final Connection connection;

    public SqliteFeedRepository(Database database) {
        this.connection = database.connection();
    }

    @Override
    public void addToTimeline(long userId, FeedEntry entry) {
        String sql = "INSERT OR IGNORE INTO feed(user_id, post_id, author_id, created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, entry.postId());
            ps.setLong(3, entry.authorId());
            ps.setString(4, entry.createdAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add to timeline", e);
        }
    }

    @Override
    public List<FeedEntry> timeline(long userId, int limit) {
        List<FeedEntry> entries = new ArrayList<>();
        String sql = "SELECT post_id, author_id, created_at FROM feed WHERE user_id = ? "
                + "ORDER BY created_at DESC, post_id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new FeedEntry(
                            rs.getLong("post_id"),
                            rs.getLong("author_id"),
                            LocalDateTime.parse(rs.getString("created_at"))));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("read timeline", e);
        }
        return entries;
    }
}
