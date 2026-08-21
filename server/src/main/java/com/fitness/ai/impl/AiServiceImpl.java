package com.fitness.ai.impl;

import com.fitness.ai.AiProvider;
import com.fitness.ai.AiService;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI 业务门面实现：基于可插拔 Provider 组装场景化提示词。
 * Provider 未配置（如缺少 GLM_API_KEY）时抛出业务异常，由全局异常处理统一返回。
 */
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiProvider aiProvider;

    @Override
    public String generateTrainingPlan(String userProfile) {
        String prompt = """
                你是一位资深健身教练。请根据以下用户信息，生成一份 7 天训练计划：
                要求：按天列出训练部位与动作名称，给出组数/次数/重量建议，并补充饮食与休息要点。
                用户信息：
                %s
                """.formatted(userProfile);
        return safeChat(prompt);
    }

    @Override
    public String analyzeDiet(String dietRecords) {
        String prompt = """
                你是一位注册营养师。请分析以下饮食记录：
                指出营养结构（热量/蛋白质/碳水/脂肪）是否均衡，热量缺口或盈余情况，
                并给出可执行的饮食改善建议。请用简洁的中文分条回答。
                饮食记录：
                %s
                """.formatted(dietRecords);
        return safeChat(prompt);
    }

    @Override
    public String chat(String question) {
        return safeChat(question);
    }

    private String safeChat(String prompt) {
        try {
            return aiProvider.chat(prompt);
        } catch (IllegalStateException e) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, e.getMessage());
        }
    }
}
