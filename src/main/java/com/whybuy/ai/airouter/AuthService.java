package com.whybuy.ai.airouter.user.service;

import com.whybuy.ai.airouter.config.jwt.JwtProvider;
import com.whybuy.ai.airouter.user.dto.LoginRequest;
import com.whybuy.ai.airouter.user.dto.SignupRequest;
import com.whybuy.ai.airouter.user.dto.TokenResponse;
import com.whybuy.ai.airouter.user.entity.User;
import com.whybuy.ai.airouter.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 회원가입
    public void signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 비밀번호 암호화해서 저장
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname());
        userRepository.save(user);
    }

    // 로그인
    public TokenResponse login(LoginRequest request) {
        // 이메일로 사용자 찾기
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 확인 (입력 비번 vs 저장된 암호화 비번)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 발급
        String token = jwtProvider.createToken(user.getEmail());
        return new TokenResponse(token, user.getNickname());
    }
}