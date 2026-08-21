package com.fitness.service;

import com.fitness.common.constant.Constants;
import com.fitness.vo.TrainingDailyStat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 训练统计缓存服务（Cache-Aside：读时回填、写时失效）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingStatCacheService {

    /** 基础 TTL 10 分钟，加随机偏移防缓存雪崩 */
    private static final long TTL_SECONDS = 600;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<List<TrainingDailyStat>> getStats(Long userId) {
        String json = redisTemplate.opsForValue().get(Constants.REDIS_KEY_TRAINING_STATS + userId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<List<TrainingDailyStat>>() {
            }));
        } catch (Exception e) {
            log.warn("训练统计缓存反序列化失败，将回源查询", e);
            return Optional.empty();
        }
    }

    public void putStats(Long userId, List<TrainingDailyStat> stats) {
        try {
            String json = objectMapper.writeValueAsString(stats);
            long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(60);
            redisTemplate.opsForValue().set(Constants.REDIS_KEY_TRAINING_STATS + userId, json,
                    ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("训练统计缓存写入失败", e);
        }
    }

    /** 训练记录增删改后失效该用户统计缓存 */
    public void evict(Long userId) {
        redisTemplate.delete(Constants.REDIS_KEY_TRAINING_STATS + userId);
        redisTemplate.delete(Constants.REDIS_KEY_TRAINING_STREAK + userId);
    }
}
