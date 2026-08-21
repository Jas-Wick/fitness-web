# 数据库设计

> 本文是《FitHub：基于 Spring Boot + Vue 的智能健身管理平台》的完整数据库设计。数据库 `fitness`，字符集 `utf8mb4`，表前缀 `t_`。

## 1. 实体关系图（ER）

```
t_user 1 ────< t_training_record   （一名用户多条训练记录）
t_training_record 1 ────< t_training_set   （一条训练记录多组动作，基础/进阶共用）
t_user 1 ────< t_food_record        （一名用户多条饮食记录）
t_user 1 ────< t_body_data          （一名用户多条身体数据）
t_user 1 ────< t_post               （一名用户多篇帖子）
t_user 1 ────< t_comment            （一名用户多条评论）
t_user 1 ────< t_like               （一名用户多次点赞）
t_user 1 ────< t_favorite           （一名用户多条收藏）

t_post 1 ────< t_comment            （一篇帖子多条评论）
t_post 1 ────< t_like               （帖子点赞）
t_post 1 ────< t_favorite           （帖子收藏）
t_comment 1 ──< t_comment           （自引用 parent_id，楼中楼）
t_comment 1 ──< t_like              （评论点赞）
t_exercise (独立字典表，管理员维护)
```

**外键策略**：遵循阿里 Java 开发规范，**不建物理外键约束**，逻辑外键关系见上，应用层维护完整性；所有外键列建普通索引保证 JOIN/过滤性能。

## 2. 建表语句（schema.sql）

```sql
-- ===================== 用户表 =====================
CREATE TABLE `t_user` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(64)  NOT NULL                COMMENT '用户名（登录账号）',
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
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===================== 训练打卡记录表 =====================
CREATE TABLE `t_training_record` (
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
  KEY `idx_user_date` (`user_id`, `train_date`)  -- 连续打卡天数/日历统计核心索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练打卡记录表';

-- ===================== 训练动作组表 =====================
CREATE TABLE `t_training_set` (
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
  KEY `idx_record` (`record_id`)                         -- 按训练记录反查动作组
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练动作组表';

-- ===================== 饮食记录表 =====================
CREATE TABLE `t_food_record` (
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
  KEY `idx_user_eat_time` (`user_id`, `eat_time`)       -- 每日统计/营养分析核心索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- ===================== 健身动作库表 =====================
CREATE TABLE `t_exercise` (
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
  KEY `idx_body_part` (`body_part`),                     -- 分类查询
  KEY `idx_name` (`name`)                                -- 搜索
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身动作库表';

-- ===================== 身体数据记录表 =====================
CREATE TABLE `t_body_data` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
  `record_date`   DATE         NOT NULL                   COMMENT '记录日期',
  `weight`        DECIMAL(5,2) DEFAULT NULL               COMMENT '体重(kg)',
  `body_fat_rate` DECIMAL(5,2) DEFAULT NULL               COMMENT '体脂率(%)',
  `muscle_mass`   DECIMAL(5,2) DEFAULT NULL               COMMENT '肌肉量(kg)',
  `chest`         DECIMAL(6,2) DEFAULT NULL               COMMENT '胸围(cm)',
  `waist`         DECIMAL(6,2) DEFAULT NULL               COMMENT '腰围(cm)',
  `hip`           DECIMAL(6,2) DEFAULT NULL               COMMENT '臀围(cm)',
  `bmi`           DECIMAL(5,2) DEFAULT NULL               COMMENT 'BMI（写入时由身高体重计算，冗余存储便于历史查询）',
  `remark`        VARCHAR(255) DEFAULT NULL               COMMENT '备注',
  `deleted`       TINYINT NOT NULL DEFAULT 0              COMMENT '逻辑删除',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `record_date`)          -- 趋势图历史查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身体数据记录表';

-- ===================== 帖子表 =====================
CREATE TABLE `t_post` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '作者ID',
  `title`         VARCHAR(200) NOT NULL                   COMMENT '标题',
  `content`       TEXT         NOT NULL                   COMMENT '正文内容',
  `post_type`     VARCHAR(32)  NOT NULL DEFAULT '问题交流' COMMENT '类型：健身经验/饮食分享/训练计划/问题交流',
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
  KEY `idx_create_time` (`create_time`)                   -- 时间线/分页排序
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ===================== 评论表 =====================
CREATE TABLE `t_comment` (
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
CREATE TABLE `t_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '点赞人ID',
  `target_type` TINYINT NOT NULL                        COMMENT '目标类型：1帖子 2评论',
  `target_id`   BIGINT UNSIGNED NOT NULL                COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),  -- 防重复点赞
  KEY `idx_target` (`target_type`, `target_id`)         -- 反查某目标被赞情况
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ===================== 收藏表 =====================
CREATE TABLE `t_favorite` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '收藏人ID',
  `post_id`     BIGINT UNSIGNED NOT NULL                COMMENT '帖子ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),     -- 防重复收藏
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

## 3. 设计要点说明

- **主键**：统一 `BIGINT UNSIGNED AUTO_INCREMENT`（MyBatis-Plus `IdType.AUTO`），够用且简单，不引入雪花 ID（避免过度设计）
- **逻辑删除**：所有业务表带 `deleted` 字段 + MyBatis-Plus `@TableLogic` 全局配置
- **冗余计数**：`t_post.like_count/comment_count/favorite_count`、`t_comment.like_count` 冗余存储，避免列表页频繁 COUNT 子查询；更新用原子 SQL（`like_count = like_count + 1`）保证一致性
- **年龄动态计算**：存 `birth_date` 而非 `age`，避免年龄随时间失真，VO 层动态算
- **BMI 归属**：不单独建 `t_bmi_record` 表，`BMI = 体重 / (身高²)` 由 `t_user.height` + 最新体重实时计算，历史 BMI 曲线直接取 `t_body_data`（其内已冗余 `bmi` 列），避免两张表存同源数据
- **打卡复用训练记录**：`t_training_record` 即打卡依据，连续天数/日历由 `GROUP BY train_date` 派生，不额外建 check_in 表
- **动作组下钻**：训练明细拆到 `t_training_set`（含动作/重量/次数/组数），基础版只填 `body_part`，进阶版补全明细；列表回填时按 `record_id` 批量查询避免 N+1
- **点赞多态**：`t_like` 用 `target_type + target_id` 统一承载帖子和评论点赞，避免拆两张结构相同的表
