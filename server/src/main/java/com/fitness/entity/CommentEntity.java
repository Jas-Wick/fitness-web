package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_comment")
public class CommentEntity extends BaseEntity {

    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
}
