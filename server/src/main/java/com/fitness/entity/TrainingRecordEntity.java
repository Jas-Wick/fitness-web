package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 训练打卡记录表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_training_record")
public class TrainingRecordEntity extends BaseEntity {

    private Long userId;
    private LocalDate trainDate;
    private Integer mode;
    private Integer durationValue;
    private String durationUnit;
    private Integer caloriesBurned;
    private String remark;
}
