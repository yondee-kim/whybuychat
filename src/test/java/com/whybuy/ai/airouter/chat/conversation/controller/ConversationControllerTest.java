package com.whybuy.ai.airouter.chat.conversation.controller;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ChatMemory chatMemory;

    @Test
    void 내_방_목록을_조회하면_200과_목록을_반환한다() throws Exception {
        when(conversationService.getConversations("user@test.com"))
                .thenReturn(List.of(
                        new Conversation("id-1", "첫번째방", "user@test.com"),
                        new Conversation("id-2", "두번째방", "user@test.com")
                ));

        mockMvc.perform(get("/conversations").with(user("user@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("첫번째방"));
    }

    @Test
    void 방을_생성하면_200과_생성된_방을_반환한다() throws Exception {
        when(conversationService.createConversation(any(), eq("user@test.com")))
                .thenReturn(new Conversation("new-id", "새방", "user@test.com"));

        mockMvc.perform(post("/conversations").param("title", "새방")
                        .with(user("user@test.com")).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.title").value("새방"));
    }

    @Test
    void 방을_삭제하면_200을_반환하고_서비스가_호출된다() throws Exception {
        mockMvc.perform(delete("/conversations/room-123")
                        .with(user("user@test.com")).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(conversationService).deleteConversation("room-123", "user@test.com");
    }

    @Test
    void 방_이름을_변경하면_200과_변경된_방을_반환한다() throws Exception {
        when(conversationService.rename(eq("room-123"), eq("user@test.com"), eq("바뀐이름")))
                .thenReturn(new Conversation("room-123", "바뀐이름", "user@test.com"));

        mockMvc.perform(patch("/conversations/room-123").param("title", "바뀐이름")
                        .with(user("user@test.com")).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("바뀐이름"));
    }

    @Test
    void 방의_메시지를_조회하면_200을_반환한다() throws Exception {
        when(chatMemory.get("room-123")).thenReturn(List.of());

        mockMvc.perform(get("/conversations/room-123/messages")
                        .with(user("user@test.com")))
                .andExpect(status().isOk());
    }
}