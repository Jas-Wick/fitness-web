package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 身体数据记录请求
 */
@Data
public class BodyDataRequest {

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    private BigDecimal weight;
    private BigDecimal bodyFatRate;
    private BigDecimal muscleMass;
    private BigDecimal chest;
    private BigDecimal waist;
    private BigDecimal hip;
    private String remark;
}
