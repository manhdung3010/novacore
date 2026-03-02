package com.novacore.server.domain;

import com.novacore.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Server membership: user joined a server (in addition to owner).
 * Owner is stored on Server.owner; this entity represents joined members.
 */
@Entity
@Table(name = "server_members", uniqueConstraints = {
    @UniqueConstraint(name = "uq_server_members_server_user", columnNames = {"server_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, foreignKey = @ForeignKey(name = "fk_server_members_server"))
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_server_members_user"))
    private User user;

    @Column(length = 64)
    private String nickname;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
