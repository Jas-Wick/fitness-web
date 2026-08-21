package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.FoodRecordRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.FoodService;
import com.fitness.vo.FoodRecordVO;
import com.fitness.vo.FoodStatVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

/**
 * 饮食记录接口
 */
@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public Result<FoodRecordVO> create(@Valid @RequestBody FoodRecordRequest request) {
        return Result.success(foodService.create(SecurityUtil.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<FoodRecordVO> update(@PathVariable Long id, @Valid @RequestBody FoodRecordRequest request) {
        return Result.success(foodService.update(SecurityUtil.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        foodService.delete(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/stat")
    public Result<FoodStatVO> stat(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(foodService.stat(SecurityUtil.getUserId(), start, end));
    }

    @GetMapping
    public Result<PageResult<FoodRecordVO>> list(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size) {
        return Result.success(foodService.list(SecurityUtil.getUserId(), page, size));
    }
}
