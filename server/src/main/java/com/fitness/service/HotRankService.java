package com.fitness.service;

import com.fitness.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 热度排行榜服务：基于 Redis ZSet 实现榜单与浏览量计数。
 *
 * <p>设计要点：
 * <ul>
 *   <li>榜单/计数都在 Redis 内原子自增（ZINCRBY），读路径不写 DB；</li>
 *   <li>每次写入续期 TTL，写入停止后自然过期（榜单降级回 DB 查询）；</li>
 *   <li>浏览量增量由定时任务批量回写 DB（{@code HotRankScheduler.flushViewCounts}）。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotRankService {

    private final StringRedisTemplate redis;

    /** 浏览量 +1 */
    public void recordView(String key, Long id) {
        increment(key, id, Constants.HOT_WEIGHT_VIEW);
    }

    /** 按权重自增（如点赞/评论/收藏），delta 可为负数 */
    public void increment(String key, Long id, double delta) {
        try {
            redis.opsForZSet().incrementScore(key, id.toString(), delta);
            redis.expire(key, Constants.HOT_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("热度自增失败 key={} id={}", key, id, e);
        }
    }

    /** 取某成员当前分数（无则 0） */
    public long score(String key, Long id) {
        try {
            Double s = redis.opsForZSet().score(key, id.toString());
            return s == null ? 0 : s.longValue();
        } catch (Exception e) {
            log.warn("读取热度分数失败 key={} id={}", key, id, e);
            return 0;
        }
    }

    /** 取分数最高的若干成员及其分数（供榜单查询） */
    public Map<Long, Double> scores(String key, int max) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redis.opsForZSet().reverseRangeWithScores(key, 0, Math.max(0, max - 1));
            if (tuples == null) {
                return Map.of();
            }
            Map<Long, Double> map = new HashMap<>();
            for (ZSetOperations.TypedTuple<String> t : tuples) {
                if (t.getValue() != null && t.getScore() != null) {
                    map.put(Long.valueOf(t.getValue()), t.getScore());
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("读取热度榜失败 key={}", key, e);
            return Map.of();
        }
    }

    /**
     * 把 ZSet 内累计的浏览量增量逐条回写 DB（flush 后移除对应成员，避免重复累加）。
     * 任一条失败仅告警，不中断后续回写。
     */
    public void flushToDb(String key, BiConsumer<Long, Long> flush) {
        Set<ZSetOperations.TypedTuple<String>> tuples;
        try {
            tuples = redis.opsForZSet().rangeWithScores(key, 0, -1);
        } catch (Exception e) {
            log.warn("读取热度增量失败 key={}", key, e);
            return;
        }
        if (tuples == null || tuples.isEmpty()) {
            return;
        }
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            String member = t.getValue();
            Double score = t.getScore();
            if (member == null || score == null || score <= 0) {
                continue;
            }
            try {
                flush.accept(Long.valueOf(member), score.longValue());
                redis.opsForZSet().remove(key, member);
            } catch (Exception e) {
                log.warn("浏览量回写失败 key={} member={}", key, member, e);
            }
        }
    }
}
