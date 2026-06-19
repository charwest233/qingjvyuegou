package com.char1234.config;

import com.char1234.ai.AiCustomerService;
import com.char1234.ai.CartTool;
import com.char1234.ai.ProductRagContentRetriever;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LangChain4j AI 配置类。
 * - ChatModel：非流式对话
 * - StreamingChatModel：流式对话（SSE 输出）
 * - ChatMemoryProvider：每个会话独立记忆
 * - AiCustomerService：通过 AiServices.builder() 组装
 */
@Configuration
public class AiConfig {

    @Value("${ai.siliconflow.base-url}")
    private String baseUrl;

    @Value("${ai.siliconflow.api-key}")
    private String apiKey;

    /**
     * 缓存每个会话的 ChatMemory，确保同一 memoryId 返回同一个实例
     */
    private final Map<Object, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    /**
     * 创建指定模型的流式聊天实例（支持动态选择模型）
     */
    public OpenAiStreamingChatModel createStreamingModel(String modelName) {
        boolean isVision = modelName != null && modelName.toUpperCase().contains("VL");
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.7)
                .maxTokens(isVision ? 4096 : 2048)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName("deepseek-ai/DeepSeek-V3")
                .temperature(0.7)
                .maxTokens(2048)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName("deepseek-ai/DeepSeek-V3")
                .temperature(0.7)
                .maxTokens(2048)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 聊天记忆提供者：每个 memoryId（会话ID）独立维护最近20条消息。
     * 使用 ConcurrentHashMap 缓存，确保同一 memoryId 始终返回同一实例。
     */
    @Bean
    public dev.langchain4j.memory.chat.ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> memoryCache.computeIfAbsent(memoryId,
                id -> MessageWindowChatMemory.withMaxMessages(20));
    }

    /**
     * AI 客服服务代理
     */
    @Bean
    public AiCustomerService aiCustomerService(
            OpenAiChatModel chatModel,
            OpenAiStreamingChatModel streamingChatModel,
            dev.langchain4j.memory.chat.ChatMemoryProvider chatMemoryProvider,
            ProductRagContentRetriever contentRetriever,
            CartTool cartTool) {
        return AiServices.builder(AiCustomerService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(contentRetriever)
                .tools(cartTool)
                .build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
