package com.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.entity.TrainingRecordEntity;
import com.fitness.vo.TrainingDailyStat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练打卡记录表 Mapper
 */
public interface TrainingRecordMapper extends BaseMapper<TrainingRecordEntity> {

    /** 按天聚合训练统计（次数/总时长(分钟)/总热量） */
    @Select("""
            SELECT train_date,
                   COUNT(*) AS record_count,
                   COALESCE(SUM(CASE WHEN duration_unit = 'HOUR' THEN duration_value * 60 ELSE duration_value END), 0) AS total_duration_minutes,
                   COALESCE(SUM(calories_burned), 0) AS total_calories
            FROM t_training_record
            WHERE user_id = #{userId} AND deleted = 0 AND train_date >= #{start}
            GROUP BY train_date
            ORDER BY train_date
            """)
    List<TrainingDailyStat> selectDailyStats(@Param("userId") Long userId, @Param("start") LocalDate start);
}
