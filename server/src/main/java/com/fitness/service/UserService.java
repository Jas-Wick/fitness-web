package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.UpdateProfileRequest;
import com.fitness.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService {

    UserVO getProfile(Long userId);

    UserVO updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    String uploadAvatar(Long userId, MultipartFile file);

    PageResult<UserVO> listUsers(long page, long size, String keyword);
}
