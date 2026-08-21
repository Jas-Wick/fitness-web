package com.fitness.controller;

import com.fitness.common.result.Result;
import com.fitness.dto.LoginRequest;
import com.fitness.dto.RefreshRequest;
import com.fitness.dto.RegisterRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.AuthService;
import com.fitness.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = SecurityUtil.getUserId();
        if (userId != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(userId, authHeader.substring(7));
        }
        return Result.success();
    }
}
