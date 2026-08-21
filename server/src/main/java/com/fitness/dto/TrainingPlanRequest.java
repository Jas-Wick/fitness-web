package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 生成训练计划请求
 */
@Data
public class TrainingPlanRequest {

    @NotBlank(message = "用户信息不能为空")
    @Size(max = 2000, message = "用户信息长度不能超过 2000")
    private String userProfile;
}
