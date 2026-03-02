package com.novacore.server.repository;

import com.novacore.server.domain.ServerJoinRequest;
import com.novacore.server.domain.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerJoinRequestRepository extends JpaRepository<ServerJoinRequest, Long> {

    List<ServerJoinRequest> findByServerIdAndStatus(Long serverId, JoinRequestStatus status);

    Optional<ServerJoinRequest> findByServerIdAndUserIdAndStatus(
            Long serverId, Long userId, JoinRequestStatus status);

    boolean existsByServerIdAndUserIdAndStatus(
            Long serverId, Long userId, JoinRequestStatus status);
}
