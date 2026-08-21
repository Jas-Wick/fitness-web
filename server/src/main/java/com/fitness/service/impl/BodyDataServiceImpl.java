package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.BodyDataRequest;
import com.fitness.entity.BodyDataEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.BodyDataMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.service.BodyService;
import com.fitness.vo.BodyDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 身体数据服务实现
 */
@Service
@RequiredArgsConstructor
public class BodyDataServiceImpl implements BodyService {

    private final BodyDataMapper bodyDataMapper;
    private final UserMapper userMapper;

    @Override
    public BodyDataVO create(Long userId, BodyDataRequest request) {
        BodyDataEntity entity = new BodyDataEntity();
        entity.setUserId(userId);
        applyRequest(entity, request);
        entity.setBmi(computeBmi(userId, request.getWeight()));
        bodyDataMapper.insert(entity);
        return BodyDataVO.from(entity);
    }

    @Override
    public BodyDataVO update(Long userId, Long id, BodyDataRequest request) {
        BodyDataEntity entity = getOwned(userId, id);
        applyRequest(entity, request);
        entity.setBmi(computeBmi(userId, request.getWeight()));
        bodyDataMapper.updateById(entity);
        return BodyDataVO.from(entity);
    }

    @Override
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        bodyDataMapper.deleteById(id);
    }

    @Override
    public PageResult<BodyDataVO> list(Long userId, long page, long size) {
        Page<BodyDataEntity> p = new Page<>(page, size);
        bodyDataMapper.selectPage(p, new LambdaQueryWrapper<BodyDataEntity>()
                .eq(BodyDataEntity::getUserId, userId)
                .orderByDesc(BodyDataEntity::getRecordDate)
                .orderByDesc(BodyDataEntity::getId));

        PageResult<BodyDataVO> result = new PageResult<>();
        result.setRecords(p.getRecords().stream().map(BodyDataVO::from).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    public List<BodyDataVO> trend(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<BodyDataEntity> wrapper = new LambdaQueryWrapper<BodyDataEntity>()
                .eq(BodyDataEntity::getUserId, userId)
                .orderByAsc(BodyDataEntity::getRecordDate);
        if (start != null) {
            wrapper.ge(BodyDataEntity::getRecordDate, start);
        }
        if (end != null) {
            wrapper.le(BodyDataEntity::getRecordDate, end);
        }
        return bodyDataMapper.selectList(wrapper).stream().map(BodyDataVO::from).toList();
    }

    /** 根据身高体重计算 BMI */
    private BigDecimal computeBmi(Long userId, BigDecimal weight) {
        if (weight == null) {
            return null;
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getHeight() == null || user.getHeight().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightM = user.getHeight().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weight.divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
    }

    private BodyDataEntity getOwned(Long userId, Long id) {
        BodyDataEntity entity = bodyDataMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或无权操作");
        }
        return entity;
    }

    private void applyRequest(BodyDataEntity entity, BodyDataRequest request) {
        entity.setRecordDate(request.getRecordDate());
        entity.setWeight(request.getWeight());
        entity.setBodyFatRate(request.getBodyFatRate());
        entity.setMuscleMass(request.getMuscleMass());
        entity.setChest(request.getChest());
        entity.setWaist(request.getWaist());
        entity.setHip(request.getHip());
        entity.setRemark(request.getRemark());
    }
}
