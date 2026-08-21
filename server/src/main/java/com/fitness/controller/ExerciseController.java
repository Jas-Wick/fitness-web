package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.service.ExerciseService;
import com.fitness.vo.ExerciseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健身动作库接口（查询公开）
 */
@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public Result<PageResult<ExerciseVO>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String bodyPart,
                                               @RequestParam(required = false) String keyword) {
        return Result.success(exerciseService.page(page, size, bodyPart, keyword));
    }

    @GetMapping("/hot")
    public Result<List<ExerciseVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(exerciseService.hot(limit));
    }

    @GetMapping("/{id}")
    public Result<ExerciseVO> detail(@PathVariable Long id) {
        return Result.success(exerciseService.detail(id));
    }
}
