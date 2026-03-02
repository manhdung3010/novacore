-- Migration: Indexes for scale (many servers, users, invites)
-- Description: Support list-by-server and list-by-user without full scan
-- Author: NovaCore

-- server_invites: list invites of a server (admin UI, audit)
CREATE INDEX IF NOT EXISTS idx_server_invites_server_id
    ON server_invites (server_id);

-- server_invites: cleanup expired (job) or filter valid by server
CREATE INDEX IF NOT EXISTS idx_server_invites_expire_at
    ON server_invites (expire_at)
    WHERE expire_at IS NOT NULL;

-- server_join_requests: "my pending requests" (user_id + status)
CREATE INDEX IF NOT EXISTS idx_join_requests_user_status
    ON server_join_requests (user_id, status);

-- server_join_requests: list by server + created (pagination, admin)
CREATE INDEX IF NOT EXISTS idx_join_requests_server_created
    ON server_join_requests (server_id, created_at DESC);
