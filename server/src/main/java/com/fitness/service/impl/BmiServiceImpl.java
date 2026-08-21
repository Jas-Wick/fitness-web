package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import com.fitness.entity.BodyDataEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.BodyDataMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.service.BmiService;
import com.fitness.vo.BmiVO;
import com.fitness.vo.BodyDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * BMI 计算服务实现
 */
@Service
@RequiredArgsConstructor
public class BmiServiceImpl implements BmiService {

    private final UserMapper userMapper;
    private final BodyDataMapper bodyDataMapper;

    @Override
    public BmiVO current(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getHeight() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先完善身高信息");
        }
        // 最新体重优先取最近一条身体数据，否则取用户资料体重
        BigDecimal weight = null;
        Page<BodyDataEntity> p = new Page<>(1, 1);
        bodyDataMapper.selectPage(p, new LambdaQueryWrapper<BodyDataEntity>()
                .eq(BodyDataEntity::getUserId, userId)
                .orderByDesc(BodyDataEntity::getRecordDate)
                .orderByDesc(BodyDataEntity::getId));
        if (!p.getRecords().isEmpty() && p.getRecords().get(0).getWeight() != null) {
            weight = p.getRecords().get(0).getWeight();
        } else {
            weight = user.getWeight();
        }
        if (weight == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先记录体重");
        }
        return calcBmi(weight, user.getHeight());
    }

    @Override
    public List<BodyDataVO> history(Long userId) {
        return bodyDataMapper.selectList(new LambdaQueryWrapper<BodyDataEntity>()
                        .eq(BodyDataEntity::getUserId, userId)
                        .orderByAsc(BodyDataEntity::getRecordDate))
                .stream().map(BodyDataVO::from).toList();
    }

    /** 计算 BMI 与评价 */
    private BmiVO calcBmi(BigDecimal weight, BigDecimal heightCm) {
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal bmi = weight.divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);

        String category;
        String suggestion;
        if (bmi.compareTo(BigDecimal.valueOf(18.5)) < 0) {
            category = "偏瘦";
            suggestion = "适当增加营养摄入与力量训练";
        } else if (bmi.compareTo(BigDecimal.valueOf(24)) < 0) {
            category = "正常";
            suggestion = "保持当前良好的状态";
        } else if (bmi.compareTo(BigDecimal.valueOf(28)) < 0) {
            category = "超重";
            suggestion = "控制饮食并加强有氧运动";
        } else {
            category = "肥胖";
            suggestion = "建议咨询专业人士制定减重计划";
        }
        return new BmiVO(bmi, category, suggestion);
    }
}
