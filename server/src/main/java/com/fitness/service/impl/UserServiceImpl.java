package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.UpdateProfileRequest;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.UserMapper;
import com.fitness.service.FileStorageService;
import com.fitness.service.UserService;
import com.fitness.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    public UserVO getProfile(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return UserVO.from(user);
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setFitnessGoal(request.getFitnessGoal());
        user.setFitnessLevel(request.getFitnessLevel());
        userMapper.updateById(user);
        return getProfile(userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码错误");
        }
        UserEntity update = new UserEntity();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String url = fileStorageService.storeImage(file, "avatar_" + userId);
        UserEntity update = new UserEntity();
        update.setId(userId);
        update.setAvatarUrl(url);
        userMapper.updateById(update);
        return url;
    }

    @Override
    public PageResult<UserVO> listUsers(long page, long size, String keyword) {
        Page<UserEntity> p = new Page<>(page, size);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                    .or().like(UserEntity::getNickname, keyword));
        }
        wrapper.orderByDesc(UserEntity::getCreateTime);
        userMapper.selectPage(p, wrapper);

        PageResult<UserVO> result = new PageResult<>();
        result.setRecords(p.getRecords().stream().map(UserVO::from).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }
}
