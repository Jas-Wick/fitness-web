package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_food_record")
public class FoodRecordEntity extends BaseEntity {

    private Long userId;
    private String foodName;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Integer mealType;
    private LocalDateTime eatTime;
    private String remark;
}
