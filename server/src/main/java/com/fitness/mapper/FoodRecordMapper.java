package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.FoodRecordEntity;
import com.fitness.vo.FoodStatVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 饮食记录表 Mapper
 */
public interface FoodRecordMapper extends BaseMapper<FoodRecordEntity> {

    /**
     * 区间营养汇总：单条 SQL 聚合，避免全量记录拉回内存求和
     * （走 idx_user_eat_time 索引，COALESCE 保证无记录返回 0）
     */
    @Select("""
            SELECT COALESCE(SUM(calories), 0) AS total_calories,
                   COALESCE(SUM(protein), 0) AS total_protein,
                   COALESCE(SUM(carbs), 0)   AS total_carbs,
                   COALESCE(SUM(fat), 0)     AS total_fat
            FROM t_food_record
            WHERE user_id = #{userId} AND deleted = 0
              AND eat_time >= #{startTime} AND eat_time < #{endTime}
            """)
    FoodStatVO sumByUserAndTime(@Param("userId") Long userId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
