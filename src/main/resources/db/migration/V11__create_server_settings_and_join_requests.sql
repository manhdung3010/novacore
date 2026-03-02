-- Migration: Server settings + Join requests (approval flow)
-- Description: require_approval, welcome message/channel, join requests
-- Author: NovaCore

-- ===== SERVER SETTINGS (1-1 with server) =====
CREATE TABLE IF NOT EXISTS server_settings (
    server_id            BIGINT PRIMARY KEY,
    require_approval     BOOLEAN NOT NULL DEFAULT FALSE,
    welcome_message      TEXT NULL,
    welcome_channel_id   BIGINT NULL,
    allow_invite_role    VARCHAR(32) NOT NULL DEFAULT 'members',
    default_role_id      BIGINT NULL,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_server_settings_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_server_settings_welcome_channel
        FOREIGN KEY (welcome_channel_id) REFERENCES channels(id) ON DELETE SET NULL,
    CONSTRAINT fk_server_settings_default_role
        FOREIGN KEY (default_role_id) REFERENCES roles(id) ON DELETE SET NULL,
    CONSTRAINT chk_allow_invite_role
        CHECK (allow_invite_role IN ('owner_only', 'members', 'none'))
);

COMMENT ON COLUMN server_settings.allow_invite_role IS 'owner_only | members | none';
COMMENT ON COLUMN server_settings.welcome_message IS 'Template e.g. Welcome {username}!';

-- ===== JOIN REQUESTS (when require_approval = true) =====
CREATE TABLE IF NOT EXISTS server_join_requests (
    id                     BIGSERIAL PRIMARY KEY,
    server_id              BIGINT NOT NULL,
    user_id                BIGINT NOT NULL,
    status                 VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    message                TEXT NULL,
    invited_by_invite_id   BIGINT NULL,
    reviewed_by            BIGINT NULL,
    reviewed_at            TIMESTAMP NULL,
    review_note             TEXT NULL,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_join_requests_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_requests_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_requests_invite
        FOREIGN KEY (invited_by_invite_id) REFERENCES server_invites(id) ON DELETE SET NULL,
    CONSTRAINT fk_join_requests_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_join_request_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Only one PENDING per (server, user). Same user may re-request after REJECTED (new row).
CREATE UNIQUE INDEX IF NOT EXISTS idx_join_requests_server_user_pending
    ON server_join_requests (server_id, user_id)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_join_requests_server_status
    ON server_join_requests (server_id, status);

CREATE INDEX IF NOT EXISTS idx_join_requests_user
    ON server_join_requests (user_id);
