package com.whybuy.ai.airouter.chat.conversation.controller;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ChatMemory chatMemory;

    @Test
    void 방_목록을_조회하면_200과_목록을_반환한다() throws Exception {
        // given
        when(conversationService.getConversations())
                .thenReturn(List.of(
                        new Conversation("id-1", "첫번째방"),
                        new Conversation("id-2", "두번째방")
                ));

        // when & then
        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("첫번째방"));
    }

    @Test
    void 방을_생성하면_200과_생성된_방을_반환한다() throws Exception {
        // given
        when(conversationService.createConversation(any()))
                .thenReturn(new Conversation("new-id", "새방"));

        // when & then
        mockMvc.perform(post("/conversations").param("title", "새방"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.title").value("새방"));
    }

    @Test
    void 방을_삭제하면_200을_반환하고_서비스가_호출된다() throws Exception {
        // when & then
        mockMvc.perform(delete("/conversations/room-123"))
                .andExpect(status().isOk());

        verify(conversationService).deleteConversation("room-123");
    }

    @Test
    void 방_이름을_변경하면_200과_변경된_방을_반환한다() throws Exception {
        // given
        when(conversationService.rename(eq("room-123"), eq("바뀐이름")))
                .thenReturn(new Conversation("room-123", "바뀐이름"));

        // when & then
        mockMvc.perform(patch("/conversations/room-123").param("title", "바뀐이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("바뀐이름"));
    }

    @Test
    void 방의_메시지를_조회하면_200을_반환한다() throws Exception {
        // given
        when(chatMemory.get("room-123")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/conversations/room-123/messages"))
                .andExpect(status().isOk());
    }
}