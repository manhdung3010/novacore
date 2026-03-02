-- Migration: Create chat schema (Discord-like) - core layer
-- Description: ENUMs, users.is_bot, servers, roles, channels, permission overwrites
-- Author: NovaCore
-- Note: Builds on existing users & auth migrations (V1-V5)

-- ===== ENUM TYPES =====
DO $$
BEGIN
    CREATE TYPE channel_type AS ENUM ('text', 'voice', 'stage', 'category');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- ===== OPTIONAL: is_bot for users (non-breaking) =====
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_bot BOOLEAN NOT NULL DEFAULT FALSE;

-- ===== SERVER (WORKSPACE) LAYER =====
CREATE TABLE IF NOT EXISTS servers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    owner_id   BIGINT NOT NULL,
    icon_url   TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_servers_owner
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS server_members (
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    nickname   VARCHAR(64),
    joined_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_server_members_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_server_members_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_server_members_server_user UNIQUE (server_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_server_members_server_user
    ON server_members (server_id, user_id);

CREATE INDEX IF NOT EXISTS idx_server_members_server_nickname
    ON server_members (server_id, nickname);

-- ===== ROLES & PERMISSIONS (Discord-style, separate from users.role) =====
CREATE TABLE IF NOT EXISTS roles (
    id               BIGSERIAL PRIMARY KEY,
    server_id        BIGINT NOT NULL,
    name             VARCHAR(64) NOT NULL,
    color            INT,
    position         INT NOT NULL,
    permissions_mask BIGINT NOT NULL,
    hoist            BOOLEAN NOT NULL DEFAULT FALSE,
    mentionable      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT uq_roles_server_name UNIQUE (server_id, name)
);

CREATE INDEX IF NOT EXISTS idx_roles_server_position
    ON roles (server_id, position DESC);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role
    ON user_roles (role_id);

-- ===== CHANNEL HIERARCHY =====
CREATE TABLE IF NOT EXISTS channels (
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGINT NOT NULL,
    name       VARCHAR(128) NOT NULL,
    type       channel_type NOT NULL,
    parent_id  BIGINT NULL,
    position   INT NOT NULL DEFAULT 0,
    bitrate    INT,
    user_limit INT,
    is_nsfw    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_channels_server
        FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_channels_parent
        FOREIGN KEY (parent_id) REFERENCES channels(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_channels_tree
    ON channels (server_id, parent_id, position);

CREATE INDEX IF NOT EXISTS idx_channels_server_type
    ON channels (server_id, type);

-- ===== CHANNEL PERMISSION OVERWRITES =====
CREATE TABLE IF NOT EXISTS channel_permission_overwrites (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    role_id    BIGINT,
    user_id    BIGINT,
    allow_mask BIGINT NOT NULL DEFAULT 0,
    deny_mask  BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cpo_channel
        FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_cpo_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_cpo_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_cpo_role_xor_user CHECK (
        (role_id IS NOT NULL AND user_id IS NULL)
        OR
        (role_id IS NULL AND user_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_cpo_channel
    ON channel_permission_overwrites (channel_id);

CREATE INDEX IF NOT EXISTS idx_cpo_role
    ON channel_permission_overwrites (role_id);

CREATE INDEX IF NOT EXISTS idx_cpo_user
    ON channel_permission_overwrites (user_id);
