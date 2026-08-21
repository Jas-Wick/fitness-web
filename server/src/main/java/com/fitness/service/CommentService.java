package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.CommentRequest;
import com.fitness.vo.CommentVO;

/**
 * 评论服务
 */
public interface CommentService {

    CommentVO create(Long userId, CommentRequest request);

    void delete(Long userId, Long id);

    PageResult<CommentVO> listByPost(Long postId, long page, long size);

    void like(Long userId, Long commentId);
}
