package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.FoodRecordRequest;
import com.fitness.entity.FoodRecordEntity;
import com.fitness.mapper.FoodRecordMapper;
import com.fitness.service.FoodService;
import com.fitness.vo.FoodRecordVO;
import com.fitness.vo.FoodStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食记录服务实现
 */
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodRecordMapper foodRecordMapper;

    @Override
    public FoodRecordVO create(Long userId, FoodRecordRequest request) {
        FoodRecordEntity entity = new FoodRecordEntity();
        entity.setUserId(userId);
        applyRequest(entity, request);
        foodRecordMapper.insert(entity);
        return FoodRecordVO.from(entity);
    }

    @Override
    public FoodRecordVO update(Long userId, Long id, FoodRecordRequest request) {
        FoodRecordEntity entity = getOwned(userId, id);
        applyRequest(entity, request);
        foodRecordMapper.updateById(entity);
        return FoodRecordVO.from(entity);
    }

    @Override
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        foodRecordMapper.deleteById(id);
    }

    @Override
    public PageResult<FoodRecordVO> list(Long userId, long page, long size) {
        Page<FoodRecordEntity> p = new Page<>(page, size);
        LambdaQueryWrapper<FoodRecordEntity> wrapper = new LambdaQueryWrapper<FoodRecordEntity>()
                .eq(FoodRecordEntity::getUserId, userId)
                .orderByDesc(FoodRecordEntity::getEatTime);
        foodRecordMapper.selectPage(p, wrapper);

        PageResult<FoodRecordVO> result = new PageResult<>();
        result.setRecords(p.getRecords().stream().map(FoodRecordVO::from).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    public FoodStatVO stat(Long userId, LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        // 聚合下沉到数据库单条 SQL，避免全量记录拉回内存
        return foodRecordMapper.sumByUserAndTime(userId, startTime, endTime);
    }

    private FoodRecordEntity getOwned(Long userId, Long id) {
        FoodRecordEntity entity = foodRecordMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或无权操作");
        }
        return entity;
    }

    private void applyRequest(FoodRecordEntity entity, FoodRecordRequest request) {
        entity.setFoodName(request.getFoodName());
        entity.setCalories(request.getCalories());
        entity.setProtein(request.getProtein());
        entity.setCarbs(request.getCarbs());
        entity.setFat(request.getFat());
        entity.setMealType(request.getMealType());
        entity.setEatTime(request.getEatTime());
        entity.setRemark(request.getRemark());
    }
}
