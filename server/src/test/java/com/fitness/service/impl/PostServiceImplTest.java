package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import com.fitness.entity.FavoriteEntity;
import com.fitness.entity.LikeEntity;
import com.fitness.entity.PostEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.FavoriteMapper;
import com.fitness.mapper.LikeMapper;
import com.fitness.mapper.PostMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.service.HotRankService;
import com.fitness.vo.PostVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 帖子服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private LikeMapper likeMapper;
    @Mock
    private FavoriteMapper favoriteMapper;
    @Mock
    private HotRankService hotRankService;

    @InjectMocks
    private PostServiceImpl postService;

    private PostEntity original;
    private UserEntity author;

    @BeforeEach
    void setUp() {
        original = new PostEntity();
        original.setId(1L);
        original.setUserId(5L);
        original.setTitle("原帖");
        original.setContent("原帖内容");
        original.setPostType("问题交流");
        original.setLikeCount(0);
        original.setCommentCount(0);
        original.setFavoriteCount(0);
        original.setViewCount(0);
        original.setStatus(1);

        author = new UserEntity();
        author.setId(5L);
        author.setNickname("作者");
    }

    // ---------- 转发 ----------

    @Test
    @DisplayName("转发：生成转发帖并回填原帖摘要")
    void forward_createsPostWithOriginalInfo() {
        when(postMapper.selectById(1L)).thenReturn(original);

        PostEntity forward = new PostEntity();
        forward.setId(2L);
        forward.setUserId(10L);
        forward.setTitle("转发");
        forward.setContent("转发一下");
        forward.setPostType("问题交流");
        forward.setOriginalPostId(1L);
        forward.setLikeCount(0);
        forward.setCommentCount(0);
        forward.setFavoriteCount(0);
        forward.setViewCount(0);
        forward.setStatus(1);
        doAnswer(inv -> {
            PostEntity e = inv.getArgument(0);
            e.setId(2L);
            return 1;
        }).when(postMapper).insert((PostEntity) any());
        when(postMapper.selectById(2L)).thenReturn(forward);
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author));
        when(likeMapper.selectList(any())).thenReturn(List.of());
        when(favoriteMapper.selectList(any())).thenReturn(List.of());
        when(postMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(original));

        PostVO vo = postService.forward(10L, 1L, "转发一下");

        assertThat(vo.getOriginalPostId()).isEqualTo(1L);
        assertThat(vo.getOriginalTitle()).isEqualTo("原帖");
        assertThat(vo.getOriginalAuthorNickname()).isEqualTo("作者");
        verify(postMapper).insert((PostEntity) any());
    }

    @Test
    @DisplayName("转发：原帖不存在返回 NOT_FOUND")
    void forward_originalNotFound() {
        when(postMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> postService.forward(10L, 1L, "转发一下"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(postMapper, never()).insert((PostEntity) any());
    }

    // ---------- 点赞 ----------

    @Test
    @DisplayName("点赞：未点赞时插入 Like 并 +1")
    void like_toggleOn() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(likeMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(likeMapper.insert((LikeEntity) any())).thenReturn(1);

        postService.like(10L, 1L);

        verify(likeMapper).insert((LikeEntity) any());
        verify(postMapper).update(eq(null), any(Wrapper.class));
    }

    @Test
    @DisplayName("取消点赞：已点赞时删除并 -1（带下限保护）")
    void like_toggleOff() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(likeMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        when(likeMapper.delete(any(Wrapper.class))).thenReturn(1);

        postService.like(10L, 1L);

        verify(likeMapper).delete(any(Wrapper.class));
        verify(postMapper).update(eq(null), any(Wrapper.class));
    }

    @Test
    @DisplayName("点赞：帖子不存在返回 NOT_FOUND")
    void like_postNotFound() {
        when(postMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> postService.like(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(likeMapper, never()).insert((LikeEntity) any());
    }

    // ---------- 收藏 ----------

    @Test
    @DisplayName("收藏：未收藏时插入 Favorite")
    void favorite_toggleOn() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(favoriteMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(favoriteMapper.insert((FavoriteEntity) any())).thenReturn(1);

        postService.favorite(10L, 1L);

        verify(favoriteMapper).insert((FavoriteEntity) any());
    }

    @Test
    @DisplayName("收藏：并发重复收藏时唯一键冲突被静默忽略，不抛异常")
    void favorite_duplicateKeyIgnored() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(favoriteMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(favoriteMapper.insert((FavoriteEntity) any()))
                .thenThrow(new DuplicateKeyException("dup"));

        postService.favorite(10L, 1L);

        // 不抛异常即通过；且不应再更新计数
        verify(postMapper, never()).update(eq(null), any(Wrapper.class));
    }

    @Test
    @DisplayName("取消收藏：已收藏时删除 Favorite")
    void favorite_toggleOff() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(favoriteMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        when(favoriteMapper.delete(any(Wrapper.class))).thenReturn(1);

        postService.favorite(10L, 1L);

        verify(favoriteMapper).delete(any(Wrapper.class));
        verify(postMapper).update(eq(null), any(Wrapper.class));
    }

    @Test
    @DisplayName("详情：返回点赞/收藏状态")
    void get_returnsLikeAndFavoriteFlags() {
        when(postMapper.selectById(1L)).thenReturn(original);
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author));
        when(likeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(likeEntity(1L)));
        when(favoriteMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PostVO vo = postService.get(10L, 1L);

        assertThat(vo.getLiked()).isTrue();
        assertThat(vo.getFavorited()).isFalse();
    }

    private LikeEntity likeEntity(Long postId) {
        LikeEntity like = new LikeEntity();
        like.setUserId(10L);
        like.setTargetId(postId);
        like.setTargetType(1);
        return like;
    }
}
