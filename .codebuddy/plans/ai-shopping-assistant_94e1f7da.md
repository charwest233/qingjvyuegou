---
name: ai-shopping-assistant
overview: 在悦选商城中新增 AI 导购助手功能：包括数据库表创建、后端 SSE 流式接口、前端 AI 对话页面、底部导航栏新增入口
design:
  architecture:
    framework: vue
  styleKeywords:
    - Modern
    - Minimal
    - Chat-UI
    - Teal-accented
    - Glassmorphism
  fontSystem:
    fontFamily: Nunito Sans
    heading:
      size: 18px
      weight: 600
    subheading:
      size: 14px
      weight: 500
    body:
      size: 15px
      weight: 400
  colorSystem:
    primary:
      - "#14B8A6"
      - "#5EEAD4"
      - "#0D9488"
    background:
      - "#F0FDFA"
      - "#FFFFFF"
      - "#F9FAFB"
    text:
      - "#134E4A"
      - "#64748B"
      - "#FFFFFF"
    functional:
      - "#14B8A6"
      - "#EF4444"
      - "#3B82F6"
todos:
  - id: create-db-tables
    content: 数据库迁移：创建 t_ai_session 和 t_ai_message 两张表
    status: completed
  - id: add-backend-deps
    content: 后端：添加 AI 实体类(AiSession/AiMessage)、Mapper 接口、Mapper XML
    status: completed
    dependencies:
      - create-db-tables
  - id: add-ai-service
    content: 后端：实现 AiChatService(AiChatServiceImpl) 调用 SiliconFlow API 流式代理
    status: completed
    dependencies:
      - add-backend-deps
  - id: add-ai-controller
    content: 后端：实现 AiChatController(REST + SSE 流式聊天接口)，修改 JwtAuthInterceptor 放行 /api/ai 路径
    status: completed
    dependencies:
      - add-ai-service
  - id: add-frontend-api
    content: 前端：创建 src/api/ai.js 封装 AI 对话 API（含 fetch SSE 流式请求）
    status: completed
  - id: update-tabbar-router
    content: 前端：修改 TabBar.vue 新增 AI 对话 Tab，修改 router/index.js 新增 /ai-chat 路由
    status: completed
  - id: create-ai-chat-page
    content: 前端：创建 AI 对话主页面（聊天区+输入区+会话列表抽屉+欢迎页+流式输出）
    status: completed
    dependencies:
      - add-frontend-api
      - update-tabbar-router
  - id: final-integration
    content: 集成配置：在 application.yml 增加 SiliconFlow API 配置，全局测试验证
    status: completed
    dependencies:
      - create-ai-chat-page
      - add-ai-controller
---

## 产品概述

在悦选商城中集成AI导购助手，用户可以通过自然语言对话获得商品选购建议。例如用户输入"我想听歌该买什么"，AI会根据商城现有商品品类推荐合适的商品，并给出选购指南。

## 核心功能

1. **底部导航栏新增"AI对话"入口** - TabBar 增加第5个Tab，图标为 MessageCircle 或 Bot
2. **AI对话界面** - 完整的聊天界面，支持多轮对话，流式输出（打字机效果）
3. **历史会话管理** - 侧边抽屉展示历史对话列表，可切换/新建/删除对话
4. **模型选择** - 支持切换不同的硅基流动模型（免费/低成本模型）
5. **多模态输入** - 支持上传图片进行多轮对话（如拍商品图让AI分析推荐）
6. **数据库持久化** - 新增两张表存储会话和消息记录

## 技术要点

- 后端代理调用硅基流动 API（兼容 OpenAI 格式），使用 SseEmitter 流式返回
- 前端使用 fetch + ReadableStream 消费 SSE 流，实现打字机输出效果
- API 密钥仅存储在服务端，不暴露给前端

## 技术栈选择

- **后端**：Spring Boot 3.2.5 + MyBatis Plus + MySQL（复用现有）
- **前端**：Vue 3 + Element Plus + Tailwind CSS + lucide-vue-next（复用现有）
- **AI API**：硅基流动 SiliconFlow API（兼容 OpenAI 格式，streaming 模式）
- **流式传输**：SseEmitter（服务端） + fetch ReadableStream（客户端）
- **HTTP 客户端**：RestTemplate（服务端调用 SiliconFlow）
- **图片上传**：复用已有文件上传机制（存本地或临时路径）

## 实现方案

### 架构设计

```
用户输入 ──→ 前端fetch SSE ──→ 后端AiChatController(SseEmitter)
                                      │
                                      ▼
                               SiliconFlow API (stream=true)
                                      │
                                      ▼
                              SSE 流转发至前端
                                      │
                                      ▼
                              前端 ReadableStream
                              逐块解析 → 追加到消息气泡
                                      │
                              流结束后完整消息存入DB
```

### 关键设计决策

1. **流式处理**：后端使用 `SseEmitter` + `@Async` 异步调用 SiliconFlow API，将响应流逐块转发给前端。前端用 `fetch` 的 `ReadableStream` 读取 SSE 数据，不依赖 EventSource（支持 POST 请求）。

2. **密钥安全**：API Key 配置在 `application.yml` 的 `ai.siliconflow.api-key` 和 `ai.siliconflow.base-url` 中，前端不感知具体密钥。

3. **消息上下文**：每次请求携带当前会话的全量历史消息（最多最近20条），保证多轮对话连续性。

4. **模型配置**：在服务端维护可用模型列表并暴露给前端配置接口，前端渲染为选择器。

5. **会话标题**：自动使用用户第一条消息的前15个字作为会话标题。

### 硅基流动模型列表

| 模型ID | 说明 | 成本 |
| --- | --- | --- |
| Qwen/Qwen2.5-7B-Instruct | 通义千问2.5 7B | 免费 |
| THUDM/glm-4-9b-chat | GLM-4 9B 对话 | 免费 |
| deepseek-ai/DeepSeek-V2.5 | DeepSeek V2.5 | 免费 |
| Qwen/Qwen2-7B-Instruct | 通义千问2 7B | 免费 |


### 数据库设计

```sql
-- AI 对话会话表
CREATE TABLE `t_ai_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(255) DEFAULT '' COMMENT '对话标题(自动用首条消息截取)',
  `model` varchar(100) NOT NULL DEFAULT 'Qwen/Qwen2.5-7B-Instruct' COMMENT '使用的模型',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话';

-- AI 对话消息表
CREATE TABLE `t_ai_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色: user/assistant/system',
  `content` text COMMENT '文本内容',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL(多模态)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `t_ai_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息';
```

### 后端 API 设计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/ai/session | 创建新会话 |
| GET | /api/ai/session/list | 获取用户会话列表 |
| DELETE | /api/ai/session/{id} | 删除会话（级联删除消息） |
| GET | /api/ai/session/{id}/messages | 获取某会话的消息列表 |
| POST | /api/ai/chat/{sessionId} | 发送消息并流式返回 AI 回复（SSE） |
| GET | /api/ai/models | 获取可用模型列表 |


### 目录结构

```
## 服务端新增文件
server/
├── src/main/java/com/char1234/
│   ├── controller/
│   │   └── AiChatController.java            # [NEW] AI聊天控制器
│   ├── service/
│   │   ├── AiChatService.java               # [NEW] AI聊天服务接口
│   │   └── AiChatServiceImpl.java           # [NEW] 实现(SiliconFlow代理+SSE)
│   ├── entity/
│   │   ├── AiSession.java                   # [NEW] 会话实体
│   │   └── AiMessage.java                   # [NEW] 消息实体
│   └── mapper/
│       ├── AiSessionMapper.java             # [NEW] 会话Mapper
│       └── AiMessageMapper.java             # [NEW] 消息Mapper
├── src/main/resources/
│   ├── application.yml                      # [MODIFY] 新增 ai.siliconflow 配置
│   └── mapper/
│       └── AiSessionMapper.xml              # [NEW] 会话SQL映射
│       └── AiMessageMapper.xml              # [NEW] 消息SQL映射
├── pom.xml                                  # [MODIFY] 可能不需要新依赖

## 前端新增/修改文件
client-front/
├── src/views/ai-chat/
│   └── index.vue                            # [NEW] AI聊天主页面(聊天区+会话列表+输入区)
├── src/components/
│   ├── AiChatMessage.vue                    # [NEW] 消息气泡组件
│   └── TabBar.vue                           # [MODIFY] 新增AI对话Tab
├── src/api/
│   └── ai.js                                # [NEW] AI聊天API封装
├── src/router/
│   └── index.js                             # [MODIFY] 新增/ai-chat路由
├── src/styles/
│   └── index.css                            # [MODIFY] 可选:新增AI相关样式
├── tailwind.config.js                       # [MODIFY] 可选:新增打字机闪烁动画
└── vite.config.js                           # [NOT MODIFIED] 已有/api代理无需修改
```

### 关键实现细节

1. **JWT 鉴权**：在 `JwtAuthInterceptor` 的 `authorize()` 方法中为 MP_USER 添加 `/api/ai/**` 路径的放行规则（所有方法均允许）。

2. **SSE 流式聊天（核心）**：

- 后端 `AiChatController` 的 `/api/ai/chat/{sessionId}` 接口：

    1. 接收用户输入（文本+可选图片）
    2. 查询会话当前所有消息（作为上下文）
    3. 保存用户消息到数据库
    4. 创建 `SseEmitter`（超时120秒）
    5. 异步线程中调用 SiliconFlow API（stream=true）
    6. 逐块解析 SSE 响应，通过 `emitter.send()` 转发，并累积完整回复
    7. 流结束后，将完整 AI 回复存入数据库
    8. 异常时发送 error 事件并设置 emitter.completeWithError

- 前端使用 `fetch('/api/ai/chat/' + sessionId, { method: 'POST', body: formData })` 获取 ReadableStream，逐行解析 `data: {...}` 格式的 SSE 数据，实时追加到当前消息气泡。

3. **多模态图片输入**：

- 使用 `multipart/form-data` 提交用户消息（文本 + 可选的图片文件）。
- 后端接收后保存图片到本地静态资源目录，生成 URL，构建消息对象发送给 SiliconFlow（对于支持视觉的模型如 Qwen-VL 可以传入 image_url）。

4. **模型选择**：前端使用 ElSelect 下拉选择模型，默认使用 Qwen/Qwen2.5-7B-Instruct，切换后只影响新创建的会话。

### 注意事项

- 后端需要启用 `@EnableAsync` 以支持异步 SSE 转发
- 硅基流动 API 超时设置为 60 秒
- 前端 axios 的 15s 超时限制不适用于 SSE 流式请求（SSE 使用原生 fetch）

## 设计风格

采用现代极简聊天界面风格，与悦选商城青绿色主题保持一致。

### 整体布局

整个 AI 对话页面采用三块纵向布局：

1. **顶部导航区**：半透明毛玻璃效果背景，左侧为"返回/历史会话"按钮（Bot 图标 + "AI导购"标题），右侧为模型选择下拉框
2. **中间聊天区**：全屏滚动消息列表，背景为极浅青绿渐变
3. **底部输入区**：固定底部，包含图片上传按钮 + 文本输入框 + 发送按钮

### 会话列表抽屉

从左侧滑出的抽屉面板，顶部带"新建对话"按钮，下方为按时间倒序排列的会话卡片列表，每条显示标题、模型名和日期。

### 消息气泡设计

- **用户消息**：右对齐，白色圆角气泡+青绿色左边框装饰
- **AI消息**：左对齐，浅青绿背景气泡（bg-primary/5），带机器人头像
- **流式输出**：打字机效果，文本逐字出现，末尾带闪烁光标动画
- **空状态**：聊天区无消息时显示居中欢迎界面，带AI机器人卡通图标和引导语

### 颜色系统

- 主色沿用 #14B8A6 (teal-500) 作为 AI 品牌色
- 输入框聚焦边框、发送按钮、选中项均为主色
- AI 消息气泡使用 bg-primary/5 浅背景
- 欢迎页面的装饰元素使用主色渐变

### 交互细节

- 消息进入时有淡淡的 fade-in 动画
- AI 回复尾部有打字闪烁光标（利用 tailwindcss-animate 的 pulse 动画）
- 发送按钮在输入为空时置灰
- 图片上传后显示缩略图预览，可点击删除
- 会话抽屉有平滑的 slide 过渡动画
- 加载历史消息时显示骨架屏

## Agent Extensions

### SubAgent

- **code-explorer**: 用于在探索阶段扫描前后端项目结构、查找现有模式文件、确认JWT鉴权规则等，确保计划的可靠性和完整性。