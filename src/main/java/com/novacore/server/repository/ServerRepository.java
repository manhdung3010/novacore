package com.novacore.server.repository;

import com.novacore.server.domain.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {

    List<Server> findByOwnerId(Long ownerId);

    /**
     * Servers where the user is owner OR has joined as member.
     */
    @Query("SELECT DISTINCT s FROM Server s WHERE s.owner.id = :userId " +
           "OR EXISTS (SELECT 1 FROM ServerMember sm WHERE sm.server = s AND sm.user.id = :userId)")
    List<Server> findAllByOwnerIdOrMemberUserId(@Param("userId") Long userId);

    /**
     * Servers where the user is owner OR member, with optional name filter and pagination/sort.
     */
    @Query("SELECT DISTINCT s FROM Server s WHERE (s.owner.id = :userId " +
           "OR EXISTS (SELECT 1 FROM ServerMember sm WHERE sm.server = s AND sm.user.id = :userId)) " +
           "AND (COALESCE(:nameSearch, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    Page<Server> findAllByOwnerIdOrMemberUserIdWithFilter(
            @Param("userId") Long userId,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    /**
     * Servers where the user is neither owner nor member (not joined yet).
     */
    @Query("SELECT s FROM Server s WHERE s.owner.id <> :userId " +
           "AND NOT EXISTS (SELECT 1 FROM ServerMember sm WHERE sm.server = s AND sm.user.id = :userId)")
    List<Server> findAllNotOwnedByAndNotMemberUserId(@Param("userId") Long userId);

    /**
     * Servers not joined, with optional name filter and pagination/sort.
     */
    @Query("SELECT s FROM Server s WHERE s.owner.id <> :userId " +
           "AND NOT EXISTS (SELECT 1 FROM ServerMember sm WHERE sm.server = s AND sm.user.id = :userId) " +
           "AND (COALESCE(:nameSearch, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :nameSearch, '%')))")
    Page<Server> findAllNotOwnedByAndNotMemberUserIdWithFilter(
            @Param("userId") Long userId,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);
}

