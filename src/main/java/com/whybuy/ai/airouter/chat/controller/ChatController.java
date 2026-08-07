package com.whybuy.ai.airouter.chat.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    private final ChatClient ollamaClient;

    public ChatController(ChatClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String conversationId) {
        return ollamaClient.prompt()
                .user(message)
                // 대화 ID에 따라 이력 매칭
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}