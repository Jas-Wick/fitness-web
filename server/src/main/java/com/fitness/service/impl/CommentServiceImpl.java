package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constant.Constants;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.CommentRequest;
import com.fitness.entity.CommentEntity;
import com.fitness.entity.LikeEntity;
import com.fitness.entity.PostEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.CommentMapper;
import com.fitness.mapper.LikeMapper;
import com.fitness.mapper.PostMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.security.SecurityUtil;
import com.fitness.service.CommentService;
import com.fitness.service.HotRankService;
import com.fitness.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final HotRankService hotRankService;

    @Override
    @Transactional
    public CommentVO create(Long userId, CommentRequest request) {
        PostEntity post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        CommentEntity comment = new CommentEntity();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        commentMapper.insert(comment);
        // 帖子评论数 +1
        postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, request.getPostId())
                .setSql("comment_count = comment_count + 1"));
        // 帖子综合热度加权
        hotRankService.increment(Constants.REDIS_KEY_POST_HOT, request.getPostId(), Constants.HOT_WEIGHT_COMMENT);
        return buildVO(comment);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        CommentEntity comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        if (!comment.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人评论");
        }
        commentMapper.deleteById(id);
        postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, comment.getPostId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    @Override
    public PageResult<CommentVO> listByPost(Long postId, long page, long size) {
        // 单次最多 200 条，防止热门帖子整包返回
        size = Math.min(size, 200);
        Page<CommentEntity> p = new Page<>(page, size);
        commentMapper.selectPage(p, new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getPostId, postId)
                .orderByAsc(CommentEntity::getCreateTime));

        PageResult<CommentVO> result = new PageResult<>();
        result.setRecords(p.getRecords().isEmpty() ? List.of() : buildVOs(p.getRecords()));
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    @Transactional
    public void like(Long userId, Long commentId) {
        CommentEntity comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetType, Constants.LIKE_TARGET_COMMENT)
                .eq(LikeEntity::getTargetId, commentId));
        if (count != null && count > 0) {
            // 以影响行数为准，避免并发双击取消时计数多减
            int deleted = likeMapper.delete(new LambdaQueryWrapper<LikeEntity>()
                    .eq(LikeEntity::getUserId, userId)
                    .eq(LikeEntity::getTargetType, Constants.LIKE_TARGET_COMMENT)
                    .eq(LikeEntity::getTargetId, commentId));
            if (deleted > 0) {
                commentMapper.update(null, new LambdaUpdateWrapper<CommentEntity>()
                        .eq(CommentEntity::getId, commentId)
                        .setSql("like_count = GREATEST(like_count - 1, 0)"));
            }
        } else {
            LikeEntity like = new LikeEntity();
            like.setUserId(userId);
            like.setTargetType(Constants.LIKE_TARGET_COMMENT);
            like.setTargetId(commentId);
            try {
                likeMapper.insert(like);
            } catch (DuplicateKeyException e) {
                // 并发下已点赞，忽略（唯一索引兜底）
                return;
            }
            commentMapper.update(null, new LambdaUpdateWrapper<CommentEntity>()
                    .eq(CommentEntity::getId, commentId)
                    .setSql("like_count = like_count + 1"));
        }
    }

    /** 批量构建评论 VO（批量加载作者，避免 N+1） */
    private List<CommentVO> buildVOs(List<CommentEntity> comments) {
        Map<Long, UserEntity> userCache = userMapper.selectBatchIds(
                        comments.stream().map(CommentEntity::getUserId).distinct().toList())
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        return comments.stream().map(c -> buildVO(c, userCache)).toList();
    }

    private CommentVO buildVO(CommentEntity comment) {
        UserEntity user = userMapper.selectById(comment.getUserId());
        return buildVO(comment, user == null ? Map.of() : Map.of(user.getId(), user));
    }

    private CommentVO buildVO(CommentEntity comment, Map<Long, UserEntity> userCache) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setUserId(comment.getUserId());
        UserEntity user = userCache.get(comment.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatarUrl());
        }
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    private boolean isAdmin() {
        return SecurityUtil.getLoginUser() != null
                && Constants.ROLE_ADMIN.equals(SecurityUtil.getLoginUser().getRole());
    }
}
