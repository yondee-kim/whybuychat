package com.whybuy.ai.airouter.chat.conversation.service;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import com.whybuy.ai.airouter.chat.conversation.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMemory chatMemory;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void 방을_생성하면_제목과_주인이_저장되고_id가_부여된다() {
        // given
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.createConversation("테스트방", "user@test.com");

        // then
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getTitle()).isEqualTo("테스트방");
        assertThat(result.getOwnerEmail()).isEqualTo("user@test.com");
    }

    @Test
    void 제목없이_방을_생성하면_기본이름이_붙는다() {
        // given
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.createConversation("", "user@test.com");

        // then
        assertThat(result.getTitle()).isEqualTo("새 대화");
    }

    @Test
    void 내_방을_삭제하면_대화내용과_방정보가_모두_삭제된다() {
        // given - 내 방이 존재한다고 설정
        Conversation mine = new Conversation("room-1", "내방", "user@test.com");
        when(conversationRepository.findByIdAndOwnerEmail("room-1", "user@test.com"))
                .thenReturn(Optional.of(mine));

        // when
        conversationService.deleteConversation("room-1", "user@test.com");

        // then
        verify(chatMemory).clear("room-1");
        verify(conversationRepository).deleteById("room-1");
    }

    @Test
    void 내_방이_아니면_삭제시_예외가_발생한다() {
        // given - 내 방으로 찾으면 없음(남의 방)
        when(conversationRepository.findByIdAndOwnerEmail("room-1", "user@test.com"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                conversationService.deleteConversation("room-1", "user@test.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 내_방의_이름을_변경할_수_있다() {
        // given
        Conversation mine = new Conversation("room-1", "예전이름", "user@test.com");
        when(conversationRepository.findByIdAndOwnerEmail("room-1", "user@test.com"))
                .thenReturn(Optional.of(mine));
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.rename("room-1", "user@test.com", "새이름");

        // then
        assertThat(result.getTitle()).isEqualTo("새이름");
    }

    @Test
    void 내_방이_아니면_이름변경시_예외가_발생한다() {
        // given
        when(conversationRepository.findByIdAndOwnerEmail("room-1", "user@test.com"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                conversationService.rename("room-1", "user@test.com", "새이름"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}