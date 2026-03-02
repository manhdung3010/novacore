package com.novacore.server.domain;

import com.novacore.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Invite link/code for joining a server.
 * Code is unique; uses/max_uses and expire_at control validity.
 */
@Entity
@Table(name = "server_invites", indexes = {
    @Index(name = "idx_server_invites_server_id", columnList = "server_id"),
    @Index(name = "idx_server_invites_expire_at", columnList = "expire_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, foreignKey = @ForeignKey(name = "fk_server_invites_server"))
    private Server server;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_server_invites_creator"))
    private User createdBy;

    @Column(nullable = false)
    @Builder.Default
    private Integer uses = 0;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean temporary = false;
}
