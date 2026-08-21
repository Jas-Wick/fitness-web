package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 训练打卡记录请求（基础版/进阶版）
 */
@Data
public class TrainingRecordRequest {

    @NotNull(message = "训练日期不能为空")
    private LocalDate trainDate;

    @NotNull(message = "训练模式不能为空")
    private Integer mode;

    private Integer durationValue;

    private String durationUnit;

    @PositiveOrZero(message = "消耗热量不能为负")
    private Integer caloriesBurned;

    private String remark;

    /** 动作组明细：基础版每项只填 bodyPart；进阶版每项填 bodyPart+exerciseName+weight+reps+sets */
    private List<TrainingSetItem> sets;

    @Data
    public static class TrainingSetItem {
        @NotBlank(message = "训练类型不能为空")
        private String bodyPart;
        private String exerciseName;
        private BigDecimal weight;
        private Integer reps;
        private Integer sets;
    }
}
