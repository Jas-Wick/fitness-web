package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_post")
public class PostEntity extends BaseEntity {

    private Long userId;
    private String title;
    private String content;
    private String postType;
    private Long originalPostId;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer status;
}
