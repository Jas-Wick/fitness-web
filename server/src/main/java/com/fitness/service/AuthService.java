package com.fitness.service;

import com.fitness.dto.LoginRequest;
import com.fitness.dto.RegisterRequest;
import com.fitness.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    LoginVO refresh(String refreshToken);

    void logout(Long userId, String accessToken);
}
