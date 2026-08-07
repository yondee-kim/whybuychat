package com.whybuy.ai.airouter.chat.conversation.service;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    // 새 대화방 생성
    public Conversation createConversation(String title) {
        // 고유한 방 id 생성
        String id = UUID.randomUUID().toString();
        String roomTitle = (title == null || title.isBlank()) ? "새 대화" : title;
        Conversation conversation = new Conversation(id, roomTitle);
        return conversationRepository.save(conversation);
    }

    // 전체 방 목록 (최근 대화순)
    public List<Conversation> getConversations() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc();
    }

    // 대화가 오갈 때 해당 방의 '마지막 시각' 갱신
    public void touch(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.touch();
            conversationRepository.save(c);
        });
    }
}