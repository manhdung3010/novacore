package com.novacore.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Server-level settings: approval flow, welcome message, invite policy.
 * One-to-one with Server.
 */
@Entity
@Table(name = "server_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerSettings {

    @Id
    @Column(name = "server_id")
    private Long serverId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, insertable = false, updatable = false)
    private Server server;

    @Column(name = "require_approval", nullable = false)
    @Builder.Default
    private boolean requireApproval = false;

    @Column(name = "welcome_message")
    private String welcomeMessage;

    @Column(name = "welcome_channel_id")
    private Long welcomeChannelId;

    @Column(name = "allow_invite_role", nullable = false, length = 32)
    @Builder.Default
    private String allowInviteRole = "members";

    @Column(name = "default_role_id")
    private Long defaultRoleId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
