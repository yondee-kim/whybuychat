package com.whybuy.ai.airouter.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;       // 로그인 식별자 (이메일)

    @Column(nullable = false)
    private String password;    // 암호화되어 저장됨

    @Column(nullable = false)
    private String nickname;    // 사용자를 부르는 이름 (표시용)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }
}