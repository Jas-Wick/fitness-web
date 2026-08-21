package com.fitness.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户信息
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getId();
    }
}
