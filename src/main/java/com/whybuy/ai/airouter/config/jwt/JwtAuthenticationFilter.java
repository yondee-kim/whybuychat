package com.whybuy.ai.airouter.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 있고 유효하면 → 인증 정보 설정
        if (token != null && jwtProvider.validate(token)) {
            String email = jwtProvider.getEmail(token);

            // "이 요청은 이 email 사용자다"라고 SecurityContext에 등록
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 넘김 (토큰 없어도 일단 통과 → 뒤에서 Security가 판단)
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 꺼내기
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // "Bearer " 제거
        }
        return null;
    }
}