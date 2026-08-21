package com.fitness.controller;

import com.fitness.common.result.Result;
import com.fitness.security.SecurityUtil;
import com.fitness.service.BmiService;
import com.fitness.vo.BmiVO;
import com.fitness.vo.BodyDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BMI 计算接口
 */
@RestController
@RequestMapping("/api/bmi")
@RequiredArgsConstructor
public class BmiController {

    private final BmiService bmiService;

    @GetMapping("/current")
    public Result<BmiVO> current() {
        return Result.success(bmiService.current(SecurityUtil.getUserId()));
    }

    @GetMapping("/history")
    public Result<List<BodyDataVO>> history() {
        return Result.success(bmiService.history(SecurityUtil.getUserId()));
    }
}
