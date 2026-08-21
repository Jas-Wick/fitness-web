package com.fitness.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子响应
 */
@Data
public class PostVO {

    private Long id;
    private String title;
    private String content;
    private String postType;
    private Long originalPostId;
    private String originalTitle;
    private String originalAuthorNickname;
    private String originalContent;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private LocalDateTime createTime;
    /** 当前用户是否已点赞 */
    private Boolean liked;
    /** 当前用户是否已收藏 */
    private Boolean favorited;
}
