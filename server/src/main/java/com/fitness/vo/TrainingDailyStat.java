package com.fitness.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 单日训练统计（供分析图/Redis 缓存）
 */
@Data
public class TrainingDailyStat {

    private LocalDate trainDate;
    private Integer recordCount;
    private Long totalDurationMinutes;
    private Long totalCalories;
}
