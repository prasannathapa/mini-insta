package com.miniinsta.post;

import com.miniinsta.platform.db.DataAccessException;
import com.miniinsta.platform.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed {@link PostRepository}. The post aggregate spans three
 * normalized tables - {@code posts}, {@code likes}, {@code comments} - and this
 * adapter maps between them and the sealed {@link Post} hierarchy. Note that the
 * denormalized {@code like_count}/{@code comment_count} columns are written from
 * the {@link Post} object (the service keeps them correct).
 */
public class SqlitePostRepository implements PostRepository {

    private final Connection connection;

    public SqlitePostRepository(Database database) {
        this.connection = database.connection();
    }

    @Override
    public <T extends Post> T save(T post) {
        String sql = "INSERT INTO posts(id, author_id, type, caption, media_url, filter, "
                + "duration_seconds, created_at, like_count, comment_count) VALUES(?,?,?,?,?,?,?,?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET caption=excluded.caption, "
                + "like_count=excluded.like_count, comment_count=excluded.comment_count";
        String mediaUrl = null;
        String filter = null;
        Integer duration = null;
        switch (post) {
            case PhotoPost photo -> {
                mediaUrl = photo.getImageUrl();
                filter = photo.getFilter();
            }
            case VideoPost video -> {
                mediaUrl = video.getVideoUrl();
                duration = video.getDurationSeconds();
            }
            case TextPost ignored -> {
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, post.getId());
            ps.setLong(2, post.getAuthorId());
            ps.setString(3, post.getType().name());
            ps.setString(4, post.getCaption());
            ps.setString(5, mediaUrl);
            ps.setString(6, filter);
            if (duration == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, duration);
            }
            ps.setString(8, post.getCreatedAt().toString());
            ps.setInt(9, post.getLikeCount());
            ps.setInt(10, post.getCommentCount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("save post", e);
        }
        return post;
    }

    @Override
    public Optional<Post> findById(long id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM posts WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("find post by id", e);
        }
    }

    @Override
    public List<Post> findByAuthor(long authorId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM posts WHERE author_id = ?")) {
            ps.setLong(1, authorId);
            return list(ps);
        } catch (SQLException e) {
            throw new DataAccessException("find posts by author", e);
        }
    }

    @Override
    public List<Post> findAll() {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM posts")) {
            return list(ps);
        } catch (SQLException e) {
            throw new DataAccessException("find all posts", e);
        }
    }

    @Override
    public boolean addLike(long postId, long userId, LocalDateTime at) {
        String sql = "INSERT OR IGNORE INTO likes(post_id, user_id, created_at) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            ps.setString(3, at.toString());
            return ps.executeUpdate() > 0; // 0 rows => already liked
        } catch (SQLException e) {
            throw new DataAccessException("add like", e);
        }
    }

    @Override
    public boolean removeLike(long postId, long userId) {
        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM likes WHERE post_id = ? AND user_id = ?")) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("remove like", e);
        }
    }

    @Override
    public boolean isLikedBy(long postId, long userId) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?")) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("is liked by", e);
        }
    }

    @Override
    public Comment addComment(Comment comment) {
        String sql = "INSERT INTO comments(id, post_id, author_id, text, created_at) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, comment.id());
            ps.setLong(2, comment.postId());
            ps.setLong(3, comment.authorId());
            ps.setString(4, comment.text());
            ps.setString(5, comment.createdAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add comment", e);
        }
        return comment;
    }

    @Override
    public List<Comment> commentsOf(long postId) {
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at")) {
            ps.setLong(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getLong("id"),
                            rs.getLong("post_id"),
                            rs.getLong("author_id"),
                            rs.getString("text"),
                            LocalDateTime.parse(rs.getString("created_at"))));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("comments of post", e);
        }
        return comments;
    }

    private List<Post> list(PreparedStatement ps) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                posts.add(map(rs));
            }
        }
        return posts;
    }

    private Post map(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long authorId = rs.getLong("author_id");
        String caption = rs.getString("caption");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        int likeCount = rs.getInt("like_count");
        int commentCount = rs.getInt("comment_count");
        return switch (PostType.valueOf(rs.getString("type"))) {
            case PHOTO -> PhotoPost.fromStorage(id, authorId, caption, createdAt, likeCount, commentCount,
                    rs.getString("media_url"), rs.getString("filter"));
            case VIDEO -> VideoPost.fromStorage(id, authorId, caption, createdAt, likeCount, commentCount,
                    rs.getString("media_url"), rs.getInt("duration_seconds"));
            case TEXT -> TextPost.fromStorage(id, authorId, caption, createdAt, likeCount, commentCount);
        };
    }
}
