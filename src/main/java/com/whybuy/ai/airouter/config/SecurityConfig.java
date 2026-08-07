package com.whybuy.ai.airouter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 비밀번호 암호화 도구
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API라 CSRF 비활성화 (JWT 방식이라 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 안 씀 (JWT라 상태 없음)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 접근 규칙
                .authorizeHttpRequests(auth -> auth
                        // 회원가입/로그인은 인증 없이 허용
                        .requestMatchers("/auth/**").permitAll()
                        // Swagger 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 채팅 화면(정적 파일) 허용
                        .requestMatchers("/", "/chat.html", "/stream.html").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}