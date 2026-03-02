package com.novacore.server.repository;

import com.novacore.server.domain.ServerChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerChannelRepository extends JpaRepository<ServerChannel, Long> {
}

