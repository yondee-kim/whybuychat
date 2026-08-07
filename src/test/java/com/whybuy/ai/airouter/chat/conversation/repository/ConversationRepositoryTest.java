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
        conversationRepository.save(new Conversation("room-1", "테스트방", "user@test.com"));

        // when
        Conversation found = conversationRepository.findById("room-1").orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("테스트방");
        assertThat(found.getOwnerEmail()).isEqualTo("user@test.com");
    }

    @Test
    void 내_방만_최신순으로_조회된다() {
        // given - user1의 방 2개, user2의 방 1개
        Conversation a = new Conversation("a", "내방1", "user1@test.com");
        Conversation b = new Conversation("b", "내방2", "user1@test.com");
        Conversation c = new Conversation("c", "남의방", "user2@test.com");

        a.setUpdatedAt(LocalDateTime.now().minusHours(1));
        b.setUpdatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(a);
        conversationRepository.save(b);
        conversationRepository.save(c);

        // when - user1의 방만 조회
        List<Conversation> result =
                conversationRepository.findByOwnerEmailOrderByUpdatedAtDesc("user1@test.com");

        // then - user1 방 2개만, 최신순
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("내방2");
        assertThat(result.get(1).getTitle()).isEqualTo("내방1");
    }

    @Test
    void 방을_삭제하면_조회되지_않는다() {
        // given
        conversationRepository.save(new Conversation("room-1", "삭제될방", "user@test.com"));

        // when
        conversationRepository.deleteById("room-1");

        // then
        assertThat(conversationRepository.findById("room-1")).isEmpty();
    }
}