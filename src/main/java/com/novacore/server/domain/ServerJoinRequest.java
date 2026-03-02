package com.novacore.server.domain;

import com.novacore.server.domain.enums.JoinRequestStatus;
import com.novacore.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request to join a server when require_approval is true.
 * One PENDING per (server, user); user may create new request after REJECTED.
 */
@Entity
@Table(name = "server_join_requests", indexes = {
    @Index(name = "idx_join_requests_server_status", columnList = "server_id, status"),
    @Index(name = "idx_join_requests_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, foreignKey = @ForeignKey(name = "fk_join_requests_server"))
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_join_requests_user"))
    private User user;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(length = 1000)
    private String message;

    @Column(name = "invited_by_invite_id")
    private Long invitedByInviteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(name = "fk_join_requests_reviewed_by"))
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
