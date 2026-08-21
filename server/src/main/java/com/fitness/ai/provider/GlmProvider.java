package com.fitness.ai.provider;

import com.fitness.ai.AiProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * 智谱 GLM 大模型 Provider（OpenAI 兼容 /chat/completions 协议）。
 *
 * <p>配置驱动：{@code ai.base-url} / {@code ai.api-key} / {@code ai.model}，
 * API Key 走环境变量 {@code GLM_API_KEY}，不落库、不打日志。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "glm")
public class GlmProvider implements AiProvider {

    private static final String DEFAULT_MODEL = "glm-4.7-flash";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GlmProvider(@Value("${ai.base-url:https://open.bigmodel.cn/api/paas/v4}") String baseUrl,
                       @Value("${ai.api-key:}") String apiKey,
                       @Value("${ai.model:glm-4.7-flash}") String model,
                       ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = StringUtils.hasText(model) ? model : DEFAULT_MODEL;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public String chat(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("AI 服务未配置，请在环境变量中设置 GLM_API_KEY");
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);
            body.put("stream", false);

            JsonNode resp = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String content = resp.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null) {
                log.warn("GLM 返回格式异常: {}", resp);
                throw new IllegalStateException("AI 返回结果格式异常");
            }
            return content;
        } catch (Exception e) {
            log.error("GLM 调用失败", e);
            throw new IllegalStateException("AI 服务调用失败：" + e.getMessage());
        }
    }

    @Override
    public String name() {
        return "glm";
    }
}
