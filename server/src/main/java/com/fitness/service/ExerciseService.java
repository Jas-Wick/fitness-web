package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.ExerciseRequest;
import com.fitness.vo.ExerciseVO;

import java.util.List;

/**
 * 健身动作库服务
 */
public interface ExerciseService {

    PageResult<ExerciseVO> page(long page, long size, String bodyPart, String keyword);

    ExerciseVO detail(Long id);

    List<ExerciseVO> hot(int limit);

    ExerciseVO create(ExerciseRequest request);

    ExerciseVO update(Long id, ExerciseRequest request);

    void delete(Long id);
}
