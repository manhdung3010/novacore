package com.novacore.server.repository;

import com.novacore.server.domain.ServerChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing ServerChannel entities.
 * Provides custom queries for finding channels by server and position.
 */
@Repository
public interface ServerChannelRepository extends JpaRepository<ServerChannel, Long> {

    /**
     * Find all channels for a specific server, ordered by position.
     *
     * @param serverId the server ID
     * @return list of channels sorted by position
     */
    List<ServerChannel> findByServerIdOrderByPosition(Long serverId);

    /**
     * Check if a channel exists for a specific server.
     *
     * @param channelId the channel ID
     * @param serverId  the server ID
     * @return true if channel exists, false otherwise
     */
    boolean existsByIdAndServerId(Long channelId, Long serverId);
}

