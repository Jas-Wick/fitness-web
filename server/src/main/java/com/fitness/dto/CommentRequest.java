package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求
 */
@Data
public class CommentRequest {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论长度不能超过 1000")
    private String content;

    /** 父评论ID（楼中楼回复，可选） */
    private Long parentId;
}
