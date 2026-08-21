package com.fitness.controller;

import com.fitness.common.result.PageResult;
import com.fitness.common.result.Result;
import com.fitness.dto.CommentRequest;
import com.fitness.security.SecurityUtil;
import com.fitness.service.CommentService;
import com.fitness.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@Valid @RequestBody CommentRequest request) {
        return Result.success(commentService.create(SecurityUtil.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        commentService.like(SecurityUtil.getUserId(), id);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<CommentVO>> list(@RequestParam Long postId,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "50") long size) {
        return Result.success(commentService.listByPost(postId, page, size));
    }
}
