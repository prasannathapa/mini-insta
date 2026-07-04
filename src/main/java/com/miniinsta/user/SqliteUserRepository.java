package com.miniinsta.user;

import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed {@link UserRepository}. Implements the exact same port as
 * {@link InMemoryUserRepository}, so it passes the same contract test and can be
 * swapped in at the composition root with no change anywhere else.
 */
public class SqliteUserRepository implements UserRepository {

    private final Connection connection;

    public SqliteUserRepository(Database database) {
        this.connection = database.connection();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(id, username, full_name, bio, created_at) VALUES(?,?,?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET username=excluded.username, "
                + "full_name=excluded.full_name, bio=excluded.bio";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getBio());
            ps.setString(5, user.getCreatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("save user", e);
        }
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            return single(ps);
        } catch (SQLException e) {
            throw new DataAccessException("find user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username.trim().toLowerCase());
            return single(ps);
        } catch (SQLException e) {
            throw new DataAccessException("find user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("find all users", e);
        }
        return users;
    }

    private Optional<User> single(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return User.fromStorage(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("bio"),
                LocalDateTime.parse(rs.getString("created_at")));
    }
}
