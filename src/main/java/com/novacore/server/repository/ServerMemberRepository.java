package com.novacore.server.repository;

import com.novacore.server.domain.ServerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerMemberRepository extends JpaRepository<ServerMember, Long> {

    List<ServerMember> findByUserId(Long userId);

    boolean existsByServerIdAndUserId(Long serverId, Long userId);

    Optional<ServerMember> findByServerIdAndUserId(Long serverId, Long userId);
}
