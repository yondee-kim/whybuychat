package com.whybuy.ai.airouter.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient ollamaClient(
            @Qualifier("ollamaChatModel") ChatModel model,
            ChatMemory chatMemory) {
        return ChatClient.builder(model)
                // 메모리 어드바이저 등록
                // - 이 클라이언트로 오는 모든 요청에 자동으로 이력이 붙음
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}