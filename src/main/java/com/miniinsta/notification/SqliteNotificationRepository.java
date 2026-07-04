package com.miniinsta.notification;

import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** SQLite-backed {@link NotificationRepository}. */
public class SqliteNotificationRepository implements NotificationRepository {

    private final Connection connection;

    public SqliteNotificationRepository(Database database) {
        this.connection = database.connection();
    }

    @Override
    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications(id, recipient_id, type, message, created_at) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, notification.id());
            ps.setLong(2, notification.recipientId());
            ps.setString(3, notification.type().name());
            ps.setString(4, notification.message());
            ps.setString(5, notification.createdAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("save notification", e);
        }
        return notification;
    }

    @Override
    public List<Notification> findByRecipient(long recipientId) {
        List<Notification> inbox = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM notifications WHERE recipient_id = ? ORDER BY created_at DESC, id DESC")) {
            ps.setLong(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    inbox.add(new Notification(
                            rs.getLong("id"),
                            rs.getLong("recipient_id"),
                            NotificationType.valueOf(rs.getString("type")),
                            rs.getString("message"),
                            LocalDateTime.parse(rs.getString("created_at"))));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("find notifications", e);
        }
        return inbox;
    }
}
