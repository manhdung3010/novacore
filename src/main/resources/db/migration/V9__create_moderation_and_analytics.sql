-- Migration: Create moderation & analytics tables
-- Description: Invites, bans, audit logs, voice analytics
-- Author: NovaCore

-- ===== MODERATION & SUPPORT =====
CREATE TABLE IF NOT EXISTS server_invites (
    id          BIGSERIAL PRIMARY KEY,
    server_id   BIGINT NOT NULL,
    code        VARCHAR(32) NOT NULL,
    created_by  BIGINT,
    uses        INT NOT NULL DEFAULT 0,
    max_uses    INT NULL,
    expire_at   TIMESTAMP NULL,
    temporary   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_server_invites_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_server_invites_creator
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_server_invites_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS bans (
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    reason     TEXT,
    banned_by  BIGINT,
    expires_at TIMESTAMP NULL,
    CONSTRAINT fk_bans_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_bans_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bans_banned_by
        FOREIGN KEY (banned_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_bans_server_user UNIQUE (server_id, user_id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGINT NOT NULL,
    actor_id   BIGINT,
    action     VARCHAR(64) NOT NULL,
    target_id  BIGINT,
    changes    JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_server_created_at
    ON audit_logs (server_id, created_at DESC);

-- ===== ANALYTICS =====
CREATE TABLE IF NOT EXISTS voice_speaking_logs (
    id             BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    started_at     TIMESTAMP NOT NULL,
    ended_at       TIMESTAMP NOT NULL,
    CONSTRAINT fk_voice_speaking_logs_participant
        FOREIGN KEY (participant_id) REFERENCES voice_participants(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_vsl_participant_time
    ON voice_speaking_logs (participant_id, started_at);

