# 🛒 青桔悦购 — 全栈电商平台

> Vue 3 + Spring Boot 3 + RabbitMQ + LangChain4j AI 智能导购 | 前后端分离 | 双端覆盖

一个功能完备的 B2C 电商平台，覆盖浏览→加购→下单→支付→物流→评价的完整交易链路，并深度集成基于大语言模型的 **AI 智能导购**（流式对话 / 多模态识别与生成 / Tool Calling / RAG 语义搜索）。

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 | 说明 |
|:---|:---|:---|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端依赖管理 |
| Node.js | 18+ | 前端运行环境 |
| MySQL | 8.0+ | 数据库，端口 3306 |
| RabbitMQ | 3.x+ | 消息队列，端口 5672 |
| Erlang | 25+ | RabbitMQ 依赖（安装 RabbitMQ 前先装） |

### 1. 安装 RabbitMQ（Windows）

```powershell
# 步骤 1：下载并安装 Erlang（RabbitMQ 依赖）
# https://github.com/erlang/otp/releases

# 步骤 2：下载并安装 RabbitMQ
# https://www.rabbitmq.com/docs/install-windows

# 步骤 3：启动服务（管理员 PowerShell）
net start RabbitMQ

# 步骤 4：验证是否启动成功
sc query RabbitMQ          # 查看服务状态
netstat -ano | findstr 5672  # 检查端口监听

# 步骤 5：启用管理界面
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.x.x\sbin"
rabbitmq-plugins enable rabbitmq_management

# 打开管理界面：http://localhost:15672
# 默认账号：guest / guest
```

### 2. 数据库初始化

```bash
# 在项目根目录执行
mysql -u root -p < db_mall.sql
mysql -u root -p < db_migration.sql
```

### 3. 配置敏感信息

在 `server/src/main/resources/` 下创建 `application-dev.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码

# AI 导购（必填，否则 AI 功能不可用）
ai:
  siliconflow:
    api-key: your-siliconflow-api-key
```

> 获取免费 API Key：访问 [siliconflow.cn](https://siliconflow.cn) 注册即送额度。

### 4. 启动后端

```bash
cd server
mvn spring-boot:run
```

启动成功标志：`Started ServerApplication in X seconds`

访问：
- 后端 API：`http://localhost:8080`
- 接口文档（含在线调试）：`http://localhost:8080/doc.html`

### 5. 启动前端

**用户端：**
```bash
cd client-front
npm install
npm run dev
```
访问 `http://localhost:5173`

**管理后台：**
```bash
cd client
npm install
npm run dev
```
访问 `http://localhost:5174`（端口见 `client/vite.config.js`）

### 6. 测试准备

测试账号（密码 `123456`）：手机号 `13800000100`

管理员账号：`admin` / `123456`

### 7. 验证 RabbitMQ 订单超时功能

1. 用户端下单（不支付）
2. 打开 RabbitMQ 管理界面 → Queues → 可看到 `order.delay.queue` 中有消息
3. 15 分钟后刷新订单页 → 订单自动变为"已取消"
4. 后端日志：`订单已自动取消（超时未支付）`

> 开发调试时可临时将 `application.yml` 中 `order.timeout-seconds` 改为 `30`（30 秒自动取消）

---

## 📸 项目演示

将录制的 GIF 放入 `docs/gifs/` 目录，命名对应即可展示效果。gif 等宽适配屏幕。

### AI 智能导购

<img src="./docs/gifs/ai-chat.gif" width="100%" alt="AI导购" />

### 用户端购物流程

<img src="./docs/gifs/shopping.gif" width="100%" alt="用户端购物" />

### 管理后台

<img src="./docs/gifs/admin.gif" width="100%" alt="管理后台" />

### WebSocket / 优惠券 / 物流

<img src="./docs/gifs/features.gif" width="100%" alt="WebSocket客服优惠券物流" />

---

## 🎬 如何录制演示 GIF

### 推荐工具：ScreenToGif

1. 下载 [ScreenToGif](https://www.screentogif.com/)（Windows 免费，免安装便携版可用）
2. 打开后点击「录像机」→ 框选浏览器窗口
3. 点击「录制」开始，操作完功能后点「停止」
4. 在编辑器中：
   - **裁剪多余帧**：选中开头/结尾无用帧 → Delete
   - **调整大小**：图像 → 调整大小 → 宽度设为 `800px` 左右（保持比例），文件不会太大
   - **控制帧率**：编辑 → 减少帧数 → 间隔 2 帧（减小体积）
5. 文件 → 另存为 → GIF → **20 fps / 256 色 / 2x 编码器**（质量和体积平衡）
6. 重命名为对应文件名放入 `docs/gifs/`

### 建议录制

| 文件名 | 功能 | 建议操作流程 |
|:---|:---|:---|
| `ai-chat.gif` | AI 导购 | 输入"帮我搜一下耳机"→ 等 AI 回复 → 加购操作 |
| `shopping.gif` | 购物全流程 | 首页浏览 → 商品详情 → 加购 → 结算 → 支付 |
| `admin.gif` | 管理后台 | 仪表盘 → 商品管理 → 订单管理 → 用户管理 |
| `features.gif` | 客服/优惠券/物流 | 用户端发消息 → 管理端回复 → 抽奖领券 → 物流追踪 |

### 注意事项

- 每个 GIF 控制在 **2–5 MB**，太大 GitHub 加载会很慢
- 保证浏览器窗口干净，不要露出个人书签/信息
- 录完一次性放入 `docs/gifs/`
| `tracking.gif` | 物流追踪 | 管理端发货 → 用户端点物流追踪 → 轨迹展示 |
| `dashboard.gif` | 仪表盘 | 管理端登录 → 仪表盘首页 → 销售趋势/热门商品 |
| `customer-service.gif` | 人工客服 | 用户端发消息 → 管理端打开客服页面 → 双向回复 |
| `coupon.gif` | 优惠券 | 转盘抽奖 → 领券 → 下单时选择优惠券抵扣 |
| `refund.gif` | 售后管理 | 用户申请退款 → 管理端审核 → 完成退款 |

### 注意事项

- 每个 GIF 控制在 **2–5 MB**，太大 GitHub 加载会很慢
- 保证浏览器窗口干净，不要露出个人书签/信息
- 可按清单全部录完后一次性放入 `docs/gifs/`
- 录不好的画面随时重录，命名不变即可覆盖

---

## ✨ 核心亮点

| 亮点 | 说明 |
|:---|:---|
| 🤖 **AI 智能导购** | SSE 流式对话 + 多模态图片识别/生成 + Tool Calling 操控购物车 + RAG 商品语义搜索 |
| 🛍️ **完整交易链路** | 登录注册 → 商品浏览搜索 → 购物车 → 结算下单 → 支付宝支付 → 物流追踪 → 评价售后 |
| ⏰ **订单超时取消** | RabbitMQ 延迟队列（TTL + DLX），15 分钟未支付自动取消并回补库存 |
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
| RabbitMQ | Spring AMQP | 延迟队列：订单超时自动取消 |
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
│            消息队列 & AI 层                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RabbitMQ (延迟队列) · LangChain4j (AI Agent)         │   │
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
| **多模态识别** | Qwen3-VL 系列 | 用户上传图片 → base64 编码 → 视觉模型理解图片内容 |
| **多模态生成** | Kolors 模型 | 自然语言描述 → 调用 SiliconFlow Images API → 网页直接显示生成图片 |
| **Tool Calling** | LangChain4j @Tool | AI 自主调用 `searchProduct` / `addCart` / `selectCart` / `removeFromCart` / `clearCart` |
| **RAG 语义搜索** | ContentRetriever | 自然语言输入 → 关键词提取 + 评分排序 → 召回商品 + 兜底推荐 |
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
| 订单 | `/api/order` | 创建/支付/发货/取消/确认/物流追踪/超时自动取消 |
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
│       │   ├── config/         #   配置类 (JWT / WebSocket / AI / RabbitMQ)
│       │   ├── controller/     #   REST 控制器 (18 个)
│       │   ├── dto/            #   数据传输对象
│       │   ├── entity/         #   实体类
│       │   ├── mapper/         #   MyBatis Mapper 接口
│       │   ├── mq/             #   RabbitMQ 延迟队列/生产者/消费者
│       │   ├── service/        #   业务层接口 + 实现
│       │   └── websocket/      #   WebSocket 客服端点
│       └── resources/
│           ├── mapper/         #   SQL XML 映射文件
│           └── application.yml #   公共配置
├── docs/gifs/                  # 演示 GIF 存放目录
├── db_mall.sql                 # 数据库建表脚本
├── db_migration.sql            # 增量迁移脚本
├── er_diagram_chen.md          # ER 图文档
└── README.md
```

---

## 📄 License

MIT
