---
name: dual-resume-generation
overview: 基于简历原文docx，修改生成两份定向简历docx：1）前端实习生版（侧重前端深度，后端AI辅助一笔带过）；2）全栈实习生版（Spring Boot+MyBatis+MQ侧重，同时保持前端优势）
todos:
  - id: edit-frontend-resume
    content: Use [skill:docx] to edit document.xml for Version A — replace 6 key text segments (target position unchanged, backend skill wording unchanged, modify project descriptions for e-commerce + psychology projects)
    status: completed
  - id: pack-frontend-resume
    content: Use [skill:docx] to pack edited XML into 曾宇函_前端开发实习生.docx
    status: completed
    dependencies:
      - edit-frontend-resume
  - id: copy-unpack-for-fullstack
    content: Copy original unpacked XML to a new directory for Version B editing
    status: completed
    dependencies:
      - pack-frontend-resume
  - id: edit-fullstack-resume
    content: Use [skill:docx] to edit document.xml for Version B — replace all 6 key segments with full-stack enhanced descriptions, change target position to 全栈开发实习生
    status: completed
    dependencies:
      - copy-unpack-for-fullstack
  - id: pack-fullstack-resume
    content: Use [skill:docx] to pack edited XML into 曾宇函_全栈开发实习生.docx
    status: completed
    dependencies:
      - edit-fullstack-resume
  - id: validate-both
    content: Use [skill:docx] to validate both output docx files are well-formed and contain correct content
    status: completed
    dependencies:
      - pack-frontend-resume
      - pack-fullstack-resume
---

## 任务目标

基于现有简历 docx（曾宇函_前端开发实习生_15259533338.docx），生成两份分岗位 Word 简历：

- **简历A**：前端开发实习生 — 侧重 Vue3/uni-app/SSE 流式优化/Markdown 渲染等前端深度，后端统一用"AI 辅助"描述避免矛盾
- **简历B**：全栈开发实习生（偏前端）— 技能栏新增 Spring Boot/MyBatis-Plus/RabbitMQ/LangChain4j/WebSocket，去掉"了解/基础"，青桔悦购项目展开后端亮点段

## 当前简历待修复问题

1. 青桔悦购"后端独立完成20+API"与技能栏"了解Spring Boot基础"自相矛盾
2. 技能栏缺少实际已实现的技术(RabbitMQ/WebSocket/RAG/LangChain4j)
3. 心语AI项目描述过薄，缺页面数、情绪分析、心绪花园等亮点
4. zoefly实习缺vue-i18n国际化、自研组件库、Canvas签名等高亮细节
5. document.xml中每个内容块重复出现2次（WPS模板双布局），两处都需同步编辑

## 技术方案

### 方案选择

使用 docx 技能的标准编辑流程：**解包 → 编辑 XML → 打包**。

- 简历原文已解包到 `d:/电商平台项目/temp_docx_unpack/`
- 编辑 `word/document.xml` 中的关键 `<w:t>` 文本节点
- 使用 `python scripts/office/pack.py` 重新打包为两份 docx

### 编辑策略

由于 document.xml 中每个内容块出现两次（WPS 模板的两个布局重复），编辑时需要用 `replace` 工具对每个目标文本执行替换，确保两处同步修改。

### 关键替换点映射

| 原文本 | 简历A（前端） | 简历B（全栈） |
| --- | --- | --- |
| `目标岗位：前端开发实习生` | 不变 | `目标岗位：全栈开发实习生` |
| `后端协作能力： 了解 Spring Boot...` | `后端协作能力：了解 Spring Boot + MyBatis-Plus 基础，可在 AI 辅助下配合后端完成接口联调与简单 CRUD 代码编写` | `后端能力：熟练使用 Spring Boot + MyBatis-Plus 进行 RESTful API 开发，掌握 RabbitMQ 延迟队列（TTL+DLX）、WebSocket 实时通信、LangChain4j AI Agent 框架集成；了解 JWT 鉴权与拦截器设计` |
| `·后端独立完成 20+ 个 RESTful API...` | `·在 AI 辅助下完成 Spring Boot 后端开发，负责接口联调与字段校验，实现跨域鉴权（JWT）、多模态 AI 导购、RabbitMQ 延迟队列等 20+ 个 API，掌握前后端联调全流程与 RESTful 架构设计` | `·后端基于 Spring Boot 3 + MyBatis-Plus 开发 18 个 Controller 共 20+ API，实现 RabbitMQ 延迟队列（TTL+DLX 订单超时自动取消并回补库存）、WebSocket 人工客服双向实时通信、LangChain4j 多模型 AI 导购（Tool Calling+RAG+多模态）、支付宝沙箱支付集成` |
| `ECharts + Tailwind + Element Plus + Spring Boot` | 不变 | `ECharts + Tailwind + Element Plus + Spring Boot + RabbitMQ + LangChain4j` |
| `ECharts + Element Plus + WangEditor + Spring boot` | 不变 | `ECharts + Element Plus + WangEditor + Spring Boot + MyBatis-Plus` |
| `· 后端在AI辅助下完成开发，基于springboot+Mybatis-plus...` | `·后端在 AI 辅助下完成基于 Spring Boot + MyBatis-Plus 的开发，实现 JWT 登录鉴权、AI 大模型流式对话（Qwen2.5-7B）、对话结束后自动情绪分析（评分/风险等级/改善建议），完成前后端联调与字段映射校验` | `·后端基于 Spring Boot + MyBatis-Plus 开发 5 个 Controller 共 21 个 API，实现 JWT 无状态认证（BCrypt 密码加密+拦截器鉴权）、OkHttp 流式调用硅基流动大模型 API（Qwen2.5-7B）、AI 情绪分析（JSON 输出解析+中文标点清洗）、ECharts 数据分析 API` |


### 其他 XML 编辑注意事项

- `<w:t xml:space="preserve">` 节点需保留 `xml:space="preserve"` 属性
- 所有文本内容中出现的特殊字符需使用 XML 实体：`&#x201C;` `&#x201D;` `&#x2019;`
- 打包命令：`python scripts/office/pack.py temp_docx_unpack/ output.docx --original 原文件.docx`