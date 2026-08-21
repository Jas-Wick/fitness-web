-- ===================== 用户表 =====================
CREATE TABLE IF NOT EXISTS `t_user` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(64)  NOT NULL                COMMENT '用户名（登录账号）',
  `user_code`       VARCHAR(32)  NOT NULL                COMMENT '业务用户编码（对外唯一指向，防猜测）',
  `password`        VARCHAR(128) NOT NULL                COMMENT '密码（BCrypt 加密）',
  `nickname`        VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
  `avatar_url`      VARCHAR(255) DEFAULT NULL            COMMENT '头像 URL',
  `gender`          TINYINT      NOT NULL DEFAULT 0      COMMENT '性别：0未知 1男 2女',
  `birth_date`      DATE         DEFAULT NULL            COMMENT '出生日期（年龄按此动态计算）',
  `height`          DECIMAL(5,2) DEFAULT NULL            COMMENT '身高(cm)',
  `weight`          DECIMAL(5,2) DEFAULT NULL            COMMENT '体重(kg)',
  `fitness_goal`    VARCHAR(64)  DEFAULT NULL            COMMENT '健身目标：增肌/减脂/塑形/增强体能',
  `fitness_level`   VARCHAR(32)  DEFAULT NULL            COMMENT '健身等级：初级/中级/高级',
  `role`            VARCHAR(32)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1正常 0禁用',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删 1已删',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_user_code` (`user_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===================== 训练打卡记录表 =====================
CREATE TABLE IF NOT EXISTS `t_training_record` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`          BIGINT UNSIGNED NOT NULL                COMMENT '用户ID（逻辑外键）',
  `train_date`       DATE         NOT NULL                   COMMENT '训练日期（打卡依据）',
  `mode`             TINYINT      NOT NULL DEFAULT 1         COMMENT '模式：1基础 2进阶',
  `duration_value`   INT          DEFAULT NULL               COMMENT '训练时长数值',
  `duration_unit`    VARCHAR(8)   DEFAULT 'MINUTE'           COMMENT '时长单位：MINUTE/HOUR',
  `calories_burned`  INT          DEFAULT NULL               COMMENT '消耗热量(千卡)',
  `remark`           VARCHAR(500) DEFAULT NULL               COMMENT '训练备注',
  `deleted`          TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `train_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练打卡记录表';

-- ===================== 训练动作组表 =====================
CREATE TABLE IF NOT EXISTS `t_training_set` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id`     BIGINT UNSIGNED NOT NULL                COMMENT '训练记录ID（逻辑外键）',
  `body_part`     VARCHAR(32)  NOT NULL                   COMMENT '训练类型：胸/肩/背/腿/手臂/核心/有氧/其他',
  `exercise_name` VARCHAR(128) DEFAULT NULL               COMMENT '动作名称（进阶版填写）',
  `weight`        DECIMAL(6,2) DEFAULT NULL               COMMENT '重量(kg)',
  `reps`          INT          DEFAULT NULL               COMMENT '每组次数',
  `sets`          INT          DEFAULT NULL               COMMENT '组数',
  `deleted`       TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练动作组表';

-- ===================== 饮食记录表 =====================
CREATE TABLE IF NOT EXISTS `t_food_record` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
  `food_name`   VARCHAR(128) NOT NULL                   COMMENT '食物名称',
  `calories`    DECIMAL(8,2) DEFAULT NULL               COMMENT '热量(千卡)',
  `protein`     DECIMAL(8,2) DEFAULT NULL               COMMENT '蛋白质(g)',
  `carbs`       DECIMAL(8,2) DEFAULT NULL               COMMENT '碳水化合物(g)',
  `fat`         DECIMAL(8,2) DEFAULT NULL               COMMENT '脂肪(g)',
  `meal_type`   TINYINT      DEFAULT NULL               COMMENT '餐次：1早餐 2午餐 3晚餐 4加餐',
  `eat_time`    DATETIME     NOT NULL                   COMMENT '摄入时间',
  `remark`      VARCHAR(255) DEFAULT NULL               COMMENT '备注',
  `deleted`     TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_eat_time` (`user_id`, `eat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- ===================== 健身动作库表 =====================
CREATE TABLE IF NOT EXISTS `t_exercise` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         VARCHAR(128) NOT NULL                   COMMENT '动作名称',
  `body_part`    VARCHAR(32)  NOT NULL                   COMMENT '训练部位：胸部/背部/肩部/腿部/手臂/核心',
  `description`  VARCHAR(1000) DEFAULT NULL              COMMENT '动作介绍',
  `steps`        TEXT                                    COMMENT '标准步骤',
  `precautions`  VARCHAR(1000) DEFAULT NULL              COMMENT '注意事项',
  `image_url`    VARCHAR(255) DEFAULT NULL               COMMENT '图片地址',
  `video_url`    VARCHAR(255) DEFAULT NULL               COMMENT '视频地址',
  `view_count`   INT NOT NULL DEFAULT 0                  COMMENT '浏览量（热门榜依据）',
  `status`       TINYINT NOT NULL DEFAULT 1              COMMENT '状态：1上架 0下架',
  `sort_order`   INT NOT NULL DEFAULT 0                  COMMENT '排序值',
  `deleted`      TINYINT NOT NULL DEFAULT 0              COMMENT '逻辑删除',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_body_part` (`body_part`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身动作库表';

-- ===================== 身体数据记录表 =====================
CREATE TABLE IF NOT EXISTS `t_body_data` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
  `record_date`   DATE         NOT NULL                   COMMENT '记录日期',
  `weight`        DECIMAL(5,2) DEFAULT NULL               COMMENT '体重(kg)',
  `body_fat_rate` DECIMAL(5,2) DEFAULT NULL               COMMENT '体脂率(%)',
  `muscle_mass`   DECIMAL(5,2) DEFAULT NULL               COMMENT '肌肉量(kg)',
  `chest`         DECIMAL(6,2) DEFAULT NULL               COMMENT '胸围(cm)',
  `waist`         DECIMAL(6,2) DEFAULT NULL               COMMENT '腰围(cm)',
  `hip`           DECIMAL(6,2) DEFAULT NULL               COMMENT '臀围(cm)',
  `bmi`           DECIMAL(5,2) DEFAULT NULL               COMMENT 'BMI（写入时由身高体重计算，冗余存储）',
  `remark`        VARCHAR(255) DEFAULT NULL               COMMENT '备注',
  `deleted`       TINYINT NOT NULL DEFAULT 0              COMMENT '逻辑删除',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身体数据记录表';

-- ===================== 帖子表 =====================
CREATE TABLE IF NOT EXISTS `t_post` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '作者ID',
  `title`         VARCHAR(200) NOT NULL                   COMMENT '标题',
  `content`       TEXT         NOT NULL                   COMMENT '正文内容',
  `post_type`     VARCHAR(32)  NOT NULL DEFAULT '问题交流' COMMENT '类型：健身经验/饮食分享/训练计划/问题交流',
  `original_post_id` BIGINT UNSIGNED DEFAULT NULL         COMMENT '转发原帖ID（NULL=原创）',
  `like_count`    INT NOT NULL DEFAULT 0                  COMMENT '点赞数（冗余计数）',
  `comment_count` INT NOT NULL DEFAULT 0                  COMMENT '评论数（冗余计数）',
  `favorite_count` INT NOT NULL DEFAULT 0                 COMMENT '收藏数（冗余计数）',
  `view_count`    INT NOT NULL DEFAULT 0                  COMMENT '浏览数',
  `status`        TINYINT NOT NULL DEFAULT 1              COMMENT '状态：1正常 0隐藏',
  `deleted`       TINYINT NOT NULL DEFAULT 0              COMMENT '逻辑删除',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type` (`post_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ===================== 评论表 =====================
CREATE TABLE IF NOT EXISTS `t_comment` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`     BIGINT UNSIGNED NOT NULL                COMMENT '帖子ID',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '评论人ID',
  `parent_id`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '父评论ID（楼中楼回复，NULL=一级评论）',
  `content`     VARCHAR(1000) NOT NULL                  COMMENT '评论内容',
  `like_count`  INT NOT NULL DEFAULT 0                  COMMENT '点赞数（冗余计数）',
  `deleted`     TINYINT NOT NULL DEFAULT 0              COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ===================== 点赞表 =====================
CREATE TABLE IF NOT EXISTS `t_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '点赞人ID',
  `target_type` TINYINT NOT NULL                        COMMENT '目标类型：1帖子 2评论',
  `target_id`   BIGINT UNSIGNED NOT NULL                COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ===================== 收藏表 =====================
CREATE TABLE IF NOT EXISTS `t_favorite` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '收藏人ID',
  `post_id`     BIGINT UNSIGNED NOT NULL                COMMENT '帖子ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
