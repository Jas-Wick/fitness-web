package com.fitness.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private UserVO user;
}
