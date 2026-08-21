package com.fitness.service;

import com.fitness.common.constant.Constants;
import com.fitness.vo.TrainingDailyStat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 训练统计缓存服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TrainingStatCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    private TrainingStatCacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private TrainingDailyStat stat;

    @BeforeEach
    void setUp() {
        // 手动构造：注入真实 ObjectMapper（带 JavaTimeModule），保证 LocalDate 序列化正确
        cacheService = new TrainingStatCacheService(redisTemplate, objectMapper);
        stat = new TrainingDailyStat();
        stat.setTrainDate(LocalDate.now());
        stat.setRecordCount(2);
        stat.setTotalDurationMinutes(60L);
        stat.setTotalCalories(300L);
    }

    @Test
    @DisplayName("读缓存：命中时返回数据")
    void getStats_hit() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(Constants.REDIS_KEY_TRAINING_STATS + "10"))
                .thenReturn(objectMapper.writeValueAsString(List.of(stat)));

        Optional<List<TrainingDailyStat>> result = cacheService.getStats(10L);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getRecordCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("读缓存：未命中返回 empty")
    void getStats_miss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(Constants.REDIS_KEY_TRAINING_STATS + "10")).thenReturn(null);

        assertThat(cacheService.getStats(10L)).isEmpty();
    }

    @Test
    @DisplayName("写缓存：序列化后写入并带 TTL")
    void putStats_setsJsonWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        cacheService.putStats(10L, List.of(stat));

        verify(valueOps).set(eq(Constants.REDIS_KEY_TRAINING_STATS + "10"), anyString(),
                anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("失效：同时删除统计与连续打卡缓存")
    void evict_removesBothKeys() {
        cacheService.evict(10L);

        verify(redisTemplate).delete(Constants.REDIS_KEY_TRAINING_STATS + "10");
        verify(redisTemplate).delete(Constants.REDIS_KEY_TRAINING_STREAK + "10");
    }

    @Test
    @DisplayName("读缓存：脏数据反序列化失败时回源（返回 empty 而非抛异常）")
    void getStats_corruptJsonReturnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(Constants.REDIS_KEY_TRAINING_STATS + "10"))
                .thenReturn("{ not valid json ");

        assertThat(cacheService.getStats(10L)).isEmpty();
    }
}
