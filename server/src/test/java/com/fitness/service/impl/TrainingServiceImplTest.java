package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.TrainingRecordRequest;
import com.fitness.entity.TrainingRecordEntity;
import com.fitness.entity.TrainingSetEntity;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.mapper.TrainingSetMapper;
import com.fitness.service.TrainingStatCacheService;
import com.fitness.vo.StreakVO;
import com.fitness.vo.TrainingDailyStat;
import com.fitness.vo.TrainingRecordVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 训练打卡服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private TrainingSetMapper trainingSetMapper;
    @Mock
    private TrainingStatCacheService trainingStatCacheService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private TrainingRecordEntity record;
    private TrainingSetEntity set;

    /** LambdaQueryWrapper 需要 MyBatis-Plus 的 TableInfo 元数据，单测中手动初始化 */
    @BeforeAll
    static void initMybatisPlusMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                TrainingRecordEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                TrainingSetEntity.class);
    }

    @BeforeEach
    void setUp() {
        record = new TrainingRecordEntity();
        record.setId(1L);
        record.setUserId(10L);
        record.setTrainDate(LocalDate.of(2026, 8, 17));
        record.setMode(2);
        record.setDurationValue(60);
        record.setDurationUnit("MINUTE");
        record.setCaloriesBurned(300);

        set = new TrainingSetEntity();
        set.setId(1L);
        set.setRecordId(1L);
        set.setBodyPart("胸");
        set.setExerciseName("杠铃卧推");
        set.setWeight(new BigDecimal("60.00"));
        set.setReps(12);
        set.setSets(4);
    }

    // ---------- 创建 ----------

    @Test
    @DisplayName("创建进阶记录：写主表 + 子表动作组 + 失效统计缓存")
    void create_advancedWritesRecordAndSets() {
        doAnswer(inv -> {
            TrainingRecordEntity e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        }).when(trainingRecordMapper).insert((TrainingRecordEntity) any());
        when(trainingSetMapper.insert((TrainingSetEntity) any())).thenReturn(1);
        when(trainingRecordMapper.selectById(1L)).thenReturn(record);
        when(trainingSetMapper.selectList(any(Wrapper.class))).thenReturn(List.of(set));

        TrainingRecordRequest req = new TrainingRecordRequest();
        req.setTrainDate(LocalDate.of(2026, 8, 17));
        req.setMode(2);
        req.setDurationValue(60);
        req.setDurationUnit("MINUTE");
        req.setCaloriesBurned(300);
        TrainingRecordRequest.TrainingSetItem item = new TrainingRecordRequest.TrainingSetItem();
        item.setBodyPart("胸");
        item.setExerciseName("杠铃卧推");
        item.setWeight(new BigDecimal("60"));
        item.setReps(12);
        item.setSets(4);
        req.setSets(List.of(item));

        TrainingRecordVO vo = trainingService.create(10L, req);

        assertThat(vo.getMode()).isEqualTo(2);
        assertThat(vo.getSets()).hasSize(1);
        assertThat(vo.getSets().get(0).getExerciseName()).isEqualTo("杠铃卧推");
        verify(trainingSetMapper).insert((TrainingSetEntity) any());
        verify(trainingStatCacheService).evict(10L);
    }

    @Test
    @DisplayName("创建基础记录：动作组只填训练类型")
    void create_basicWritesOnlyBodyPart() {
        doAnswer(inv -> {
            TrainingRecordEntity e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        }).when(trainingRecordMapper).insert((TrainingRecordEntity) any());
        TrainingRecordEntity basic = new TrainingRecordEntity();
        basic.setId(1L);
        basic.setUserId(10L);
        basic.setMode(1);
        when(trainingRecordMapper.selectById(1L)).thenReturn(basic);
        when(trainingSetMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        TrainingRecordRequest req = new TrainingRecordRequest();
        req.setTrainDate(LocalDate.of(2026, 8, 17));
        req.setMode(1);
        TrainingRecordRequest.TrainingSetItem item = new TrainingRecordRequest.TrainingSetItem();
        item.setBodyPart("有氧");
        req.setSets(List.of(item));

        TrainingRecordVO vo = trainingService.create(10L, req);

        assertThat(vo.getMode()).isEqualTo(1);
        verify(trainingSetMapper).insert((TrainingSetEntity) any());
    }

    // ---------- 更新 / 删除 ----------

    @Test
    @DisplayName("更新：先删后插子表并失效缓存")
    void update_replacesSets() {
        when(trainingRecordMapper.selectById(1L)).thenReturn(record);
        when(trainingSetMapper.selectList(any(Wrapper.class))).thenReturn(List.of(set));

        TrainingRecordRequest req = new TrainingRecordRequest();
        req.setTrainDate(LocalDate.of(2026, 8, 17));
        req.setMode(2);
        TrainingRecordRequest.TrainingSetItem item = new TrainingRecordRequest.TrainingSetItem();
        item.setBodyPart("背");
        item.setExerciseName("引体向上");
        req.setSets(List.of(item));

        trainingService.update(10L, 1L, req);

        verify(trainingSetMapper).delete(any(Wrapper.class));
        verify(trainingSetMapper).insert((TrainingSetEntity) any());
        verify(trainingStatCacheService).evict(10L);
    }

    @Test
    @DisplayName("更新：操作他人记录被拒绝")
    void update_notOwned() {
        record.setUserId(99L);
        when(trainingRecordMapper.selectById(1L)).thenReturn(record);

        TrainingRecordRequest req = new TrainingRecordRequest();
        req.setTrainDate(LocalDate.of(2026, 8, 17));
        req.setMode(1);

        assertThatThrownBy(() -> trainingService.update(10L, 1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("删除：删除主表与子表并失效缓存")
    void delete_removesRecordAndSets() {
        when(trainingRecordMapper.selectById(1L)).thenReturn(record);

        trainingService.delete(10L, 1L);

        verify(trainingRecordMapper).deleteById(1L);
        verify(trainingSetMapper).delete(any(Wrapper.class));
        verify(trainingStatCacheService).evict(10L);
    }

    // ---------- 统计缓存 ----------

    @Test
    @DisplayName("统计：缓存命中时不再查库")
    void stats_cacheHitSkipsDb() {
        TrainingDailyStat day = new TrainingDailyStat();
        day.setTrainDate(LocalDate.now());
        day.setRecordCount(2);
        day.setTotalDurationMinutes(60L);
        day.setTotalCalories(300L);
        when(trainingStatCacheService.getStats(10L)).thenReturn(Optional.of(List.of(day)));

        List<TrainingDailyStat> result = trainingService.stats(10L, 30);

        assertThat(result).hasSize(1);
        verify(trainingRecordMapper, never()).selectDailyStats(any(), any());
    }

    @Test
    @DisplayName("统计：缓存未命中时查库并写缓存")
    void stats_cacheMissFillsAndCaches() {
        when(trainingStatCacheService.getStats(10L)).thenReturn(Optional.empty());
        when(trainingRecordMapper.selectDailyStats(eq(10L), any(LocalDate.class))).thenReturn(List.of());

        List<TrainingDailyStat> result = trainingService.stats(10L, 30);

        assertThat(result).hasSize(30);
        assertThat(result.get(0).getRecordCount()).isZero();
        verify(trainingStatCacheService).putStats(eq(10L), any());
    }

    // ---------- 连续打卡 ----------

    @Test
    @DisplayName("连续打卡：连续两天 + 当前今天已打卡")
    void streak_calculatesCurrentAndLongest() {
        TrainingRecordEntity d1 = dateRecord(1L, LocalDate.now().minusDays(1));
        TrainingRecordEntity d2 = dateRecord(2L, LocalDate.now());
        when(trainingRecordMapper.selectList(any(Wrapper.class))).thenReturn(List.of(d1, d2));

        StreakVO streak = trainingService.streak(10L);

        assertThat(streak.getCurrentStreak()).isEqualTo(2);
        assertThat(streak.getLongestStreak()).isEqualTo(2);
        assertThat(streak.getTotalDays()).isEqualTo(2);
    }

    // ---------- 列表 ----------

    @Test
    @DisplayName("列表：分页返回并回填动作组")
    void list_returnsPageWithSets() {
        when(trainingRecordMapper.selectPage(any(), any(Wrapper.class))).thenAnswer(inv -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<TrainingRecordEntity> page = inv.getArgument(0);
            page.setRecords(List.of(record));
            page.setTotal(1);
            return page;
        });
        when(trainingSetMapper.selectList(any(Wrapper.class))).thenReturn(List.of(set));

        PageResult<TrainingRecordVO> result = trainingService.list(10L, 1, 10);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().get(0).getSets()).hasSize(1);
    }

    private TrainingRecordEntity dateRecord(Long id, LocalDate date) {
        TrainingRecordEntity e = new TrainingRecordEntity();
        e.setId(id);
        e.setUserId(10L);
        e.setTrainDate(date);
        return e;
    }
}
