package com.fitness.security;

import com.fitness.common.constant.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Token 存储服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("保存 refresh token：写入 Redis 并带 TTL")
    void saveRefreshToken_setsWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        tokenService.saveRefreshToken(1L, "jti-1", 604800L);

        verify(valueOps).set(eq(Constants.REDIS_KEY_REFRESH + "1"), eq("jti-1"),
                eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("获取 refresh token 的 jti")
    void getRefreshTokenJti() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(Constants.REDIS_KEY_REFRESH + "1")).thenReturn("jti-1");

        assertThat(tokenService.getRefreshTokenJti(1L)).isEqualTo("jti-1");
    }

    @Test
    @DisplayName("删除 refresh token")
    void deleteRefreshToken() {
        tokenService.deleteRefreshToken(1L);

        verify(redisTemplate).delete(Constants.REDIS_KEY_REFRESH + "1");
    }

    @Test
    @DisplayName("加入黑名单：TTL 大于 0 才写入")
    void addToBlacklist_withTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        tokenService.addToBlacklist("jti-1", 100L);

        verify(valueOps).set(eq(Constants.REDIS_KEY_BLACKLIST + "jti-1"), eq("1"),
                eq(100L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("加入黑名单：TTL 为 0 时跳过写入")
    void addToBlacklist_zeroTtlSkipped() {
        tokenService.addToBlacklist("jti-1", 0L);

        verify(valueOps, org.mockito.Mockito.never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("登录失败计数：首次计数为 1 时设置过期时间")
    void incrementLoginFailCount_setsExpireOnFirst() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(Constants.REDIS_KEY_LOGIN_FAIL + "admin")).thenReturn(1L);

        long count = tokenService.incrementLoginFailCount("admin");

        assertThat(count).isEqualTo(1L);
        verify(redisTemplate).expire(eq(Constants.REDIS_KEY_LOGIN_FAIL + "admin"),
                eq(Constants.LOGIN_FAIL_TTL_SECONDS), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("查询登录失败次数：无记录返回 0")
    void getLoginFailCount_emptyReturnsZero() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(Constants.REDIS_KEY_LOGIN_FAIL + "admin")).thenReturn(null);

        assertThat(tokenService.getLoginFailCount("admin")).isZero();
    }
}
