package com.fitness.common.util;

import java.security.SecureRandom;

/**
 * 用户业务编码生成器：user- + 15 位字母数字混合随机值，作为用户对外唯一指向。
 *
 * <p>随机值使用 SecureRandom（加密安全、不可预测），且字符集剔除易混淆字符
 * （0/O、1/I/l），防止被猜测或枚举。唯一性由数据库 uk_user_code 唯一索引兜底。
 */
public final class UserCodeGenerator {

    private static final String PREFIX = "user-";
    private static final int RANDOM_LENGTH = 15;

    /** 字母数字混合字符集，剔除易混淆字符 0/O/1/I/l */
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    private UserCodeGenerator() {
    }

    /** 生成 user- + 15 位随机值（如 user-8xK3mQ7vLp2wD5cR） */
    public static String generate() {
        StringBuilder sb = new StringBuilder(PREFIX.length() + RANDOM_LENGTH);
        sb.append(PREFIX);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
