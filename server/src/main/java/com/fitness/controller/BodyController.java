package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.BodyDataRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.BodyService;
import com.fitness.vo.BodyDataVO;
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
import java.util.List;

/**
 * 身体数据接口
 */
@RestController
@RequestMapping("/api/body")
@RequiredArgsConstructor
public class BodyController {

    private final BodyService bodyService;

    @PostMapping
    public Result<BodyDataVO> create(@Valid @RequestBody BodyDataRequest request) {
        return Result.success(bodyService.create(SecurityUtil.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<BodyDataVO> update(@PathVariable Long id, @Valid @RequestBody BodyDataRequest request) {
        return Result.success(bodyService.update(SecurityUtil.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bodyService.delete(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/trend")
    public Result<List<BodyDataVO>> trend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(bodyService.trend(SecurityUtil.getUserId(), start, end));
    }

    @GetMapping
    public Result<PageResult<BodyDataVO>> list(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return Result.success(bodyService.list(SecurityUtil.getUserId(), page, size));
    }
}
