package com.fitness.ai;

/**
 * AI Provider SPI：第三方大模型统一抽象，未来可插拔实现。
 *
 * <p>暂不绑定具体供应商（OpenAI 兼容 / Claude / 国产大模型等），
 * 新增供应商只需实现本接口并在配置中指定即可。</p>
 */
public interface AiProvider {

    /**
     * 统一对话协议：输入提示词，返回模型文本输出
     *
     * @param prompt 提示词
     * @return 模型输出
     */
    String chat(String prompt);

    /** Provider 标识 */
    String name();
}
