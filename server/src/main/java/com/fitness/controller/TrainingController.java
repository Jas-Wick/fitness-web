package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.TrainingRecordRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.TrainingService;
import com.fitness.vo.StreakVO;
import com.fitness.vo.TrainingDailyStat;
import com.fitness.vo.TrainingRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练打卡接口
 */
@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    public Result<TrainingRecordVO> create(@Valid @RequestBody TrainingRecordRequest request) {
        return Result.success(trainingService.create(SecurityUtil.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<TrainingRecordVO> update(@PathVariable Long id, @Valid @RequestBody TrainingRecordRequest request) {
        return Result.success(trainingService.update(SecurityUtil.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trainingService.delete(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/streak")
    public Result<StreakVO> streak() {
        return Result.success(trainingService.streak(SecurityUtil.getUserId()));
    }

    @GetMapping("/calendar")
    public Result<List<LocalDate>> calendar(@RequestParam int year, @RequestParam int month) {
        return Result.success(trainingService.calendar(SecurityUtil.getUserId(), year, month));
    }

    @GetMapping("/stats")
    public Result<List<TrainingDailyStat>> stats(@RequestParam(defaultValue = "30") int days) {
        return Result.success(trainingService.stats(SecurityUtil.getUserId(), days));
    }

    @GetMapping("/{id}")
    public Result<TrainingRecordVO> get(@PathVariable Long id) {
        return Result.success(trainingService.get(SecurityUtil.getUserId(), id));
    }

    @GetMapping
    public Result<PageResult<TrainingRecordVO>> list(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size) {
        return Result.success(trainingService.list(SecurityUtil.getUserId(), page, size));
    }
}
