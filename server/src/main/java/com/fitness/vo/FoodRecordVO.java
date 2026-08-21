package com.fitness.vo;

import com.fitness.entity.FoodRecordEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录响应
 */
@Data
public class FoodRecordVO {

    private Long id;
    private String foodName;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Integer mealType;
    private LocalDateTime eatTime;
    private String remark;
    private LocalDateTime createTime;

    public static FoodRecordVO from(FoodRecordEntity e) {
        FoodRecordVO vo = new FoodRecordVO();
        vo.setId(e.getId());
        vo.setFoodName(e.getFoodName());
        vo.setCalories(e.getCalories());
        vo.setProtein(e.getProtein());
        vo.setCarbs(e.getCarbs());
        vo.setFat(e.getFat());
        vo.setMealType(e.getMealType());
        vo.setEatTime(e.getEatTime());
        vo.setRemark(e.getRemark());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}
