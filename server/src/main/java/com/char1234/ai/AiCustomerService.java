package com.char1234.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * AI 智能客服接口。
 * 由 LangChain4j 在运行时生成代理实现，自动处理：
 * - 对话记忆（ChatMemory）
 * - 商品知识检索（ContentRetriever）
 * - 工具调用（CartTool）
 * - 流式输出
 */
@SystemMessage(fromResource = "system-prompt.txt")
public interface AiCustomerService {

    /**
     * 流式对话
     *
     * @param memoryId 会话ID，用于隔离不同会话的记忆
     * @param message  用户消息
     * @return 流式 AI 回复（TokenStream 支持 SSE 逐字输出）
     */
    TokenStream streamChat(@MemoryId String memoryId, @UserMessage String message);
}
