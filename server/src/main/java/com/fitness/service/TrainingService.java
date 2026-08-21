package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.TrainingRecordRequest;
import com.fitness.vo.StreakVO;
import com.fitness.vo.TrainingDailyStat;
import com.fitness.vo.TrainingRecordVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练打卡服务
 */
public interface TrainingService {

    TrainingRecordVO create(Long userId, TrainingRecordRequest request);

    TrainingRecordVO update(Long userId, Long id, TrainingRecordRequest request);

    void delete(Long userId, Long id);

    TrainingRecordVO get(Long userId, Long id);

    PageResult<TrainingRecordVO> list(Long userId, long page, long size);

    StreakVO streak(Long userId);

    List<LocalDate> calendar(Long userId, int year, int month);

    List<TrainingDailyStat> stats(Long userId, int days);
}
