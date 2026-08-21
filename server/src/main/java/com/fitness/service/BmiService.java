package com.fitness.service;

import com.fitness.vo.BmiVO;
import com.fitness.vo.BodyDataVO;

import java.util.List;

/**
 * BMI 计算服务
 */
public interface BmiService {

    BmiVO current(Long userId);

    List<BodyDataVO> history(Long userId);
}
