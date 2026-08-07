package com.whybuy.ai.airouter.chat.conversation.repository;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    // 모든 방을 '마지막 대화 시각' 최신순으로 조회
    List<Conversation> findAllByOrderByUpdatedAtDesc();
}