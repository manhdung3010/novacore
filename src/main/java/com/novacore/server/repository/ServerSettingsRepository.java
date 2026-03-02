package com.novacore.server.repository;

import com.novacore.server.domain.ServerSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerSettingsRepository extends JpaRepository<ServerSettings, Long> {

    Optional<ServerSettings> findByServerId(Long serverId);
}
