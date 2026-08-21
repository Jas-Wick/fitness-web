package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.BodyDataRequest;
import com.fitness.vo.BodyDataVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 身体数据服务
 */
public interface BodyService {

    BodyDataVO create(Long userId, BodyDataRequest request);

    BodyDataVO update(Long userId, Long id, BodyDataRequest request);

    void delete(Long userId, Long id);

    PageResult<BodyDataVO> list(Long userId, long page, long size);

    List<BodyDataVO> trend(Long userId, LocalDate start, LocalDate end);
}
