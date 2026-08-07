package com.whybuy.ai.airouter.chat.conversation.controller;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.service.ConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
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
}