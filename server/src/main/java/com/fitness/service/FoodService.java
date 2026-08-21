package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.FoodRecordRequest;
import com.fitness.vo.FoodRecordVO;
import com.fitness.vo.FoodStatVO;

import java.time.LocalDate;

/**
 * 饮食记录服务
 */
public interface FoodService {

    FoodRecordVO create(Long userId, FoodRecordRequest request);

    FoodRecordVO update(Long userId, Long id, FoodRecordRequest request);

    void delete(Long userId, Long id);

    PageResult<FoodRecordVO> list(Long userId, long page, long size);

    FoodStatVO stat(Long userId, LocalDate start, LocalDate end);
}
