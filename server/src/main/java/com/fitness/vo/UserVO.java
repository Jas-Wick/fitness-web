package com.fitness.vo;

import com.fitness.entity.UserEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 用户信息响应
 */
@Data
public class UserVO {

    private Long id;
    private String userCode;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthDate;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private String role;
    private LocalDateTime createTime;

    /** 从实体转换，年龄按出生日期动态计算 */
    public static UserVO from(UserEntity e) {
        UserVO vo = new UserVO();
        vo.setId(e.getId());
        vo.setUserCode(e.getUserCode());
        vo.setUsername(e.getUsername());
        vo.setNickname(e.getNickname());
        vo.setAvatarUrl(e.getAvatarUrl());
        vo.setGender(e.getGender());
        vo.setBirthDate(e.getBirthDate());
        vo.setAge(e.getBirthDate() == null ? null
                : Period.between(e.getBirthDate(), LocalDate.now()).getYears());
        vo.setHeight(e.getHeight());
        vo.setWeight(e.getWeight());
        vo.setFitnessGoal(e.getFitnessGoal());
        vo.setFitnessLevel(e.getFitnessLevel());
        vo.setRole(e.getRole());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}
