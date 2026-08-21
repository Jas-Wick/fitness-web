package com.fitness.vo;

import lombok.Data;

/**
 * 站点统计响应
 */
@Data
public class SiteStatsVO {

    private Long userCount;
    private Long trainingCount;
    private Long foodCount;
    private Long postCount;
    private Long commentCount;
}
