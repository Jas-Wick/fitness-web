package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 帖子发布/更新请求
 */
@Data
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过 10000")
    private String content;

    @NotBlank(message = "帖子类型不能为空")
    private String postType;
}
