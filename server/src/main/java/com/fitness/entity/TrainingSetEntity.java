package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 训练动作组表实体（训练记录的明细子表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_training_set")
public class TrainingSetEntity extends BaseEntity {

    private Long recordId;
    private String bodyPart;
    private String exerciseName;
    private BigDecimal weight;
    private Integer reps;
    private Integer sets;
}
