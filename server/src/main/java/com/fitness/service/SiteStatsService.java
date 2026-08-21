package com.fitness.service;

import com.fitness.common.constant.Constants;
import com.fitness.mapper.CommentMapper;
import com.fitness.mapper.FoodRecordMapper;
import com.fitness.mapper.PostMapper;
import com.fitness.mapper.TrainingRecordMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.vo.SiteStatsVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 站点统计服务：全站规模数据（用户/打卡/饮食/帖子/评论数）。
 *
 * <p>Cache-Aside 缓存到 Redis（10 分钟 + 随机偏移），由定时任务定期重算，
 * 读取路径不直接打库。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteStatsService {

    /** 基础 TTL 10 分钟 + 随机偏移防雪崩 */
    private static final long TTL_SECONDS = 600;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final FoodRecordMapper foodRecordMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    /** 站点统计（Cache-Aside：命中返回，未命中重算并回填） */
    public SiteStatsVO getStats() {
        String json = redis.opsForValue().get(Constants.REDIS_KEY_STATS_SITE);
        if (json != null) {
            try {
                return objectMapper.readValue(json, SiteStatsVO.class);
            } catch (Exception e) {
                log.warn("站点统计缓存反序列化失败，将重算", e);
            }
        }
        return refreshAndCache();
    }

    /** 重算统计并回填缓存（供定时任务与缓存未命中时调用） */
    public SiteStatsVO refreshAndCache() {
        SiteStatsVO vo = new SiteStatsVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setTrainingCount(trainingRecordMapper.selectCount(null));
        vo.setFoodCount(foodRecordMapper.selectCount(null));
        vo.setPostCount(postMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(null));
        try {
            String json = objectMapper.writeValueAsString(vo);
            long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(60);
            redis.opsForValue().set(Constants.REDIS_KEY_STATS_SITE, json, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("站点统计缓存写入失败", e);
        }
        return vo;
    }
}
