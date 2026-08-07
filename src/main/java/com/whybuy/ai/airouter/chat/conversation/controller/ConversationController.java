package com.whybuy.ai.airouter.chat.conversation.controller;

import com.whybuy.ai.airouter.chat.conversation.dto.MessageResponse;
import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.service.ConversationService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final ChatMemory chatMemory;

    public ConversationController(ConversationService conversationService, ChatMemory chatMemory) {
        this.conversationService = conversationService;
        this.chatMemory = chatMemory;
    }

    // 방 목록 조회 - GET /conversations
    @GetMapping
    public List<Conversation> list() {
        return conversationService.getConversations();
    }

    // 새 방 생성 - POST /conversations
    @PostMapping
    public Conversation create(@RequestParam(required = false) String title) {
        return conversationService.createConversation(title);
    }

    // 특정 방의 지난 메시지 조회 - GET /conversations/{id}/messages
    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable String id) {
        List<Message> messages = chatMemory.get(id);
        return messages.stream()
                .map(m -> new MessageResponse(
                        m.getMessageType().getValue(),   // "user" 또는 "assistant"
                        m.getText()))
                .toList();
    }
}