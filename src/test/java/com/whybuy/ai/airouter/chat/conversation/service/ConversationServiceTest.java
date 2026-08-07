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
    void 방을_생성하면_제목이_저장되고_id가_부여된다() {
        // given - Repository의 save는 받은 걸 그대로 돌려주도록 설정
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.createConversation("테스트방");

        // then
        assertThat(result.getId()).isNotBlank();          // id가 생겼는지
        assertThat(result.getTitle()).isEqualTo("테스트방"); // 제목이 맞는지
        verify(conversationRepository).save(any(Conversation.class)); // save가 호출됐는지
    }

    @Test
    void 제목없이_방을_생성하면_기본이름이_붙는다() {
        // given
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.createConversation("");

        // then
        assertThat(result.getTitle()).isEqualTo("새 대화");
    }

    @Test
    void 방을_삭제하면_대화내용과_방정보가_모두_삭제된다() {
        // given
        String id = "room-123";

        // when
        conversationService.deleteConversation(id);

        // then - 두 저장소 삭제가 각각 호출됐는지
        verify(chatMemory).clear(id);
        verify(conversationRepository).deleteById(id);
    }

    @Test
    void 존재하는_방의_이름을_변경할_수_있다() {
        // given - findById가 방을 돌려주도록 설정
        Conversation existing = new Conversation("room-123", "예전이름");
        when(conversationRepository.findById("room-123"))
                .thenReturn(Optional.of(existing));
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Conversation result = conversationService.rename("room-123", "새이름");

        // then
        assertThat(result.getTitle()).isEqualTo("새이름");
    }

    @Test
    void 없는_방의_이름을_변경하면_예외가_발생한다() {
        // given - findById가 빈 값(방 없음)을 돌려주도록
        when(conversationRepository.findById("없는id"))
                .thenReturn(Optional.empty());

        // when & then - 예외가 터지는지 검증
        assertThatThrownBy(() -> conversationService.rename("없는id", "아무이름"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}