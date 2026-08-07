package com.whybuy.ai.airouter.user.controller;

import com.whybuy.ai.airouter.user.dto.LoginRequest;
import com.whybuy.ai.airouter.user.dto.SignupRequest;
import com.whybuy.ai.airouter.user.dto.TokenResponse;
import com.whybuy.ai.airouter.user.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입 - POST /auth/signup
    @PostMapping("/signup")
    public void signup(@RequestBody SignupRequest request) {
        authService.signup(request);
    }

    // 로그인 - POST /auth/login
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}