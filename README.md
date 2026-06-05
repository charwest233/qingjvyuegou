# 🛒 悦选商城 — 全栈电商平台

> Vue 3 + Spring Boot 3 + LangChain4j AI 智能导购 | 前后端分离 | 微信小程序

一个面向 C 端消费者的全功能电商平台，涵盖商品浏览、购物车、下单、评价、收藏等完整交易链路，并集成了基于大语言模型的 **AI 智能导购客服**。

---

## ✨ 核心亮点

| 亮点 | 说明 |
|------|------|
| 🤖 **AI 智能导购** | DeepSeek V3 + RAG 知识库 + Tool Calling，实现商品搜索、加购、查购物车等自然语言交互 |
| 📱 **多端覆盖** | Web 端（Vue 3）+ 微信小程序端 |
| 🎨 **现代化 UI** | Tailwind CSS + Element Plus + ECharts 数据可视化 |
| 🔐 **安全认证** | JWT Token 无状态认证 + 拦截器校验 |
| 📐 **标准化架构** | Controller → Service → Mapper 三层架构，MyBatis Plus 持久层 |
| 📖 **API 文档** | Knife4j / SpringDoc OpenAPI 自动生成在线接口文档 |

---

## 🛠️ 技术栈

### 前端（Web）

| 技术 | 用途 |
|------|------|
| Vue 3 (Composition API) | 前端框架 |
| Vite 5 | 构建工具 |
| Element Plus | UI 组件库 |
| Tailwind CSS | 原子化样式 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由 |
| ECharts + vue-echarts | 数据可视化 |
| Lucide Vue Next | 图标库 |

### 前端（小程序）

| 技术 | 用途 |
|------|------|
| 微信小程序原生 | 移动端用户入口 |

### 后端

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2.5 | 主框架 |
| Java 17 | 运行环境 |
| MyBatis Plus 3.5.5 | ORM 持久层 |
| MySQL 8.0 | 关系数据库 |
| Redis | 缓存 / Token 管理 |
| LangChain4j 1.15.1 | AI Agent 框架 |
| DeepSeek V3 (SiliconFlow) | 大语言模型 |
| JWT (jjwt 0.9.1) | 用户认证 |
| SpringDoc OpenAPI 2.5 | 接口文档 |
| WebSocket | 实时通信 |
| Lombok | 代码简化 |

---

## 🏗️ 项目架构

```
┌──────────────────────────────────────────────────────┐
│                    客户端层                           │
│  ┌─────────────────┐  ┌──────────────────────────┐  │
│  │  Vue 3 Web 端    │  │  微信小程序端              │  │
│  └────────┬────────┘  └────────────┬─────────────┘  │
│           │        HTTP / SSE      │                 │
├───────────┼───────────────────────┼─────────────────┤
│           │      表现层             │                 │
│           ▼                        ▼                 │
│  ┌───────────────────────────────────────────────┐  │
│  │          Controller (13 个)                    │  │
│  │   JWT 拦截器 → REST API / SSE 流式             │  │
│  └──────────────────┬────────────────────────────┘  │
│                     │                                │
│          业务层     │                                │
│  ┌──────────────────▼────────────────────────────┐  │
│  │          Service (12 个接口 + 11 实现)          │  │
│  │   AI Chat · 商品 · 订单 · 购物车 · 评价 ...     │  │
│  └──────────────────┬────────────────────────────┘  │
│                     │                                │
│          持久层     │                                │
│  ┌──────────────────▼────────────────────────────┐  │
│  │     MyBatis Plus Mapper + XML                  │  │
│  │     MySQL 8.0 (9 张表)  │  Redis 缓存          │  │
│  └───────────────────────────────────────────────┘  │
│           AI 层                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  LangChain4j → DeepSeek V3 (SiliconFlow)      │  │
│  │  @AiService · @Tool · RAG · ChatMemory · SSE   │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│   🖥️  Spring Boot 3.2.5 + Java 17                  │
└──────────────────────────────────────────────────────┘
```

---

## 🤖 AI 智能导购（核心功能）

### 技术架构

```
用户输入 → AiChatServiceImpl
              │
              ├──→ ChatMemory（对话记忆，最近 20 条）
              ├──→ ContentRetriever（RAG 商品知识库检索）
              ├──→ CartTool（5 个 @Tool 工具，LLM 自主调用）
              │      ├─ searchProduct    商品搜索
              │      ├─ addCart          添加到购物车
              │      ├─ selectCart       查询购物车
              │      ├─ removeFromCart   移除商品
              │      └─ clearCart        清空购物车
              │
              └──→ TokenStream → SSE 流式逐字推送 → 前端
```

### 交互示例

| 用户输入 | AI 行为 |
|----------|---------|
| "帮我搜一下草莓" | 调用 `searchProduct("草莓")` → 返回商品列表 |
| "把草莓加到购物车" | 先搜索 → 获取 ID → 调用 `addCart(userId, 123, 1)` |
| "看看我的购物车" | 调用 `selectCart(userId)` → 组织回复 |
| "清空购物车" | 调用 `clearCart(userId)` → 确认清空 |

### 前端 SSE 流式渲染

- 使用 `fetch` + `ReadableStream` 接收 SSE 事件
- 逐字渲染 + Markdown 实时解析（加粗 / 斜体 / 列表）
- `AbortController` 支持中断请求

---

## 📄 功能页面一览

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/home` | 商品分类导航 + Banner + 推荐 |
| 商品列表 | `/product-list` | 搜索 + 筛选 + 分页 |
| 商品详情 | `/product-detail/:id` | 大图 + 规格 + 加购 |
| 购物车 | `/cart` | 全选 / 数量 / 删除 / 结算 |
| 订单确认 | `/checkout` | 地址选择 + 订单预览 |
| 我的订单 | `/orders` | 订单列表 + 状态筛选 |
| 我的收藏 | `/favorites` | 收藏商品管理 |
| 收货地址 | `/address` | 增删改查 |
| 个人中心 | `/user` | 头像 / 昵称 / 统计 |
| 我的评价 | `/user/reviews` | 评价历史 |
| AI 客服 | `/ai-chat` | 智能导购对话 |
| 登录 | `/login` | 手机号 / 微信登录 |
| 管理后台 | `/admin/reviews` | 评价审核管理 |
| 系统设置 | `/settings` | 个人偏好配置 |

---

## 🚀 本地开发

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |

### 1. 数据库初始化

```bash
mysql -u root -p < db_mall.sql
```

### 2. 后端启动

```bash
cd server
# 确保已创建 application-dev.yml（本地敏感配置，已加入 .gitignore）
mvn spring-boot:run
```

启动后访问：
- 后端 API：`http://localhost:8080`
- API 文档：`http://localhost:8080/doc.html`

### 3. 前端启动

```bash
cd client-front
npm install
npm run dev
```

启动后访问：`http://localhost:5173`

### 4. AI 功能配置

在 `server/src/main/resources/application-dev.yml` 中配置：

```yaml
ai:
  siliconflow:
    api-key: your-siliconflow-api-key   # 硅基流动 API Key
```

默认使用 DeepSeek V3 模型，可通过 `application.yml` 中的 `langchain4j.open-ai.chat-model.model-name` 切换。

### 5. 微信小程序端

`client/` 目录为微信小程序源码，通过微信开发者工具打开即可。

---

## 📁 项目结构

```
电商平台项目/
├── client/                     # 微信小程序端
├── client-front/               # Vue 3 Web 前端
│   ├── src/
│   │   ├── api/                #   接口封装 (axios)
│   │   ├── components/         #   公共组件
│   │   ├── router/             #   路由配置
│   │   ├── stores/             #   Pinia 状态管理
│   │   ├── utils/              #   工具函数
│   │   └── views/              #   页面组件 (14 个)
│   ├── vite.config.js
│   └── package.json
├── server/                     # Spring Boot 后端
│   └── src/main/
│       ├── java/com/char1234/
│       │   ├── ai/             #   AI Agent (LangChain4j)
│       │   ├── config/         #   配置类 (JWT/WebSocket/AI...)
│       │   ├── controller/     #   控制器 (13 个)
│       │   ├── dto/            #   数据传输对象
│       │   ├── entity/         #   实体类
│       │   ├── interceptor/    #   拦截器
│       │   ├── mapper/         #   MyBatis 映射接口
│       │   ├── service/        #   业务层接口 + 实现
│       │   └── websocket/      #   WebSocket 处理
│       └── resources/
│           ├── mapper/         #   SQL XML 映射文件
│           ├── application.yml #   公共配置（可提交）
│           └── system-prompt.txt  # AI 系统提示词
├── db_mall.sql                 # 数据库建表脚本
├── design-system/              # 设计系统规范文档
├── mall-cloud/                 # 微服务迁移方案
└── README.md
```

---

## 🗄️ 数据库设计

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `t_user` | 用户表 | id, open_id, phone, nickname |
| `t_admin` | 管理员 | id, username, password |
| `t_category` | 商品分类 | id, name, parent_id |
| `t_product` | 商品 | id, name, price, stock, category_id |
| `t_cart` | 购物车 | id, user_id, product_id, quantity |
| `t_order` | 订单 | id, order_no, user_id, total_amount, status |
| `t_order_item` | 订单项 | id, order_id, product_id, quantity, price |
| `t_user_address` | 收货地址 | id, user_id, receiver, phone, address |
| `t_product_favorite` | 商品收藏 | id, user_id, product_id |

详细 ER 图参见：`er_diagram_chen.md`

---

## 📡 API 接口文档

项目集成 Knife4j，启动后端后访问：

```
http://localhost:8080/doc.html
```

主要接口模块：

| 模块 | 前缀 | 说明 |
|------|------|------|
| 商品 | `/api/product` | CRUD + 分页搜索 |
| 分类 | `/api/category` | 分类树查询 |
| 购物车 | `/api/cart` | 增删改查 |
| 订单 | `/api/order` | 创建 / 查询 / 状态流转 |
| 地址 | `/api/address` | 收货地址管理 |
| 收藏 | `/api/favorite` | 收藏 / 取消 |
| 评价 | `/api/review` | 评价提交 / 审核 |
| 用户 | `/api/user` | 登录 / 更新信息 |
| AI 客服 | `/api/ai` | 对话 / 会话管理 (SSE) |
| 管理后台 | `/api/admin` | 管理员功能 |
| 仪表盘 | `/api/dashboard` | ECharts 数据统计 |

---

## 🔐 安全配置

项目通过 `application-dev.yml` 存放敏感信息（已在 `.gitignore` 中排除），`application.yml` 仅保留 `${DB_PASSWORD}`、`${SILICONFLOW_API_KEY}` 等占位符，可安全提交到代码仓库。

如需部署生产环境，通过操作系统环境变量或 `application-prod.yml` 注入真实值。

---

## 🔮 未来规划

参见 `mall-cloud/README.md` — 已规划 Spring Cloud 微服务迁移方案，将单体应用拆分为：

- `mall-gateway` — API 网关
- `mall-auth` — 认证中心
- `mall-user` / `mall-product` / `mall-order` — 业务微服务
- Nacos + Sentinel + Seata 分布式基础设施

---

## 📄 License

MIT
