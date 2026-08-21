package com.fitness.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 转发请求（评语可选）
 */
@Data
public class ForwardRequest {

    @Size(max = 1000, message = "转发评语不能超过 1000 字")
    private String content;
}
