package com.fitness.controller;

import com.fitness.common.result.Result;
import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.UpdateProfileRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.UserService;
import com.fitness.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.success(userService.getProfile(SecurityUtil.getUserId()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success(userService.updateProfile(SecurityUtil.getUserId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(SecurityUtil.getUserId(), request);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.uploadAvatar(SecurityUtil.getUserId(), file));
    }
}
