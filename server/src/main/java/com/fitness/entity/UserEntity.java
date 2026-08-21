package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    private String username;
    private String userCode;
    private String password;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthDate;
    private BigDecimal height;
    private BigDecimal weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private String role;
    private Integer status;
    private LocalDateTime lastLoginTime;
}
