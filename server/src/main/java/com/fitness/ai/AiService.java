package com.fitness.ai;

/**
 * AI 业务门面：预留 AI 健身助手能力（阶段五落地）。
 *
 * <p>业务代码依赖本接口而非具体实现，保持 AI 模块与业务解耦，
 * 未来实现时按需补充 DTO 与具体 Provider。</p>
 */
public interface AiService {

    /** AI 生成训练计划 */
    String generateTrainingPlan(String userProfile);

    /** AI 饮食分析 */
    String analyzeDiet(String dietRecords);

    /** AI 健身问答 */
    String chat(String question);
}
