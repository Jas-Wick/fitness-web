package com.fitness.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连续打卡统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakVO {

    /** 当前连续打卡天数 */
    private int currentStreak;
    /** 最长连续打卡天数 */
    private int longestStreak;
    /** 累计打卡天数 */
    private int totalDays;
}
