package com.fitness.controller;

import com.fitness.common.result.Result;
import com.fitness.service.SiteStatsService;
import com.fitness.vo.SiteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final SiteStatsService siteStatsService;

    /** 站点统计（公开） */
    @GetMapping("/site")
    public Result<SiteStatsVO> site() {
        return Result.success(siteStatsService.getStats());
    }
}
