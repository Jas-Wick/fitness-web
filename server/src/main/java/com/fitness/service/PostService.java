package com.fitness.service;

import com.fitness.common.result.PageResult;
import com.fitness.dto.PostRequest;
import com.fitness.vo.PostVO;

import java.util.List;

/**
 * 帖子服务
 */
public interface PostService {

    PostVO create(Long userId, PostRequest request);

    PostVO update(Long userId, Long id, PostRequest request);

    void delete(Long userId, Long id);

    PostVO get(Long userId, Long id);

    PostVO forward(Long userId, Long originalId, String content);

    PageResult<PostVO> list(Long userId, long page, long size, String postType);

    List<PostVO> hot(Long userId, int limit);

    void like(Long userId, Long postId);

    void favorite(Long userId, Long postId);

    PageResult<PostVO> listFavorites(Long userId, long page, long size);
}
