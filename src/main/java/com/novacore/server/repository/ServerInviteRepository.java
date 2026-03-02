package com.novacore.server.repository;

import com.novacore.server.domain.ServerInvite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerInviteRepository extends JpaRepository<ServerInvite, Long> {

    Optional<ServerInvite> findByCode(String code);

    List<ServerInvite> findByServerIdOrderByIdDesc(Long serverId, Pageable pageable);
}
