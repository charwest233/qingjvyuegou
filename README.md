# 🛒 悦选商城 — 全栈电商平台

> Vue 3 + Spring Boot 3 + LangChain4j AI 智能导购 | 前后端分离 | 双端覆盖

一个功能完备的 B2C 电商平台，覆盖浏览→加购→下单→支付→物流→评价的完整交易链路，并深度集成基于大语言模型的 **AI 智能导购**（流式对话 / 多模态识别与生成 / Tool Calling / RAG 语义搜索）。

> **演示 GIF 预留区** — 将录制的 GIF 替换以下占位图即可展示效果

| AI 智能导购 | 商品浏览与搜索 | 购物车与结算 |
|:---:|:---:|:---:|
| ![AI导购](./docs/gifs/ai-chat.gif) | ![商品浏览](./docs/gifs/product.gif) | ![购物车](./docs/gifs/cart.gif) |

| 订单管理 | 物流追踪 | ECharts 仪表盘 |
|:---:|:---:|:---:|
| ![订单](./docs/gifs/orders.gif) | ![物流](./docs/gifs/tracking.gif) | ![仪表盘](./docs/gifs/dashboard.gif) |

| 人工客服 (WebSocket) | 优惠券抽奖 | 售后管理 |
|:---:|:---:|:---:|
| ![客服](./docs/gifs/customer-service.gif) | ![优惠券](./docs/gifs/coupon.gif) | ![售后](./docs/gifs/refund.gif) |

---

## ✨ 核心亮点

| 亮点 | 说明 |
|:---|:---|
| 🤖 **AI 智能导购** | SSE 流式对话 + 多模态图片识别/生成 + Tool Calling 操控购物车 + RAG 商品语义搜索 |
| 🛍️ **完整交易链路** | 登录注册 → 商品浏览搜索 → 购物车 → 结算下单 → 支付宝支付 → 物流追踪 → 评价售后 |
| 💬 **人工客服** | WebSocket 双向实时通信，用户端与管理端即时聊天，消息持久化到数据库 |
| 🎫 **优惠券系统** | 每日抽奖领取优惠券，下单自动计算抵扣金额 |
| 📊 **数据可视化** | ECharts 销售趋势/热门商品/订单统计仪表盘 |
| 🚚 **物流追踪** | 发货后模拟物流轨迹，可视化展示运输节点与坐标 |
| 🎨 **响应式 UI** | Tailwind CSS + Element Plus，移动端/桌面端自适应 |
| 🔐 **JWT 认证** | 无状态 Token 鉴权 + 拦截器校验 + 路由守卫 |
| 📱 **双端覆盖** | 用户端 Web（Vue 3）+ 独立管理后台（Vue 3） |

---

## 🛠️ 技术栈

### 前端

| 技术 | 版本 | 用途 |
|:---|:---|:---|
| Vue 3 | Composition API + `<script setup>` | 前端框架 |
| Vite | 5 | 构建工具 |
| Vue Router 4 | — | 路由（懒加载 + 路由守卫） |
| Pinia | — | 状态管理 |
| Element Plus | — | UI 组件库 |
| Tailwind CSS | — | 原子化样式 |
| ECharts + vue-echarts | — | 数据可视化 |
| Axios | — | HTTP 通信（拦截器封装） |
| Lucide Vue Next | — | 图标库 |
| SCSS | — | 样式预处理 |

### 后端

| 技术 | 版本 | 用途 |
|:---|:---|:---|
| Spring Boot | 3.2.5 | 主框架 |
| Java | 17 | 运行环境 |
| MyBatis Plus | 3.5.5 | ORM 持久层 |
| MySQL | 8.0 | 关系数据库 |
| LangChain4j | 1.15.1 | AI Agent 框架 |
| DeepSeek V3 / Qwen3-VL / Kolors | (SiliconFlow) | 大语言模型 / 视觉模型 / 图片生成 |
| JWT (jjwt) | 0.9.1 | 用户认证 |
| WebSocket (Spring) | — | 人工客服实时通信 |
| Knife4j / SpringDoc | 2.5 | API 接口文档 |
| Alipay SDK | — | 支付宝沙箱支付 |

---

## 🏗️ 项目架构

```
┌──────────────────────────────────────────────────────────────┐
│                       客户端层                                │
│  ┌──────────────────────┐  ┌────────────────────────────┐   │
│  │  client-front (用户端)│  │  client (管理后台)          │   │
│  │  24 条路由 / 25 个页面│  │  8 条路由 / 9 个页面       │   │
│  └──────────┬───────────┘  └──────────────┬─────────────┘   │
│             │     HTTP / SSE / WebSocket   │                 │
├─────────────┼─────────────────────────────┼─────────────────┤
│             │        表现层                 │                 │
│             ▼                              ▼                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │               Controller (18 个)                      │   │
│  │     JWT 拦截器 → REST API / SSE / WebSocket           │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                     │
│             业务层     │                                     │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │               Service (12 个)                         │   │
│  │   AI 导购 · 商品 · 订单 · 购物车 · 支付 · 售后        │   │
│  │   客服 · 优惠券 · 评价 · 收藏 · 物流 · 仪表盘         │   │
│  └─────────────────────┬────────────────────────────────┘   │
│                        │                                     │
│             持久层     │                                     │
│  ┌─────────────────────▼────────────────────────────────┐   │
│  │     MyBatis Plus Mapper + XML                         │   │
│  │     MySQL 8.0（12 张表）                               │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│              AI 层                                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  LangChain4j → DeepSeek V3 / Qwen3-VL / Kolors        │   │
│  │  @Tool (CartTool) · RAG · ChatMemory · TokenStream    │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│      🖥️  Spring Boot 3.2.5 + Java 17                        │
└──────────────────────────────────────────────────────────────┘
```

---

## 🤖 AI 智能导购（核心功能）

### 功能矩阵

| 能力 | 实现方式 | 说明 |
|:---|:---|:---|
| **流式对话** | SSE + TokenStream | AI 回复逐字流式输出，前端 `fetch` + `ReadableStream` 逐块读取 |
| **多模态识别** | Qwen2.5-VL 系列 | 用户上传图片 → base64 编码 → 视觉模型理解图片内容 |
| **多模态生成** | Kolors 模型 | 自然语言描述 → 调用 SiliconFlow Images API → 网页直接显示生成图片 |
| **Tool Calling** | LangChain4j @Tool | AI 自主调用 `searchProduct` / `addCart` / `selectCart` / `removeFromCart` / `clearCart` |
| **RAG 语义搜索** | ContentRetriever | 自然语言输入 → 向量匹配 → 召回相关商品，带相似度评分和兜底推荐 |
| **对话记忆** | MessageWindowChatMemory | 每会话保留最近 20 条历史，支持上下文连续对话 |
| **模型切换** | 前端模型选择器 | DeepSeek V3（文本）/ Qwen3-VL（视觉）/ Kolors（生图） |

### 前端 SSE 流式渲染优化

```
fetch + ReadableStream 逐块读取
    ├─ TextDecoder({ stream: true }) → 解决中文跨 chunk 乱码
    ├─ requestAnimationFrame 防抖渲染 → 合并高频更新
    ├─ [...messages] 数组引用替换 → 触发 Vue 3 响应式增量更新
    └─ 60ms 滚动节流 → 避免 DOM 频繁重排
```

### 对话示例

| 用户输入 | AI 行为 |
|:---|:---|
| "帮我搜一下耳机" | 调用 `searchProduct("耳机")` → 返回商品列表 |
| "把第一个加到购物车" | 解析上条搜索结果 → 调用 `addCart(userId, pid, 1)` |
| "看看我的购物车有什么" | 调用 `selectCart(userId)` → 整理回复 |
| "清空购物车" | 调用 `clearCart(userId)` → 确认清空 |
| [上传商品图片] + "这是什么" | 视觉模型识别图片内容并描述 |
| "生成一只卡通猫" | Kolors 模型生成图片 → 网页直接展示 |

---

## 📄 功能页面

### 用户端（25 个页面）

| 页面 | 路由 | 功能 |
|:---|:---|:---|
| 登录/注册 | `/login` | 手机号密码登录 + 短信验证码注册（含眼球动效 UI） |
| 首页 | `/` | 商品分类导航 + Banner 轮播 + 推荐商品 |
| 商品列表 | `/product-list` | 搜索 + 分类筛选 + 价格排序 + 分页 |
| 商品详情 | `/product/:id` | 主图展示 + 商品信息 + 加购 |
| 购物车 | `/cart` | 全选/反选 + 数量编辑 + 删除 + 合计金额结算 |
| 结算下单 | `/checkout` | 地址选择 + 商品确认 + 优惠券选择 + 金额预览 |
| 支付页面 | `/payment/:id` | 支付宝支付跳转 / 模拟支付 |
| 支付成功 | `/payment/success` | 支付结果展示 |
| 我的订单 | `/orders` | 订单列表 + 状态筛选 + 取消/确认收货 |
| 物流追踪 | `/orders/tracking/:orderId` | 模拟物流轨迹 + 节点可视化 |
| 收货地址 | `/address` | 地址 CRUD + 默认地址设置 |
| 我的评价 | `/user/reviews` | 已评价订单列表 |
| 收藏夹 | `/favorites` | 收藏商品管理 |
| 个人中心 | `/user` | 用户信息 + 统计数据 |
| 系统设置 | `/settings` | 个人偏好配置 |
| **AI 导购** | `/ai-chat` | SSE 流式对话 + 图片识别/生成 + 模型切换 |
| **人工客服** | `/customer-service` | WebSocket 实时聊天 |
| **优惠券抽奖** | `/coupon/draw` | 每日转盘抽奖 |
| **我的优惠券** | `/coupon/my` | 优惠券列表 + 使用状态 |
| **申请售后** | `/orders/refund/apply/:orderId` | 退款/退货申请 |
| **售后列表** | `/orders/refund/list` | 售后记录 |
| **售后详情** | `/orders/refund/:id` | 售后进度查看 |
| 管理评价 | `/admin/reviews` | 评价审核管理 |

### 管理后台（9 个页面）

| 页面 | 路由 | 功能 |
|:---|:---|:---|
| 管理员登录 | `/login` | 管理员账号登录 |
| **仪表盘** | `/dashboard` | ECharts 销售趋势 + 热门商品 + 最新订单 + 核心指标统计 |
| 商品管理 | `/products` | 商品 CRUD + 上下架 |
| 分类管理 | `/categories` | 分类 CRUD + 排序 |
| 订单管理 | `/orders` | 订单列表 + 发货 + 状态变更 |
| 用户管理 | `/users` | 用户列表 + 状态管理 |
| **人工客服** | `/customer-service` | WebSocket 管理端接待用户 |
| 售后管理 | `/refunds` | 审核退款 + 确认收货完成退款 |

---

## 🗄️ 数据库设计

| 表名 | 说明 |
|:---|:---|
| `t_user` | 用户表（手机号/邮箱/密码/微信 openid） |
| `t_admin` | 管理员表 |
| `t_category` | 商品分类 |
| `t_product` | 商品（含评分/评价数) |
| `t_cart` | 购物车 |
| `t_order` | 订单（含支付/物流/优惠券字段） |
| `t_order_item` | 订单商品项 |
| `t_user_address` | 收货地址 |
| `t_product_favorite` | 商品收藏 |
| `t_product_review` | 商品评价 |
| `t_ai_session` | AI 对话会话 |
| `t_ai_message` | AI 对话消息（含图片 URL） |
| `t_coupon` / `t_user_coupon` / `t_coupon_draw` | 优惠券系统（3 张表） |
| `t_order_refund` | 售后/退款申请表 |
| `t_customer_service_message` | 客服消息记录 |

详细 ER 图参见：[`er_diagram_chen.md`](./er_diagram_chen.md)

---

## 📡 API 接口

| 模块 | 前缀 | 核心能力 |
|:---|:---|:---|
| 商品 | `/api/product` | 分页列表/详情/CRUD/RAG 语义搜索 |
| 分类 | `/api/category` | 分类树/CRUD/排序 |
| 购物车 | `/api/cart` | 增删改查/全选/合并本地购物车 |
| 订单 | `/api/order` | 创建/支付/发货/取消/确认/物流追踪 |
| 地址 | `/api/user/addresses` | 收货地址 CRUD + 默认设置 |
| 收藏 | `/api/user/favorites` | 收藏/取消/列表/检查 |
| 评价 | `/api/review` | 提交/商品评价/用户评价/管理 |
| 用户 | `/api/user` | 微信登录/密码登录/注册/信息更新 |
| 管理员 | `/api/admin` | 登录/信息/密码修改 |
| **AI 导购** | `/api/ai` | SSE 流式对话/会话管理/模型列表 |
| **客服** | `/api/customer-service` | WebSocket + 消息历史 |
| **优惠券** | `/api/coupon` | 抽奖/列表/可用券筛选 |
| **售后** | `/api/refund` + `/api/admin/refund` | 申请/审核/退货物流 |
| **支付** | `/api/alipay` | 支付宝支付/回调通知 |
| **仪表盘** | `/api/dashboard` | 统计数据/销售趋势/热门商品 |

启动后端后访问 API 文档：`http://localhost:8080/doc.html`

---

## 🚀 本地开发

### 环境要求

| 工具 | 版本 |
|:---|:---|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |

### 1. 数据库初始化

```bash
# 导入基础建表脚本
mysql -u root -p < db_mall.sql

# 导入增量迁移脚本（AI对话/评价/客服/优惠券/支付/物流/售后）
mysql -u root -p < db_migration.sql
```

### 2. 后端启动

```bash
cd server
# 创建 application-dev.yml 配置敏感信息（API Key / 数据库密码等）
mvn spring-boot:run
```

访问：
- 后端 API：`http://localhost:8080`
- API 文档：`http://localhost:8080/doc.html`

### 3. 用户端前端启动

```bash
cd client-front
npm install
npm run dev
```

访问：`http://localhost:5173`

### 4. 管理后台启动

```bash
cd client
npm install
npm run dev
```

访问管理端独立端口（查看 `client/vite.config.js` 配置）

### 5. AI 功能配置

在 `server/src/main/resources/application-dev.yml` 中配置：

```yaml
ai:
  siliconflow:
    api-key: your-siliconflow-api-key   # 硅基流动 API Key
```

支持的模型：
- `deepseek-ai/DeepSeek-V3` — 纯文本对话（默认推荐）
- `Qwen/Qwen3-VL-30B-A3B-Instruct` — 图片识别
- `Qwen/Qwen3-VL-8B-Instruct` — 图片识别（轻量）
- `Kwai-Kolors/Kolors` — 图片生成

---

## 📁 项目结构

```
电商平台项目/
├── client/                     # 管理后台（Vue 3）
│   ├── src/
│   │   ├── views/              #   9 个管理页面
│   │   ├── api/                #   接口封装
│   │   ├── router/             #   路由配置
│   │   └── stores/             #   状态管理
│   ├── vite.config.js
│   └── package.json
├── client-front/               # 用户端前端（Vue 3）
│   ├── src/
│   │   ├── api/                #   接口封装 (axios)
│   │   ├── components/         #   8 个公共组件
│   │   ├── router/             #   路由配置（24 条）
│   │   ├── stores/             #   Pinia 状态管理（购物车/商品）
│   │   ├── utils/              #   工具函数
│   │   └── views/              #   25 个页面
│   ├── vite.config.js
│   └── package.json
├── server/                     # Spring Boot 后端
│   └── src/main/
│       ├── java/com/char1234/
│       │   ├── ai/             #   AI Agent (LangChain4j Tool + RAG)
│       │   ├── config/         #   配置类 (JWT / WebSocket / AI)
│       │   ├── controller/     #   REST 控制器 (18 个)
│       │   ├── dto/            #   数据传输对象
│       │   ├── entity/         #   实体类
│       │   ├── mapper/         #   MyBatis Mapper 接口
│       │   ├── service/        #   业务层接口 + 实现
│       │   └── websocket/      #   WebSocket 客服端点
│       └── resources/
│           ├── mapper/         #   SQL XML 映射文件
│           └── application.yml #   公共配置
├── db_mall.sql                 # 数据库建表脚本
├── db_migration.sql            # 增量迁移脚本
├── er_diagram_chen.md          # ER 图文档
├── design-system/              # 设计系统规范
└── README.md
```

---

## 📄 License

MIT
