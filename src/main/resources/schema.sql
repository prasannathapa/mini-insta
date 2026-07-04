-- Mini Instagram schema (SQLite).
--
-- NORMALIZED core (one fact in one place, referential integrity via FKs) with a
-- few DELIBERATE denormalizations for read speed: counters on users/posts and
-- the materialized `feed` table. Those denormalized columns are maintained by
-- the services on write - that upkeep is the cost we trade for fast reads.

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY,
    username        TEXT    NOT NULL UNIQUE,
    full_name       TEXT    NOT NULL,
    bio             TEXT    NOT NULL DEFAULT '',
    created_at      TEXT    NOT NULL,
    follower_count  INTEGER NOT NULL DEFAULT 0,   -- denormalized
    following_count INTEGER NOT NULL DEFAULT 0    -- denormalized
);

CREATE TABLE IF NOT EXISTS follows (
    follower_id INTEGER NOT NULL REFERENCES users(id),
    followee_id INTEGER NOT NULL REFERENCES users(id),
    created_at  TEXT    NOT NULL,
    PRIMARY KEY (follower_id, followee_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_followee ON follows(followee_id);

CREATE TABLE IF NOT EXISTS posts (
    id               INTEGER PRIMARY KEY,
    author_id        INTEGER NOT NULL REFERENCES users(id),
    type             TEXT    NOT NULL,            -- PHOTO | VIDEO | TEXT
    caption          TEXT    NOT NULL,
    media_url        TEXT,
    filter           TEXT,
    duration_seconds INTEGER,
    created_at       TEXT    NOT NULL,
    like_count       INTEGER NOT NULL DEFAULT 0,  -- denormalized
    comment_count    INTEGER NOT NULL DEFAULT 0   -- denormalized
);
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id);

CREATE TABLE IF NOT EXISTS likes (
    post_id    INTEGER NOT NULL REFERENCES posts(id),
    user_id    INTEGER NOT NULL REFERENCES users(id),
    created_at TEXT    NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id         INTEGER PRIMARY KEY,
    post_id    INTEGER NOT NULL REFERENCES posts(id),
    author_id  INTEGER NOT NULL REFERENCES users(id),
    text       TEXT    NOT NULL,
    created_at TEXT    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);

CREATE TABLE IF NOT EXISTS notifications (
    id           INTEGER PRIMARY KEY,
    recipient_id INTEGER NOT NULL REFERENCES users(id),
    type         TEXT    NOT NULL,
    message      TEXT    NOT NULL,
    created_at   TEXT    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id);

-- Materialized per-user timeline (fan-out on write). Reading a feed is one
-- indexed range scan here instead of a join across follows and posts.
CREATE TABLE IF NOT EXISTS feed (
    user_id    INTEGER NOT NULL,
    post_id    INTEGER NOT NULL,
    author_id  INTEGER NOT NULL,
    created_at TEXT    NOT NULL,
    PRIMARY KEY (user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_feed_user ON feed(user_id, created_at);
