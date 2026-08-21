package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.ExerciseRequest;
import com.fitness.service.ExerciseService;
import com.fitness.service.FileStorageService;
import com.fitness.service.UserService;
import com.fitness.vo.ExerciseVO;
import com.fitness.vo.UserVO;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端接口（需要 ADMIN 角色，由 SecurityConfig 统一控制）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExerciseService exerciseService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @PostMapping("/exercise")
    public Result<ExerciseVO> createExercise(@Valid @RequestBody ExerciseRequest request) {
        return Result.success(exerciseService.create(request));
    }

    @PutMapping("/exercise/{id}")
    public Result<ExerciseVO> updateExercise(@PathVariable Long id, @Valid @RequestBody ExerciseRequest request) {
        return Result.success(exerciseService.update(id, request));
    }

    @DeleteMapping("/exercise/{id}")
    public Result<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.delete(id);
        return Result.success();
    }

    @PostMapping("/exercise/image")
    public Result<String> uploadExerciseImage(@RequestParam("file") MultipartFile file) {
        return Result.success(fileStorageService.storeImage(file, "exercise"));
    }

    @GetMapping("/user")
    public Result<PageResult<UserVO>> listUsers(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(page, size, keyword));
    }
}
