package com.fitness.service.impl;

import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.LoginRequest;
import com.fitness.dto.RegisterRequest;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.UserMapper;
import com.fitness.security.JwtUtil;
import com.fitness.security.TokenService;
import com.fitness.vo.LoginVO;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity admin;
    private Claims refreshClaims;

    @BeforeEach
    void setUp() {
        admin = new UserEntity();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("$2a$10$hashed");
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);

        // 构建 token 链：generateRefreshToken → parseToken → 拿到 jti
        refreshClaims = mock(Claims.class);
        lenient().when(refreshClaims.getId()).thenReturn("refresh-jti-1");
        lenient().when(refreshClaims.getSubject()).thenReturn("1");
        lenient().when(refreshClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 100_000));
        lenient().when(refreshClaims.get("type", String.class)).thenReturn("refresh");
        lenient().when(jwtUtil.generateAccessToken(any(), anyString(), anyString())).thenReturn("fake-access-token");
        lenient().when(jwtUtil.generateRefreshToken(any())).thenReturn("fake-refresh-token");
        lenient().when(jwtUtil.parseToken("fake-refresh-token")).thenReturn(refreshClaims);
    }

    // ---------- 注册 ----------

    @Test
    @DisplayName("注册成功：无重名时插入并返回 LoginVO")
    void register_success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded");
        when(userMapper.insert((UserEntity) any())).thenReturn(1);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("admin123");

        LoginVO vo = authService.register(req);

        assertThat(vo.getAccessToken()).isEqualTo("fake-access-token");
        assertThat(vo.getUser().getUsername()).isEqualTo("newuser");
        verify(userMapper).insert((UserEntity) any());
    }

    @Test
    @DisplayName("注册失败：用户名已存在返回 CONFLICT")
    void register_usernameExists() {
        when(userMapper.selectCount(any())).thenReturn(1L);
        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.CONFLICT.getCode());
        verify(userMapper, never()).insert((UserEntity) any());
    }

    @Test
    @DisplayName("注册失败：并发下唯一键冲突降级为 409 而非 500")
    void register_duplicateKeyConflict() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userMapper.insert((UserEntity) any())).thenThrow(new DuplicateKeyException("dup"));

        RegisterRequest req = new RegisterRequest();
        req.setUsername("taken");
        req.setPassword("admin123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.CONFLICT.getCode());
    }

    // ---------- 登录 ----------

    @Test
    @DisplayName("登录成功：密码匹配且账号正常，清除失败计数")
    void login_success() {
        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("admin123", admin.getPassword())).thenReturn(true);
        when(tokenService.getLoginFailCount("admin")).thenReturn(0L);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        LoginVO vo = authService.login(req);

        assertThat(vo.getUser().getUsername()).isEqualTo("admin");
        verify(tokenService).clearLoginFailCount("admin");
    }

    @Test
    @DisplayName("登录被限流：失败次数达到阈值直接拒绝，不再查库")
    void login_lockedByFailCount() {
        when(tokenService.getLoginFailCount("admin")).thenReturn(5L);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.FORBIDDEN.getCode());
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("登录失败：密码错误时失败计数 +1")
    void login_wrongPasswordIncrementsFail() {
        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("wrong", admin.getPassword())).thenReturn(false);
        when(tokenService.getLoginFailCount("admin")).thenReturn(0L);
        when(tokenService.incrementLoginFailCount("admin")).thenReturn(1L);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.UNAUTHORIZED.getCode());
        verify(tokenService).incrementLoginFailCount("admin");
    }

    @Test
    @DisplayName("登录失败：账号被禁用返回 FORBIDDEN")
    void login_disabled() {
        admin.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("admin123", admin.getPassword())).thenReturn(true);
        when(tokenService.getLoginFailCount("admin")).thenReturn(0L);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.FORBIDDEN.getCode());
    }

    // ---------- 刷新 ----------

    @Test
    @DisplayName("刷新成功：refresh token 类型正确、jti 匹配、账号正常")
    void refresh_success() {
        when(tokenService.getRefreshTokenJti(1L)).thenReturn("refresh-jti-1");
        when(userMapper.selectById(1L)).thenReturn(admin);

        LoginVO vo = authService.refresh("fake-refresh-token");

        assertThat(vo.getUser().getUsername()).isEqualTo("admin");
        verify(tokenService).saveRefreshToken(eq(1L), eq("refresh-jti-1"), any(Long.class));
    }

    @Test
    @DisplayName("刷新失败：非 refresh 类型的 token 被拒绝")
    void refresh_wrongTokenType() {
        when(refreshClaims.get("type", String.class)).thenReturn("access");
        when(jwtUtil.parseToken("fake-refresh-token")).thenReturn(refreshClaims);

        assertThatThrownBy(() -> authService.refresh("fake-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.UNAUTHORIZED.getCode());
    }

    @Test
    @DisplayName("刷新失败：jti 与 Redis 不匹配视为已失效")
    void refresh_jtiMismatch() {
        when(tokenService.getRefreshTokenJti(1L)).thenReturn("another-jti");

        assertThatThrownBy(() -> authService.refresh("fake-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.UNAUTHORIZED.getCode());
    }

    @Test
    @DisplayName("刷新失败：账号被禁用后无法续期")
    void refresh_disabledUser() {
        admin.setStatus(0);
        when(tokenService.getRefreshTokenJti(1L)).thenReturn("refresh-jti-1");
        when(userMapper.selectById(1L)).thenReturn(admin);

        assertThatThrownBy(() -> authService.refresh("fake-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.FORBIDDEN.getCode());
    }

    // ---------- 登出 ----------

    @Test
    @DisplayName("登出：删除 refresh token 并将 access token 加入黑名单")
    void logout_blacklistsToken() {
        when(jwtUtil.parseToken("fake-access-token")).thenReturn(refreshClaims);

        authService.logout(1L, "fake-access-token");

        verify(tokenService).deleteRefreshToken(1L);
        // TTL 为剩余有效秒数，随时间流逝非精确值，仅断言 key 正确
        verify(tokenService).addToBlacklist(eq("refresh-jti-1"), anyLong());
    }

    @Test
    @DisplayName("登出：token 已无效时静默忽略，不抛异常")
    void logout_invalidTokenIgnored() {
        when(jwtUtil.parseToken("bad-token")).thenThrow(new RuntimeException("expired"));

        authService.logout(1L, "bad-token");

        verify(tokenService).deleteRefreshToken(1L);
    }
}
