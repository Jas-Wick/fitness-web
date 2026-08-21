package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constant.Constants;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
import com.fitness.common.result.ResultCode;
import com.fitness.dto.PostRequest;
import com.fitness.entity.FavoriteEntity;
import com.fitness.entity.LikeEntity;
import com.fitness.entity.PostEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.FavoriteMapper;
import com.fitness.mapper.LikeMapper;
import com.fitness.mapper.PostMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.security.SecurityUtil;
import com.fitness.service.HotRankService;
import com.fitness.service.PostService;
import com.fitness.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final HotRankService hotRankService;

    @Override
    public PostVO create(Long userId, PostRequest request) {
        PostEntity post = new PostEntity();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPostType(request.getPostType());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setViewCount(0);
        post.setStatus(1);
        postMapper.insert(post);
        return get(userId, post.getId());
    }

    @Override
    public PostVO update(Long userId, Long id, PostRequest request) {
        PostEntity post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改他人帖子");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPostType(request.getPostType());
        postMapper.updateById(post);
        return get(userId, id);
    }

    @Override
    public void delete(Long userId, Long id) {
        PostEntity post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        if (!post.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人帖子");
        }
        postMapper.deleteById(id);
    }

    @Override
    public PostVO get(Long userId, Long id) {
        PostEntity post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        // 浏览量：综合热度 +1（榜单），浏览量增量 +1（定时回写 DB），读路径不写库
        hotRankService.increment(Constants.REDIS_KEY_POST_HOT, id, Constants.HOT_WEIGHT_VIEW);
        hotRankService.recordView(Constants.REDIS_KEY_POST_VIEWS, id);
        long viewDelta = hotRankService.score(Constants.REDIS_KEY_POST_VIEWS, id);
        post.setViewCount(post.getViewCount() + (int) viewDelta);

        Map<Long, UserEntity> userCache = loadUsers(List.of(post.getUserId()));
        PostVO vo = buildVO(post, userId, userCache, likedSet(userId, List.of(id)),
                favoritedSet(userId, List.of(id)));
        fillOriginalPosts(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<PostVO> list(Long userId, long page, long size, String postType) {
        Page<PostEntity> p = new Page<>(page, size);
        LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getStatus, 1);
        if (StringUtils.hasText(postType)) {
            wrapper.eq(PostEntity::getPostType, postType);
        }
        wrapper.orderByDesc(PostEntity::getCreateTime);
        postMapper.selectPage(p, wrapper);

        List<PostEntity> posts = p.getRecords();
        List<Long> postIds = posts.stream().map(PostEntity::getId).toList();
        Map<Long, UserEntity> userCache = loadUsers(
                posts.stream().map(PostEntity::getUserId).distinct().toList());
        Set<Long> liked = likedSet(userId, postIds);
        Set<Long> favorited = favoritedSet(userId, postIds);

        List<PostVO> vos = posts.stream()
                .map(e -> buildVO(e, userId, userCache, liked, favorited))
                .toList();
        fillOriginalPosts(vos);

        PageResult<PostVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    @Override
    @Transactional
    public void like(Long userId, Long postId) {
        PostEntity post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        Long liked = likeMapper.selectCount(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetType, Constants.LIKE_TARGET_POST)
                .eq(LikeEntity::getTargetId, postId));
        boolean currentlyLiked = liked != null && liked > 0;
        toggleLike(userId, Constants.LIKE_TARGET_POST, postId,
                () -> postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                        .eq(PostEntity::getId, postId).setSql("like_count = GREATEST(like_count - 1, 0)")),
                () -> postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                        .eq(PostEntity::getId, postId).setSql("like_count = like_count + 1")));
        // 综合热度按点赞方向加权
        hotRankService.increment(Constants.REDIS_KEY_POST_HOT, postId,
                currentlyLiked ? -Constants.HOT_WEIGHT_LIKE : Constants.HOT_WEIGHT_LIKE);
    }

    @Override
    @Transactional
    public void favorite(Long userId, Long postId) {
        PostEntity post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId).eq(FavoriteEntity::getPostId, postId));
        boolean currentlyFavorited = count != null && count > 0;
        if (currentlyFavorited) {
            // 以影响行数为准，避免并发双击取消时计数多减
            int deleted = favoriteMapper.delete(new LambdaQueryWrapper<FavoriteEntity>()
                    .eq(FavoriteEntity::getUserId, userId).eq(FavoriteEntity::getPostId, postId));
            if (deleted > 0) {
                postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                        .eq(PostEntity::getId, postId).setSql("favorite_count = GREATEST(favorite_count - 1, 0)"));
                hotRankService.increment(Constants.REDIS_KEY_POST_HOT, postId, -Constants.HOT_WEIGHT_FAVORITE);
            }
        } else {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            try {
                favoriteMapper.insert(favorite);
            } catch (DuplicateKeyException e) {
                // 并发下已收藏，忽略（唯一索引兜底）
                return;
            }
            postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                    .eq(PostEntity::getId, postId).setSql("favorite_count = favorite_count + 1"));
            hotRankService.increment(Constants.REDIS_KEY_POST_HOT, postId, Constants.HOT_WEIGHT_FAVORITE);
        }
    }

    @Override
    @Transactional
    public PostVO forward(Long userId, Long originalId, String content) {
        PostEntity original = postMapper.selectById(originalId);
        if (original == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        PostEntity post = new PostEntity();
        post.setUserId(userId);
        post.setTitle("转发");
        post.setContent(content);
        post.setPostType(original.getPostType());
        post.setOriginalPostId(originalId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setViewCount(0);
        post.setStatus(1);
        postMapper.insert(post);
        return get(userId, post.getId());
    }

    @Override
    public PageResult<PostVO> listFavorites(Long userId, long page, long size) {
        Page<FavoriteEntity> fp = new Page<>(page, size);
        favoriteMapper.selectPage(fp, new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .orderByDesc(FavoriteEntity::getCreateTime));
        List<Long> postIds = fp.getRecords().stream().map(FavoriteEntity::getPostId).toList();
        if (postIds.isEmpty()) {
            PageResult<PostVO> empty = new PageResult<>();
            empty.setRecords(List.of());
            empty.setTotal(0);
            empty.setPage(page);
            empty.setSize(size);
            return empty;
        }
        List<PostEntity> posts = postMapper.selectBatchIds(postIds);
        Map<Long, UserEntity> userCache = loadUsers(
                posts.stream().map(PostEntity::getUserId).distinct().toList());
        Set<Long> liked = likedSet(userId, postIds);

        List<PostVO> vos = posts.stream()
                .map(e -> buildVO(e, userId, userCache, liked, Set.of(e.getId())))
                .toList();
        fillOriginalPosts(vos);

        PageResult<PostVO> result = new PageResult<>();
        result.setRecords(vos);
        result.setTotal(fp.getTotal());
        result.setPage(fp.getCurrent());
        result.setSize(fp.getSize());
        return result;
    }

    @Override
    public List<PostVO> hot(Long userId, int limit) {
        limit = Math.max(1, Math.min(limit, 50));
        Map<Long, Double> rank = hotRankService.scores(Constants.REDIS_KEY_POST_HOT, limit * 2);
        if (rank.isEmpty()) {
            return List.of();
        }
        List<PostEntity> posts = postMapper.selectBatchIds(rank.keySet().stream().toList())
                .stream().filter(p -> Integer.valueOf(1).equals(p.getStatus())).toList();
        List<Long> postIds = posts.stream().map(PostEntity::getId).toList();
        Map<Long, UserEntity> userCache = loadUsers(
                posts.stream().map(PostEntity::getUserId).distinct().toList());
        Set<Long> liked = likedSet(userId, postIds);
        Set<Long> favorited = favoritedSet(userId, postIds);

        List<PostVO> vos = posts.stream()
                .map(e -> buildVO(e, userId, userCache, liked, favorited))
                .toList();
        fillOriginalPosts(vos);
        // 按 Redis 综合热度分降序返回
        return vos.stream()
                .sorted(Comparator.comparingDouble((PostVO v) -> rank.getOrDefault(v.getId(), 0D)).reversed())
                .toList();
    }

    /** 点赞切换（帖子/评论通用） */
    private void toggleLike(Long userId, int targetType, Long targetId, Runnable decrement, Runnable increment) {
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetType, targetType)
                .eq(LikeEntity::getTargetId, targetId));
        if (count != null && count > 0) {
            // 以影响行数为准，避免并发双击取消时计数多减
            int deleted = likeMapper.delete(new LambdaQueryWrapper<LikeEntity>()
                    .eq(LikeEntity::getUserId, userId)
                    .eq(LikeEntity::getTargetType, targetType)
                    .eq(LikeEntity::getTargetId, targetId));
            if (deleted > 0) {
                decrement.run();
            }
        } else {
            LikeEntity like = new LikeEntity();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            try {
                likeMapper.insert(like);
            } catch (DuplicateKeyException e) {
                // 并发下已点赞，忽略（唯一索引兜底）
                return;
            }
            increment.run();
        }
    }

    private Set<Long> likedSet(Long userId, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }
        return likeMapper.selectList(new LambdaQueryWrapper<LikeEntity>()
                        .eq(LikeEntity::getUserId, userId)
                        .eq(LikeEntity::getTargetType, Constants.LIKE_TARGET_POST)
                        .in(LikeEntity::getTargetId, postIds))
                .stream().map(LikeEntity::getTargetId).collect(Collectors.toSet());
    }

    private Set<Long> favoritedSet(Long userId, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }
        return favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteEntity>()
                        .eq(FavoriteEntity::getUserId, userId)
                        .in(FavoriteEntity::getPostId, postIds))
                .stream().map(FavoriteEntity::getPostId).collect(Collectors.toSet());
    }

    private Map<Long, UserEntity> loadUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    /** 回填转发帖的原帖摘要（批量加载，避免 N+1） */
    private void fillOriginalPosts(List<PostVO> vos) {
        List<Long> originalIds = vos.stream()
                .map(PostVO::getOriginalPostId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (originalIds.isEmpty()) {
            return;
        }
        Map<Long, PostEntity> originals = postMapper.selectBatchIds(originalIds).stream()
                .collect(Collectors.toMap(PostEntity::getId, Function.identity()));
        Map<Long, UserEntity> authors = loadUsers(originals.values().stream()
                .map(PostEntity::getUserId).distinct().toList());
        for (PostVO vo : vos) {
            if (vo.getOriginalPostId() == null) {
                continue;
            }
            PostEntity original = originals.get(vo.getOriginalPostId());
            if (original == null) {
                continue;
            }
            vo.setOriginalTitle(original.getTitle());
            String content = original.getContent();
            vo.setOriginalContent(content != null && content.length() > 100
                    ? content.substring(0, 100) : content);
            UserEntity author = authors.get(original.getUserId());
            if (author != null) {
                vo.setOriginalAuthorNickname(author.getNickname());
            }
        }
    }

    private PostVO buildVO(PostEntity post, Long currentUserId, Map<Long, UserEntity> userCache,
                           Set<Long> liked, Set<Long> favorited) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setPostType(post.getPostType());
        vo.setOriginalPostId(post.getOriginalPostId());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setViewCount(post.getViewCount());
        vo.setAuthorId(post.getUserId());
        UserEntity author = userCache.get(post.getUserId());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatarUrl());
        }
        vo.setCreateTime(post.getCreateTime());
        vo.setLiked(liked.contains(post.getId()));
        vo.setFavorited(favorited.contains(post.getId()));
        return vo;
    }

    private boolean isAdmin() {
        return SecurityUtil.getLoginUser() != null
                && Constants.ROLE_ADMIN.equals(SecurityUtil.getLoginUser().getRole());
    }
}
