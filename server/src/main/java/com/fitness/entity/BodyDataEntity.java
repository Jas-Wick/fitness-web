package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 身体数据记录表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_body_data")
public class BodyDataEntity extends BaseEntity {

    private Long userId;
    private LocalDate recordDate;
    private BigDecimal weight;
    private BigDecimal bodyFatRate;
    private BigDecimal muscleMass;
    private BigDecimal chest;
    private BigDecimal waist;
    private BigDecimal hip;
    private BigDecimal bmi;
    private String remark;
}
