package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录请求
 */
@Data
public class FoodRecordRequest {

    @NotBlank(message = "食物名称不能为空")
    private String foodName;

    @PositiveOrZero(message = "热量不能为负")
    private BigDecimal calories;

    @PositiveOrZero(message = "蛋白质不能为负")
    private BigDecimal protein;

    @PositiveOrZero(message = "碳水不能为负")
    private BigDecimal carbs;

    @PositiveOrZero(message = "脂肪不能为负")
    private BigDecimal fat;

    private Integer mealType;

    @NotNull(message = "摄入时间不能为空")
    private LocalDateTime eatTime;

    private String remark;
}
