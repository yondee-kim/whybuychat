package com.whybuy.ai.airouter.chat.conversation.repository;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    // 특정 사용자의 방만, 최근순
    List<Conversation> findByOwnerEmailOrderByUpdatedAtDesc(String ownerEmail);

    // 특정 방이 특정 사용자 것인지 확인용
    Optional<Conversation> findByIdAndOwnerEmail(String id, String ownerEmail);
}