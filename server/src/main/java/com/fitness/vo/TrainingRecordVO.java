package com.fitness.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练打卡记录响应
 */
@Data
public class TrainingRecordVO {

    private Long id;
    private LocalDate trainDate;
    private Integer mode;
    private Integer durationValue;
    private String durationUnit;
    private Integer caloriesBurned;
    private String remark;
    private List<TrainingSetVO> sets;
    private LocalDateTime createTime;
}
