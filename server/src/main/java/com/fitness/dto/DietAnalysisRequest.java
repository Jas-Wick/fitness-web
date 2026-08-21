package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 饮食分析请求
 */
@Data
public class DietAnalysisRequest {

    @NotBlank(message = "饮食记录不能为空")
    @Size(max = 3000, message = "饮食记录长度不能超过 3000")
    private String dietRecords;
}
