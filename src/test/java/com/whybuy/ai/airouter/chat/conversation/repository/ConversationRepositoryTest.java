package com.whybuy.ai.airouter.chat.conversation.repository;

import com.whybuy.ai.airouter.chat.conversation.entity.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    void 저장한_방을_id로_조회할_수_있다() {
        // given
        Conversation saved = conversationRepository.save(new Conversation("room-1", "테스트방"));

        // when
        Conversation found = conversationRepository.findById("room-1").orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("테스트방");
    }

    @Test
    void 방_목록이_마지막_대화시각_최신순으로_정렬된다() {
        // given - 시각을 다르게 해서 3개 저장
        Conversation a = new Conversation("a", "오래된방");
        Conversation b = new Conversation("b", "중간방");
        Conversation c = new Conversation("c", "최신방");

        // updatedAt을 명시적으로 다르게 설정
        a.setUpdatedAt(LocalDateTime.now().minusHours(2));
        b.setUpdatedAt(LocalDateTime.now().minusHours(1));
        c.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(a);
        conversationRepository.save(b);
        conversationRepository.save(c);

        // when
        List<Conversation> result = conversationRepository.findAllByOrderByUpdatedAtDesc();

        // then - 최신방 → 중간방 → 오래된방 순서여야 함
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("최신방");
        assertThat(result.get(1).getTitle()).isEqualTo("중간방");
        assertThat(result.get(2).getTitle()).isEqualTo("오래된방");
    }

    @Test
    void 방을_삭제하면_조회되지_않는다() {
        // given
        conversationRepository.save(new Conversation("room-1", "삭제될방"));

        // when
        conversationRepository.deleteById("room-1");

        // then
        assertThat(conversationRepository.findById("room-1")).isEmpty();
    }
}