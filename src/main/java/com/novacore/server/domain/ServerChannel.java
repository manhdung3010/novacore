package com.novacore.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Channel within a server (text/voice/stage/category).
 * Backed by the "channels" table in the database.
 */
@Entity
@Table(name = "channels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false, foreignKey = @ForeignKey(name = "fk_channels_server"))
    private Server server;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "channel_type")
    private ChannelType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_channels_parent"))
    private ServerChannel parent;

    @Column(nullable = false)
    private int position;

    @Column
    private Integer bitrate;

    @Column(name = "user_limit")
    private Integer userLimit;

    @Column(name = "is_nsfw", nullable = false)
    private boolean nsfw;
}


