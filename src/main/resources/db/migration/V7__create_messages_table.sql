-- Migration: Create messages table
-- Description: Core text message storage
-- Author: NovaCore

CREATE TABLE IF NOT EXISTS messages (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    author_id  BIGINT,
    content    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at  TIMESTAMP NULL,
    CONSTRAINT fk_messages_channel
        FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_messages_channel_created_at
    ON messages (channel_id, created_at DESC);

