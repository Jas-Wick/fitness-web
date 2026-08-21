package com.fitness.security;

import com.fitness.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Token 存储服务：refresh token 保存与 access token 黑名单吊销
 */
@Component
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;

    /** 保存 refresh token 的 jti（一个用户一份，用于校验与吊销） */
    public void saveRefreshToken(Long userId, String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(Constants.REDIS_KEY_REFRESH + userId, jti, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshTokenJti(Long userId) {
        return redisTemplate.opsForValue().get(Constants.REDIS_KEY_REFRESH + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(Constants.REDIS_KEY_REFRESH + userId);
    }

    /** access token 加入黑名单（登出吊销） */
    public void addToBlacklist(String jti, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(Constants.REDIS_KEY_BLACKLIST + jti, "1", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(Constants.REDIS_KEY_BLACKLIST + jti));
    }

    /** 登录失败计数 +1，首次写入时设置过期时间 */
    public long incrementLoginFailCount(String username) {
        String key = Constants.REDIS_KEY_LOGIN_FAIL + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Constants.LOGIN_FAIL_TTL_SECONDS, TimeUnit.SECONDS);
        }
        return count == null ? 0 : count;
    }

    /** 查询登录失败次数 */
    public long getLoginFailCount(String username) {
        String value = redisTemplate.opsForValue().get(Constants.REDIS_KEY_LOGIN_FAIL + username);
        return value == null ? 0 : Long.parseLong(value);
    }

    /** 登录成功后清除失败计数 */
    public void clearLoginFailCount(String username) {
        redisTemplate.delete(Constants.REDIS_KEY_LOGIN_FAIL + username);
    }
}
