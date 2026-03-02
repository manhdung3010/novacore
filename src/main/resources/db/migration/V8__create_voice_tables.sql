-- Migration: Create voice session tables
-- Description: Voice sessions and participants
-- Author: NovaCore

CREATE TABLE IF NOT EXISTS voice_sessions (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    region     VARCHAR(64),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at   TIMESTAMP NULL,
    CONSTRAINT fk_voice_sessions_channel
        FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS voice_participants (
    id                  BIGSERIAL PRIMARY KEY,
    session_id          BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at             TIMESTAMP NULL,
    self_muted          BOOLEAN NOT NULL DEFAULT FALSE,
    server_muted        BOOLEAN NOT NULL DEFAULT FALSE,
    self_deafened       BOOLEAN NOT NULL DEFAULT FALSE,
    video_enabled       BOOLEAN NOT NULL DEFAULT FALSE,
    screen_sharing      BOOLEAN NOT NULL DEFAULT FALSE,
    stream_key          VARCHAR NULL,
    speaking_timestamp  TIMESTAMP NULL,
    request_to_speak_at TIMESTAMP NULL,
    CONSTRAINT fk_vp_session
        FOREIGN KEY (session_id) REFERENCES voice_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_vp_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_vp_session_user
    ON voice_participants (session_id, user_id);

CREATE INDEX IF NOT EXISTS idx_vp_session_joined
    ON voice_participants (session_id, joined_at);

