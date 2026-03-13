package com.novacore.chat.repository;

import com.novacore.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    boolean existsByMessageUuid(UUID messageUuid);

    Slice<ChatMessage> findByChannel_IdOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    Slice<ChatMessage> findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long channelId,
            Instant before,
            Pageable pageable);
}

