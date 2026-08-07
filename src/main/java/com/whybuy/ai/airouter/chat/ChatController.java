package com.whybuy.ai.airouter.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatClient ollamaClient;

    public ChatController(ChatClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    // 기존 - 한 번에 응답
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

    // 신규 - 스트리밍 응답
    @GetMapping(value = "/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> chatStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String conversationId) {
        return ollamaClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .map(chunk -> chunk.replace(" ", "\u00A0"));  // 공백을 깨지지 않는 공백으로
    }
}