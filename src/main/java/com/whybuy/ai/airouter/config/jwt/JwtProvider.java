package com.whybuy.ai.airouter.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // 토큰 발급 - email을 담아서
    public String createToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)              // 토큰의 주인 (email)
                .issuedAt(now)               // 발급 시각
                .expiration(expiry)          // 만료 시각
                .signWith(key)               // 비밀키로 서명
                .compact();
    }

    // 토큰에서 email 꺼내기
    public String getEmail(String token) {
        return parse(token).getSubject();
    }

    // 토큰 검증 - 유효하면 true
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;  // 위조/만료/형식오류 등
        }
    }

    // 내부 - 토큰 해석 (서명 검증 포함)
    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}