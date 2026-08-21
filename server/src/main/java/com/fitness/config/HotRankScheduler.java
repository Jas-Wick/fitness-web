package com.fitness.config;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fitness.common.constant.Constants;
import com.fitness.entity.ExerciseEntity;
import com.fitness.entity.PostEntity;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.PostMapper;
import com.fitness.service.HotRankService;
import com.fitness.service.SiteStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：
 * <ul>
 *   <li>浏览量增量批量回写 DB（Redis → MySQL，缓解读路径写库）；</li>
 *   <li>站点统计定期重算（Redis 缓存）。</li>
 * </ul>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class HotRankScheduler {

    private final HotRankService hotRankService;
    private final SiteStatsService siteStatsService;
    private final ExerciseMapper exerciseMapper;
    private final PostMapper postMapper;

    /** 每 10 分钟把 Redis 里累计的浏览量增量回写 DB */
    @Scheduled(fixedDelay = 600_000, initialDelay = 60_000)
    public void flushViewCounts() {
        try {
            hotRankService.flushToDb(Constants.REDIS_KEY_EXERCISE_HOT, (id, delta) ->
                    exerciseMapper.update(null, new LambdaUpdateWrapper<ExerciseEntity>()
                            .eq(ExerciseEntity::getId, id)
                            .setSql("view_count = view_count + " + delta)));
            hotRankService.flushToDb(Constants.REDIS_KEY_POST_VIEWS, (id, delta) ->
                    postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                            .eq(PostEntity::getId, id)
                            .setSql("view_count = view_count + " + delta)));
        } catch (Exception e) {
            log.error("浏览量增量回写失败", e);
        }
    }

    /** 每 10 分钟重算一次站点统计缓存 */
    @Scheduled(fixedDelay = 600_000, initialDelay = 120_000)
    public void refreshSiteStats() {
        try {
            siteStatsService.refreshAndCache();
        } catch (Exception e) {
            log.error("站点统计重算失败", e);
        }
    }
}
