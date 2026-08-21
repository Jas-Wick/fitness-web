package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constant.Constants;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.ExerciseRequest;
import com.fitness.entity.ExerciseEntity;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.service.ExerciseService;
import com.fitness.service.HotRankService;
import com.fitness.vo.ExerciseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 健身动作库服务实现
 */
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseMapper exerciseMapper;
    private final HotRankService hotRankService;

    @Override
    public PageResult<ExerciseVO> page(long page, long size, String bodyPart, String keyword) {
        Page<ExerciseEntity> p = new Page<>(page, size);
        LambdaQueryWrapper<ExerciseEntity> wrapper = new LambdaQueryWrapper<ExerciseEntity>()
                .eq(ExerciseEntity::getStatus, 1);
        if (StringUtils.hasText(bodyPart)) {
            wrapper.eq(ExerciseEntity::getBodyPart, bodyPart);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ExerciseEntity::getName, keyword);
        }
        wrapper.orderByAsc(ExerciseEntity::getSortOrder).orderByDesc(ExerciseEntity::getId);
        exerciseMapper.selectPage(p, wrapper);

        PageResult<ExerciseVO> result = new PageResult<>();
        result.setRecords(p.getRecords().stream().map(ExerciseVO::from).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    public ExerciseVO detail(Long id) {
        ExerciseEntity entity = exerciseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动作不存在");
        }
        // 浏览量改走 Redis ZSet 自增，读路径不写 DB，由定时任务批量回写
        hotRankService.recordView(Constants.REDIS_KEY_EXERCISE_HOT, id);
        long delta = hotRankService.score(Constants.REDIS_KEY_EXERCISE_HOT, id);
        entity.setViewCount(entity.getViewCount() + (int) delta);
        return ExerciseVO.from(entity);
    }

    @Override
    public List<ExerciseVO> hot(int limit) {
        limit = Math.max(1, Math.min(limit, 50));
        // 综合 DB 基数与 Redis 增量计算热度；Redis 榜单为空时降级回 DB
        Map<Long, Double> rank = hotRankService.scores(Constants.REDIS_KEY_EXERCISE_HOT, limit * 2);
        List<ExerciseVO> merged = new ArrayList<>();
        if (!rank.isEmpty()) {
            List<ExerciseEntity> entities = exerciseMapper.selectBatchIds(rank.keySet().stream().toList());
            merged = entities.stream()
                    .filter(e -> Integer.valueOf(1).equals(e.getStatus()))
                    .map(e -> {
                        ExerciseVO vo = ExerciseVO.from(e);
                        vo.setViewCount(e.getViewCount()
                                + (int) rank.getOrDefault(e.getId(), 0D).longValue());
                        return vo;
                    })
                    .sorted(Comparator.comparing(ExerciseVO::getViewCount, Comparator.reverseOrder()))
                    .toList();
        }
        if (merged.size() < limit) {
            // 不足则从 DB 按浏览量补齐
            Set<Long> existing = merged.stream().map(ExerciseVO::getId).collect(Collectors.toSet());
            Page<ExerciseEntity> p = new Page<>(1, limit - merged.size());
            exerciseMapper.selectPage(p, new LambdaQueryWrapper<ExerciseEntity>()
                    .eq(ExerciseEntity::getStatus, 1)
                    .orderByDesc(ExerciseEntity::getViewCount));
            List<ExerciseVO> fill = new ArrayList<>(merged);
            p.getRecords().stream().map(ExerciseVO::from)
                    .filter(vo -> !existing.contains(vo.getId()))
                    .forEach(fill::add);
            merged = fill;
        }
        return merged;
    }

    @Override
    public ExerciseVO create(ExerciseRequest request) {
        ExerciseEntity entity = new ExerciseEntity();
        applyRequest(entity, request);
        exerciseMapper.insert(entity);
        return ExerciseVO.from(entity);
    }

    @Override
    public ExerciseVO update(Long id, ExerciseRequest request) {
        ExerciseEntity entity = exerciseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动作不存在");
        }
        applyRequest(entity, request);
        exerciseMapper.updateById(entity);
        return ExerciseVO.from(entity);
    }

    @Override
    public void delete(Long id) {
        exerciseMapper.deleteById(id);
    }

    private void applyRequest(ExerciseEntity entity, ExerciseRequest request) {
        entity.setName(request.getName());
        entity.setBodyPart(request.getBodyPart());
        entity.setDescription(request.getDescription());
        entity.setSteps(request.getSteps());
        entity.setPrecautions(request.getPrecautions());
        entity.setImageUrl(request.getImageUrl());
        entity.setVideoUrl(request.getVideoUrl());
        entity.setSortOrder(request.getSortOrder());
        entity.setStatus(request.getStatus());
    }
}
