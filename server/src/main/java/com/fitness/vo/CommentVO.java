package com.fitness.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论响应
 */
@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private LocalDateTime createTime;
}
