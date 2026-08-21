package com.fitness.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类：生成与解析 access / refresh token
 */
@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-token-expire-seconds}") long accessExpireSeconds,
                   @Value("${jwt.refresh-token-expire-seconds}") long refreshExpireSeconds) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret 长度不足，至少需要 " + MIN_SECRET_BYTES + " 字节");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    /** 生成 access token（subject=userId，附带 role/username 与类型标识） */
    public String generateAccessToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim("type", TOKEN_TYPE_ACCESS)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 生成 refresh token（subject=userId，仅用于换发，带类型标识） */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", TOKEN_TYPE_REFRESH)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 解析 token，无效/过期会抛异常 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 是否为 access token */
    public static boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(claims.get("type", String.class));
    }

    /** 是否为 refresh token */
    public static boolean isRefreshToken(Claims claims) {
        return TOKEN_TYPE_REFRESH.equals(claims.get("type", String.class));
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }
}
