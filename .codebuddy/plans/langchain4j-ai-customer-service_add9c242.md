---
name: langchain4j-ai-customer-service
overview: 将现有的手动 API 调用 AI 对话改造为 LangChain4j + @Tool function calling 智能客服，支持 AI 操作购物车、搜索商品等业务能力
todos:
  - id: add-langchain4j-dependency
    content: 在pom.xml添加langchain4j-open-ai依赖 + application.yml添加langchain4j配置，复用现有SiliconFlow api-key
    status: completed
  - id: create-ai-config
    content: 创建AiConfig配置类(ChatLanguageModel/StreamingChatModel/ChatMemoryProvider) + ProductRagContentRetriever(适配现有ProductRagService)
    status: completed
    dependencies:
      - add-langchain4j-dependency
  - id: create-tool-and-service
    content: "创建CartTool(@Tool: 加购物车/搜商品/查购物车) + AiCustomerService(@AiService接口) + system-prompt.txt"
    status: completed
    dependencies:
      - create-ai-config
  - id: modify-product-mapper
    content: 在ProductMapper添加selectByName方法，按商品名称模糊查询
    status: completed
  - id: refactor-ai-chat-impl
    content: 重写AiChatServiceImpl.streamChat，用AiCustomerService替换RestTemplate手动调用，保持SSE协议不变
    status: completed
    dependencies:
      - create-tool-and-service
      - modify-product-mapper
  - id: update-frontend-suggestions
    content: 更新前端ai-chat/index.vue的推荐问题和placeholder文案，体现新增的购物车操作能力
    status: completed
---

## 需求概述

将现有AI导购升级为**智能客服**，AI不仅能对话推荐商品，还能**直接操作购物车**。用户可以用自然语言说"帮我把苹果加入购物车"或"看看我的购物车有什么"，AI自动识别意图并调用后端业务接口执行操作。

## 核心功能

1. **商品知识问答**：AI基于商品知识库回答价格、参数、推荐等问题
2. **加购物车**：用户说出商品名称，AI自动查找商品并加入购物车（支持名称/数量提取）
3. **查购物车**：用户查询购物车内容，AI调用工具获取并汇总展示
4. **流式打字输出**：保持现有SSE流式效果不变
5. **保持现有UI和交互**：前端不做大改，仅微调推荐问题文案

## 约束

- 前端SSE协议（event:message / event:done / event:error）不变
- 会话管理（AiSession/AiMessage表）不变
- 用户登录鉴权（JwtContextHolder）不变
- 保持向后兼容，现有功能不受影响

## 技术选型

| 技术 | 选择 | 理由 |
| --- | --- | --- |
| AI框架 | **LangChain4j 1.0.0-beta2** | 参考项目（ptu-meiduo-backend-ai）使用相同方案，@AiService + @Tool成熟 |
| LLM接入 | langchain4j-open-ai模块 | SiliconFlow提供OpenAI兼容API，可复用现有api-key |
| 对话记忆 | MessageWindowChatMemory | 保持最近20轮上下文 |
| 商品检索 | 自定义ContentRetriever | 包装现有ProductRagService的searchByQuery关键词搜索 |
| SSE流式 | SseEmitter + Flux适配 | 保持前端现有协议不变 |
| 模型 | DeepSeek-V3（硅基流动） | 支持Function Calling，当前已在用 |


## 实现方案

### 架构变更

```
现状：  前端SSE → AiChatController → AiChatServiceImpl
          → RestTemplate手动调SiliconFlow API（纯对话，无Function Calling）

改造后：前端SSE → AiChatController → AiChatServiceImpl（仅streamChat改）
          → AiCustomerService(@AiService)
               ├── CartTool(@Tool "加购物车") → CartService.addToCart()
               ├── CartTool(@Tool "查购物车") → CartService.getCartList()
               ├── ProductRagContentRetriever → ProductRagService.searchByQuery()
               └── SiliconFlow Chat API（由LangChain4j自动管理）
```

### 设计决策

1. **会话管理保持独立**：创建/删除/列表/历史消息继续走MySQL t_ai_session/t_ai_message表，LangChain4j的ChatMemory仅用于运行时的上下文维护
2. **只替换streamChat内部逻辑**：AiChatService接口不变，只重写streamChat方法的AI调用部分，会话CRUD不动
3. **适配现有SSE协议**：AiCustomerService返回Flux&lt;String&gt;，AiChatServiceImpl负责将Flux转为SseEmitter的event:message推送，前端零改动
4. **商品ID解析方式**：CartTool.addCart接收商品名称参数，内部调用ProductRagService.searchByQuery查找最匹配商品获取ID，再调CartService

### ContentRetriever设计

实现LangChain4j的ContentRetriever接口，包装ProductRagService：

```java
@Component
public class ProductRagContentRetriever implements ContentRetriever {
    @Autowired private ProductRagService productRagService;

    @Override
    public List<Content> retrieve(Query query) {
        List<Map<String, Object>> products = productRagService.searchByQuery(query.text(), 5);
        return products.stream()
            .map(p -> Content.from("商品名：" + p.get("name") + "，价格：" + p.get("price")
                + "元，销量：" + p.get("salesCount") + "，ID：" + p.get("id")))
            .collect(Collectors.toList());
    }
}
```

## 目录结构变更

```
server/pom.xml                                         # [MODIFY] +langchain4j依赖
server/src/main/resources/application.yml              # [MODIFY] +langchain4j配置项
server/src/main/java/com/char1234/
  config/AiConfig.java                                 # [NEW] LangChain4j Bean配置
  ai/AiCustomerService.java                            # [NEW] @AiService接口定义
  ai/CartTool.java                                     # [NEW] 购物车@Tool工具类
  ai/ProductRagContentRetriever.java                   # [NEW] ContentRetriever适配器
  service/impl/AiChatServiceImpl.java                  # [MODIFY] 替换streamChat内部逻辑
  mapper/ProductMapper.java                            # [MODIFY] +selectByName方法
client-front/src/views/ai-chat/index.vue              # [MODIFY] 更新推荐问题文案
```

## 数据流

```mermaid
sequenceDiagram
    participant User as 用户
    participant UI as Vue3聊天页
    participant Ctrl as AiChatController
    parent participant Svc as AiChatServiceImpl
    participant AI as AiCustomerService(@AiService)
    participant Cart as CartTool
    participant RAG as ProductRagContentRetriever
    participant LLM as SiliconFlow API

    User->>UI: "帮我把草莓加2斤到购物车"
    UI->>Ctrl: POST /api/ai/chat/{sessionId} (FormData)
    Ctrl->>Svc: streamChat(userId, sessionId, text, emitter)
    Svc->>Svc: 保存用户消息到MySQL
    Svc->>AI: streamChat(memoryId=userId, message)
    AI->>RAG: ContentRetriever检索商品
    RAG-->>AI: 返回商品列表（含"草莓"的ID和价格）
    AI->>Cart: Tool Calling → addCart(userId, 商品ID, 2)
    Cart->>Cart: CartService.addToCart()
    Cart-->>AI: 返回"添加成功"
    AI->>LLM: 调用DeepSeek生成自然语言回复
    LLM-->>AI: 流式输出"已成功加2斤草莓到购物车"
    AI-->>Svc: Flux&lt;String&gt; 流式token
    Svc-->>Ctrl: SseEmitter推送event:message
    Ctrl-->>UI: SSE数据流
    UI-->>User: 逐字显示"已成功加2斤草莓到购物车"
```

## 关键代码结构

### AiCustomerService(@AiService)

```java
@AiService(
    chatModel = "chatModel",
    streamingChatModel = "streamingChatModel",
    chatMemoryProvider = "chatMemoryProvider",
    contentRetriever = "contentRetriever",
    tools = {"cartTool"}
)
public interface AiCustomerService {
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> streamChat(@MemoryId String memoryId, @UserMessage String message);
}
```

### CartTool

```java
@Component
public class CartTool {
    @Autowired private CartService cartService;
    @Autowired private ProductRagService productRagService;

    @Tool("添加商品到购物车。注意：如果用户说商品名称，先用searchProduct搜索获取商品ID后再调用")
    public String addCart(@ToolMemoryId String userId,
                          @P("商品ID") Long productId,
                          @P("数量，默认为1") Integer quantity) {
        cartService.addToCart(Long.valueOf(userId), productId, quantity != null ? quantity : 1);
        return "添加成功";
    }

    @Tool("根据商品名称搜索商品，返回商品列表含ID、名称、价格")
    public String searchProduct(@P("搜索关键词") String keyword) {
        List<Map<String, Object>> products = productRagService.searchByQuery(keyword, 5);
        if (products.isEmpty()) return "未找到相关商品";
        return products.stream().map(p ->
            "ID=" + p.get("id") + " 名称=" + p.get("name") + " 价格=" + p.get("price") + "元"
        ).collect(Collectors.joining("\n"));
    }

    @Tool("查询当前用户的购物车内容")
    public String selectCart(@ToolMemoryId String userId) {
        List<CartItem> items = cartService.getCartList(Long.valueOf(userId));
        if (items.isEmpty()) return "购物车是空的";
        return "您的购物车有" + items.size() + "件商品:\n" + items.stream()
            .map(i -> i.getProductName() + " × " + i.getQuantity() + " = " + (i.getPrice() * i.getQuantity()) + "元")
            .collect(Collectors.joining("\n"));
    }
}
```

## 工具扩展

无需使用额外Agent扩展，纯后端代码改造。