package com.whybuy.ai.airouter.chat.conversation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
public class Conversation {

    @Id
    private String id;              // 방 식별자 = conversationId

    @Column(nullable = false)
    private String title;           // 방 이름

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;     // 방 주인 (이 방을 만든 사용자의 email)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 방 생성용 편의 생성자
    public Conversation(String id, String title, String ownerEmail) {
        this.id = id;
        this.title = title;
        this.ownerEmail = ownerEmail;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 대화가 오갈 때 마지막 시각 갱신용
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}