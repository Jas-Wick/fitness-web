package com.fitness.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BMI 结果响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BmiVO {

    private BigDecimal bmi;
    /** 评价：偏瘦/正常/超重/肥胖 */
    private String category;
    private String suggestion;
}
