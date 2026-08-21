package com.fitness.common.constant;

/**
 * 全局常量
 */
public final class Constants {

    private Constants() {
    }

    /** 角色 */
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 点赞目标类型 */
    public static final int LIKE_TARGET_POST = 1;
    public static final int LIKE_TARGET_COMMENT = 2;

    /** Redis Key 前缀 */
    public static final String REDIS_PREFIX = "fitness:";
    public static final String REDIS_KEY_REFRESH = REDIS_PREFIX + "refresh:";
    public static final String REDIS_KEY_BLACKLIST = REDIS_PREFIX + "blacklist:";
    public static final String REDIS_KEY_EXERCISE_HOT = REDIS_PREFIX + "exercise:hot";
    public static final String REDIS_KEY_POST_HOT = REDIS_PREFIX + "post:hot";
    public static final String REDIS_KEY_POST_VIEWS = REDIS_PREFIX + "post:views";
    public static final String REDIS_KEY_STATS_SITE = REDIS_PREFIX + "stats:site";
    public static final String REDIS_KEY_LOGIN_FAIL = REDIS_PREFIX + "login:fail:";

    /** 帖子综合热度权重：浏览/点赞/评论/收藏 */
    public static final int HOT_WEIGHT_VIEW = 1;
    public static final int HOT_WEIGHT_LIKE = 3;
    public static final int HOT_WEIGHT_COMMENT = 5;
    public static final int HOT_WEIGHT_FAVORITE = 4;

    /** 热度榜 TTL（秒），写入时续期，写入停止自然过期 */
    public static final long HOT_TTL_SECONDS = 1800;

    /** 登录失败限流：最大失败次数与锁定时长（秒） */
    public static final int LOGIN_FAIL_MAX = 5;
    public static final long LOGIN_FAIL_TTL_SECONDS = 900;

    /** 训练统计缓存（Redis） */
    public static final String REDIS_KEY_TRAINING_STATS = REDIS_PREFIX + "training:stats:";
    public static final String REDIS_KEY_TRAINING_STREAK = REDIS_PREFIX + "training:streak:";
    public static final int TRAINING_STATS_DAYS = 30;
}
