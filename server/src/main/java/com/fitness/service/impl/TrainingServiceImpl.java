package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constant.Constants;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.TrainingRecordRequest;
import com.fitness.entity.TrainingRecordEntity;
import com.fitness.entity.TrainingSetEntity;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.mapper.TrainingSetMapper;
import com.fitness.service.TrainingService;
import com.fitness.service.TrainingStatCacheService;
import com.fitness.vo.StreakVO;
import com.fitness.vo.TrainingDailyStat;
import com.fitness.vo.TrainingRecordVO;
import com.fitness.vo.TrainingSetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练打卡服务实现
 */
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingSetMapper trainingSetMapper;
    private final TrainingStatCacheService trainingStatCacheService;

    @Override
    @Transactional
    public TrainingRecordVO create(Long userId, TrainingRecordRequest request) {
        TrainingRecordEntity entity = new TrainingRecordEntity();
        entity.setUserId(userId);
        applyRecord(entity, request);
        trainingRecordMapper.insert(entity);
        saveSets(entity.getId(), request.getSets());
        trainingStatCacheService.evict(userId);
        return get(userId, entity.getId());
    }

    @Override
    @Transactional
    public TrainingRecordVO update(Long userId, Long id, TrainingRecordRequest request) {
        TrainingRecordEntity entity = getOwned(userId, id);
        applyRecord(entity, request);
        trainingRecordMapper.updateById(entity);
        // 动作组先删后插
        trainingSetMapper.delete(new LambdaQueryWrapper<TrainingSetEntity>()
                .eq(TrainingSetEntity::getRecordId, id));
        saveSets(id, request.getSets());
        trainingStatCacheService.evict(userId);
        return get(userId, id);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        trainingRecordMapper.deleteById(id);
        trainingSetMapper.delete(new LambdaQueryWrapper<TrainingSetEntity>()
                .eq(TrainingSetEntity::getRecordId, id));
        trainingStatCacheService.evict(userId);
    }

    @Override
    public TrainingRecordVO get(Long userId, Long id) {
        TrainingRecordEntity entity = getOwned(userId, id);
        return toVO(entity, loadSets(List.of(id)).getOrDefault(id, List.of()));
    }

    @Override
    public PageResult<TrainingRecordVO> list(Long userId, long page, long size) {
        Page<TrainingRecordEntity> p = new Page<>(page, size);
        LambdaQueryWrapper<TrainingRecordEntity> wrapper = new LambdaQueryWrapper<TrainingRecordEntity>()
                .eq(TrainingRecordEntity::getUserId, userId)
                .orderByDesc(TrainingRecordEntity::getTrainDate)
                .orderByDesc(TrainingRecordEntity::getId);
        trainingRecordMapper.selectPage(p, wrapper);

        List<TrainingRecordEntity> records = p.getRecords();
        Map<Long, List<TrainingSetEntity>> setsMap = loadSets(
                records.stream().map(TrainingRecordEntity::getId).toList());

        PageResult<TrainingRecordVO> result = new PageResult<>();
        result.setRecords(records.stream()
                .map(e -> toVO(e, setsMap.getOrDefault(e.getId(), List.of())))
                .toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    public StreakVO streak(Long userId) {
        List<LocalDate> dates = distinctDates(userId, null, null);
        Set<LocalDate> set = new HashSet<>(dates);

        // 当前连续：从今天（或昨天，允许今日尚未打卡）往前数
        int current = 0;
        LocalDate cursor = LocalDate.now();
        if (!set.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        while (set.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        // 最长连续
        int longest = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate date : dates) {
            if (prev != null && prev.plusDays(1).equals(date)) {
                run++;
            } else {
                run = 1;
            }
            longest = Math.max(longest, run);
            prev = date;
        }

        return new StreakVO(current, longest, dates.size());
    }

    @Override
    public List<LocalDate> calendar(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return distinctDates(userId, start, end);
    }

    @Override
    public List<TrainingDailyStat> stats(Long userId, int days) {
        if (days == Constants.TRAINING_STATS_DAYS) {
            Optional<List<TrainingDailyStat>> cached = trainingStatCacheService.getStats(userId);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        List<TrainingDailyStat> result = computeStats(userId, days);
        if (days == Constants.TRAINING_STATS_DAYS) {
            trainingStatCacheService.putStats(userId, result);
        }
        return result;
    }

    private List<TrainingDailyStat> computeStats(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        Map<LocalDate, TrainingDailyStat> map = trainingRecordMapper
                .selectDailyStats(userId, start).stream()
                .collect(Collectors.toMap(TrainingDailyStat::getTrainDate, s -> s));

        // 补齐区间内所有天（无记录补 0），保证前端图 X 轴连续
        List<TrainingDailyStat> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = start.plusDays(i);
            TrainingDailyStat stat = map.get(date);
            if (stat == null) {
                stat = new TrainingDailyStat();
                stat.setTrainDate(date);
                stat.setRecordCount(0);
                stat.setTotalDurationMinutes(0L);
                stat.setTotalCalories(0L);
            }
            result.add(stat);
        }
        return result;
    }

    /** 查询某用户去重后的打卡日期（升序） */
    private List<LocalDate> distinctDates(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<TrainingRecordEntity> wrapper = new LambdaQueryWrapper<TrainingRecordEntity>()
                .select(TrainingRecordEntity::getTrainDate)
                .eq(TrainingRecordEntity::getUserId, userId);
        if (start != null) {
            wrapper.ge(TrainingRecordEntity::getTrainDate, start);
        }
        if (end != null) {
            wrapper.le(TrainingRecordEntity::getTrainDate, end);
        }
        return trainingRecordMapper.selectList(wrapper).stream()
                .map(TrainingRecordEntity::getTrainDate)
                .distinct()
                .sorted()
                .toList();
    }

    private void applyRecord(TrainingRecordEntity entity, TrainingRecordRequest request) {
        entity.setTrainDate(request.getTrainDate());
        entity.setMode(request.getMode());
        entity.setDurationValue(request.getDurationValue());
        entity.setDurationUnit(request.getDurationUnit());
        entity.setCaloriesBurned(request.getCaloriesBurned());
        entity.setRemark(request.getRemark());
    }

    private void saveSets(Long recordId, List<TrainingRecordRequest.TrainingSetItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (TrainingRecordRequest.TrainingSetItem item : items) {
            TrainingSetEntity set = new TrainingSetEntity();
            set.setRecordId(recordId);
            set.setBodyPart(item.getBodyPart());
            set.setExerciseName(item.getExerciseName());
            set.setWeight(item.getWeight());
            set.setReps(item.getReps());
            set.setSets(item.getSets());
            trainingSetMapper.insert(set);
        }
    }

    private Map<Long, List<TrainingSetEntity>> loadSets(List<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return Map.of();
        }
        return trainingSetMapper.selectList(new LambdaQueryWrapper<TrainingSetEntity>()
                        .in(TrainingSetEntity::getRecordId, recordIds))
                .stream()
                .collect(Collectors.groupingBy(TrainingSetEntity::getRecordId));
    }

    private TrainingRecordVO toVO(TrainingRecordEntity e, List<TrainingSetEntity> sets) {
        TrainingRecordVO vo = new TrainingRecordVO();
        vo.setId(e.getId());
        vo.setTrainDate(e.getTrainDate());
        vo.setMode(e.getMode());
        vo.setDurationValue(e.getDurationValue());
        vo.setDurationUnit(e.getDurationUnit());
        vo.setCaloriesBurned(e.getCaloriesBurned());
        vo.setRemark(e.getRemark());
        vo.setCreateTime(e.getCreateTime());
        vo.setSets(sets.stream().map(TrainingSetVO::from).toList());
        return vo;
    }

    private TrainingRecordEntity getOwned(Long userId, Long id) {
        TrainingRecordEntity entity = trainingRecordMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在或无权操作");
        }
        return entity;
    }
}
