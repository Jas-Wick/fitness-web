package com.fitness.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest {

    private String nickname;
    private Integer gender;
    private LocalDate birthDate;
    private BigDecimal height;
    private BigDecimal weight;
    private String fitnessGoal;
    private String fitnessLevel;
}
