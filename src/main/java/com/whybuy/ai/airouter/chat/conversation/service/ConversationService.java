package com.whybuy.ai.airouter.chat.conversation.service;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.repository.ConversationRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMemory chatMemory;

    public ConversationService(ConversationRepository conversationRepository, ChatMemory chatMemory) {
        this.conversationRepository = conversationRepository;
        this.chatMemory = chatMemory;
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

    // 대화방 삭제 - 방 정보 + 대화 내용 함께 삭제
    @Transactional
    public void deleteConversation(String conversationId) {
        chatMemory.clear(conversationId);                 // 대화 내용 삭제
        conversationRepository.deleteById(conversationId); // 방 정보 삭제
    }

    // 대화방 이름 변경
    public Conversation rename(String conversationId, String newTitle) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + conversationId));
        conversation.setTitle(newTitle);
        return conversationRepository.save(conversation);
    }
}