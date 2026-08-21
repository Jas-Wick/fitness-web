package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 问答请求
 */
@Data
public class ChatRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题长度不能超过 2000")
    private String question;
}
