package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.ForwardRequest;
import com.fitness.dto.PostRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.PostService;
import com.fitness.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<PostVO> create(@Valid @RequestBody PostRequest request) {
        return Result.success(postService.create(SecurityUtil.getUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<PostVO> update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return Result.success(postService.update(SecurityUtil.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        postService.like(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        postService.favorite(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/forward")
    public Result<PostVO> forward(@PathVariable Long id, @Valid @RequestBody ForwardRequest request) {
        return Result.success(postService.forward(SecurityUtil.getUserId(), id, request.getContent()));
    }

    @GetMapping("/favorites")
    public Result<PageResult<PostVO>> favorites(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size) {
        return Result.success(postService.listFavorites(SecurityUtil.getUserId(), page, size));
    }

    @GetMapping("/hot")
    public Result<List<PostVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(postService.hot(SecurityUtil.getUserId(), limit));
    }

    @GetMapping("/{id}")
    public Result<PostVO> get(@PathVariable Long id) {
        return Result.success(postService.get(SecurityUtil.getUserId(), id));
    }

    @GetMapping
    public Result<PageResult<PostVO>> list(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String postType) {
        return Result.success(postService.list(SecurityUtil.getUserId(), page, size, postType));
    }
}
