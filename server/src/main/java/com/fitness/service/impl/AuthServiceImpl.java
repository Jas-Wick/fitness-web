package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.constant.Constants;
import com.fitness.common.util.UserCodeGenerator;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.LoginRequest;
import com.fitness.dto.RegisterRequest;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.UserMapper;
import com.fitness.security.JwtUtil;
import com.fitness.security.TokenService;
import com.fitness.service.AuthService;
import com.fitness.vo.LoginVO;
import com.fitness.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public LoginVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setUserCode(UserCodeGenerator.generate());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername() : request.getNickname());
        user.setRole(Constants.ROLE_USER);
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 逻辑删除后同名用户仍占用唯一索引，转为友好提示而非 500
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        String username = request.getUsername();
        if (tokenService.getLoginFailCount(username) >= Constants.LOGIN_FAIL_MAX) {
            throw new BusinessException(ResultCode.FORBIDDEN, "尝试次数过多，请稍后再试");
        }
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            tokenService.incrementLoginFailCount(username);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        tokenService.clearLoginFailCount(username);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refresh token 无效或已过期");
        }
        if (!JwtUtil.isRefreshToken(claims)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refresh token 无效或已过期");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String savedJti = tokenService.getRefreshTokenJti(userId);
        if (savedJti == null || !savedJti.equals(claims.getId())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refresh token 已失效，请重新登录");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return buildLoginVO(user);
    }

    @Override
    public void logout(Long userId, String accessToken) {
        tokenService.deleteRefreshToken(userId);
        try {
            Claims claims = jwtUtil.parseToken(accessToken);
            long remaining = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            tokenService.addToBlacklist(claims.getId(), remaining);
        } catch (Exception ignored) {
            // token 已无效，无需处理
        }
    }

    /** 生成并缓存 access + refresh token，组装登录响应 */
    private LoginVO buildLoginVO(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        Claims refreshClaims = jwtUtil.parseToken(refreshToken);
        long ttl = (refreshClaims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        tokenService.saveRefreshToken(user.getId(), refreshClaims.getId(), ttl);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setUser(UserVO.from(user));
        return vo;
    }
}
