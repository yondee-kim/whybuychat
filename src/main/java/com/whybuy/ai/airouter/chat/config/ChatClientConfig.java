package com.whybuy.ai.airouter.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient ollamaClient(@Qualifier("ollamaChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    // 나중에 키 넣으면 활성화
    // @Bean
    // public ChatClient anthropicClient(@Qualifier("anthropicChatModel") ChatModel model) {
    //     return ChatClient.builder(model).build();
    // }
}