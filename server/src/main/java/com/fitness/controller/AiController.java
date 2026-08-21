package com.fitness.controller;

import com.fitness.ai.AiService;
import com.fitness.common.result.Result;
import com.fitness.dto.ChatRequest;
import com.fitness.dto.DietAnalysisRequest;
import com.fitness.dto.TrainingPlanRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力接口（需登录；未配置 API Key 时返回 503）
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** AI 健身问答 */
    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(aiService.chat(request.getQuestion()));
    }

    /** AI 生成训练计划 */
    @PostMapping("/training-plan")
    public Result<String> trainingPlan(@Valid @RequestBody TrainingPlanRequest request) {
        return Result.success(aiService.generateTrainingPlan(request.getUserProfile()));
    }

    /** AI 饮食分析 */
    @PostMapping("/diet-analysis")
    public Result<String> dietAnalysis(@Valid @RequestBody DietAnalysisRequest request) {
        return Result.success(aiService.analyzeDiet(request.getDietRecords()));
    }
}
