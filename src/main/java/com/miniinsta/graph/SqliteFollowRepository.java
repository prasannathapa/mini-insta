package com.miniinsta.graph;

import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** SQLite-backed {@link FollowRepository} over the {@code follows} edge table. */
public class SqliteFollowRepository implements FollowRepository {

    private final Connection connection;

    public SqliteFollowRepository(Database database) {
        this.connection = database.connection();
    }

    @Override
    public boolean add(Follow follow) {
        String sql = "INSERT OR IGNORE INTO follows(follower_id, followee_id, created_at) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, follow.followerId());
            ps.setLong(2, follow.followeeId());
            ps.setString(3, follow.createdAt().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("add follow", e);
        }
    }

    @Override
    public boolean remove(long followerId, long followeeId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM follows WHERE follower_id = ? AND followee_id = ?")) {
            ps.setLong(1, followerId);
            ps.setLong(2, followeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("remove follow", e);
        }
    }

    @Override
    public boolean exists(long followerId, long followeeId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM follows WHERE follower_id = ? AND followee_id = ?")) {
            ps.setLong(1, followerId);
            ps.setLong(2, followeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("follow exists", e);
        }
    }

    @Override
    public List<Long> followeesOf(long userId) {
        return ids("SELECT followee_id FROM follows WHERE follower_id = ?", userId, "followee_id");
    }

    @Override
    public List<Long> followersOf(long userId) {
        return ids("SELECT follower_id FROM follows WHERE followee_id = ?", userId, "follower_id");
    }

    @Override
    public long followerCount(long userId) {
        return count("SELECT COUNT(*) FROM follows WHERE followee_id = ?", userId);
    }

    @Override
    public long followeeCount(long userId) {
        return count("SELECT COUNT(*) FROM follows WHERE follower_id = ?", userId);
    }

    private List<Long> ids(String sql, long key, String column) {
        List<Long> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong(column));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("follow ids", e);
        }
        return result;
    }

    private long count(String sql, long key) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException("follow count", e);
        }
    }
}
