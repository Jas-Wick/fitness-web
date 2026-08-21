package com.fitness.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 饮食统计响应（某时间段汇总）
 */
@Data
public class FoodStatVO {

    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
}
