---
name: branch-parallel-microservice-refactor
overview: 采用「分支并行」策略重构为模块化单体：第一周完成纯文件搬移的包结构重构（零逻辑改动），同步并行推进基础设施搭建（Nacos+RabbitMQ），新功能在重构后的域包中开发，最后统一做事件驱动的跨域解耦。
todos:
  - id: phase0-dependencies
    content: "Phase 0: pom.xml 新增 Spring Cloud Alibaba + Nacos Config + RabbitMQ 依赖和 dependencyManagement"
    status: completed
  - id: phase1-restructure-packages
    content: "Phase 1: 包结构重构——将平铺的 controller/service/entity/mapper 按7个领域分包搬移（user/product/order/review/sms/admin），调整所有文件的 package 声明和 import 路径，不修改业务逻辑"
    status: completed
  - id: phase1-cleanup-old-packages
    content: "Phase 1: 删除旧的平铺包空目录（controller/ entity/ mapper/ service/），验证编译通过"
    status: completed
    dependencies:
      - phase1-restructure-packages
  - id: phase2a-nacos-config
    content: "Phase 2a: 搭建 Nacos Config——创建 bootstrap.yml 配置 Nacos 连接地址，创建 Nacos 侧配置 data-id=mall-server.yaml，本地 application.yml 精简为兜底配置"
    status: pending
    dependencies:
      - phase0-dependencies
      - phase1-cleanup-old-packages
  - id: phase2b-rabbitmq-infra
    content: "Phase 2b: 搭建 RabbitMQ 基础设施——RabbitMQConfig 定义延迟交换机/事件交换机/队列/绑定，添加 application.yml 中 RabbitMQ 连接配置"
    status: pending
    dependencies:
      - phase0-dependencies
      - phase1-cleanup-old-packages
  - id: phase2c-event-framework
    content: "Phase 2c: 创建事件基础设施——DomainEvent 接口 + DomainEventPublisher(事务安全发送) + EventConsumerConfig(容器配置)"
    status: pending
    dependencies:
      - phase0-dependencies
      - phase1-cleanup-old-packages
  - id: phase3a-domain-events
    content: "Phase 3a: 订单域事件定义——创建 OrderCreatedEvent/OrderPaidEvent/OrderCancelledEvent POJO"
    status: pending
    dependencies:
      - phase2b-rabbitmq-infra
      - phase2c-event-framework
  - id: phase3b-order-event-consumer
    content: "Phase 3b: 订单延迟队列消费——创建 OrderEventConsumer 监听延迟队列，替代 OrderAutoCancelTask 的超时取消逻辑；ProductEventConsumer 监听订单事件处理库存/销量"
    status: pending
    dependencies:
      - phase3a-domain-events
  - id: phase3c-decouple-order
    content: "Phase 3c: OrderServiceImpl 事件解耦——下单改为发OrderCreatedEvent（异步扣库存），支付发OrderPaidEvent（异步更新销量），取消发OrderCancelledEvent（异步恢复库存），保留 OrderAutoCancelTask 作为兜底降级"
    status: pending
    dependencies:
      - phase3b-order-event-consumer
  - id: phase3d-test-verify
    content: "Phase 3d: 全功能回归验证——启动应用 + Nacos + RabbitMQ + Redis + MySQL，测试完整下单/支付/取消/超时取消/评价链路"
    status: pending
    dependencies:
      - phase3c-decouple-order
---

## 需求概述

将悦选商城单体项目改造为 "模块化单体 + 事件驱动" 架构，采用分支并行策略：

**第一阶段（立即执行，不阻塞）**：包结构重构——将平铺的 controller/service/entity/mapper 按 7 个业务域（user/product/order/cart/review/sms/admin）分散到领域包中，只搬文件不改逻辑，当天完成。

**第二阶段（并行推进）**：

- 基建线：添加 Maven 依赖、引入 Nacos Config 集中管理配置、引入 RabbitMQ 搭建延迟队列和事件交换机
- 功能线：新功能按领域包开发，跨域暂时保留直接 Service 调用

**第三阶段（统一解耦）**：用领域事件替换所有跨域直接调用（下单扣库存、支付更新销量、取消恢复库存、超时取消等）

## 技术方案

### 核心思路：分支并行策略

```
时间轴 ──────────────────────────────────────────────────>
        │                    │                          │
    包结构重构             基建/功能并行              事件解耦
    (纯搬文件)            (不互相阻塞)              (一次性切)
        │                    │                          │
        ▼                    ▼                          ▼
    ┌────────┐      ┌──────────────┐           ┌──────────────┐
    │user    │      │ 基建线:       │           │下单→异步扣库存│
    │product │      │ Nacos Config  │           │支付→更新销量  │
    │order   │      │ RabbitMQ      │           │取消→恢复库存  │
    │cart    │      │ 事件框架      │           │超时→延迟队列  │
    │review  │      └──────────────┘           └──────────────┘
    │sms     │      ┌──────────────┐
    │admin   │      │ 功能线:       │
    └────────┘      │ 新功能开发    │
                    │ (直接调旧代码) │
                    └──────────────┘
```

### 技术栈选择

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 3.2.5 (保留) | 主框架 |
| MyBatis Plus | 3.5.5 (保留) | ORM |
| Spring Cloud Alibaba | 2023.0.1.0 (新增) | 微服务基础设施 |
| Nacos Config | (新增) | 配置中心 |
| RabbitMQ + Spring AMQP | (新增) | 消息队列、事件驱动 |
| Redis | (保留) | 缓存 |
| JWT | (保留) | 鉴权 |
| WebSocket | (保留) | 实时推送 |


### 模块化包结构设计

从平铺结构改造为领域包结构，每个领域自治：

```
com.char1234/
├── ServerApplication.java                     # [保留] 启动类
├── common/                                    # [保留] 公共层
│   └── Result.java
├── config/                                    # [保留] 全局配置
│   ├── JwtAuthInterceptor.java
│   ├── WebMvcConfig.java
│   ├── MyBatisPlusConfig.java
│   ├── WebSocketConfig.java
│   ├── Knife4jConfig.java
│   └── RabbitMQConfig.java                    # [NEW] MQ交换机/队列
├── context/                                   # [保留] JWT上下文
│   └── JwtContextHolder.java
├── util/                                      # [保留] 工具类
│   ├── JwtUtil.java
│   └── JwtPrincipalType.java
├── event/                                     # [NEW] 事件基础设施
│   ├── DomainEvent.java                       # 领域事件接口
│   ├── DomainEventPublisher.java              # 事件发布器(事务同步)
│   └── EventConsumerConfig.java               # 消息监听容器配置
├── user/                                      # [NEW] 用户域
│   ├── controller/ (UserController, UserAddressMpController, ProductFavoriteMpController)
│   ├── service/ (UserService, UserServiceImpl, UserAddressService, UserAddressServiceImpl, ProductFavoriteService, ProductFavoriteServiceImpl)
│   ├── entity/ (User, UserAddress, ProductFavorite)
│   └── mapper/ (UserMapper, UserAddressMapper, ProductFavoriteMapper)
├── product/                                   # [NEW] 商品域
│   ├── controller/ (ProductController, CategoryController)
│   ├── service/ (ProductService, ProductServiceImpl, CategoryService, CategoryServiceImpl)
│   ├── entity/ (Product, Category)
│   ├── mapper/ (ProductMapper, CategoryMapper)
│   └── event/                                 # [NEW] 监听订单事件
│       └── ProductEventConsumer.java          # 扣库存/恢复库存/更新销量
├── order/                                     # [NEW] 订单域
│   ├── controller/ (OrderController)
│   ├── service/ (OrderService, OrderServiceImpl)
│   ├── entity/ (Order, OrderItem, CartItem, CartItemEntity)
│   ├── mapper/ (OrderMapper, OrderItemMapper, CartItemMapper)
│   ├── event/                                 # [NEW] 订单领域事件
│   │   ├── OrderCreatedEvent.java
│   │   ├── OrderPaidEvent.java
│   │   ├── OrderCancelledEvent.java
│   │   └── OrderEventConsumer.java            # 延迟队列消费者
│   └── task/
│       └── OrderAutoCancelTask.java           # [MODIFY] 保留兜底
├── review/                                    # [NEW] 评价域
│   ├── controller/ (ProductReviewController)
│   ├── service/ (ProductReviewService, ProductReviewServiceImpl)
│   ├── entity/ (ProductReview)
│   └── mapper/ (ProductReviewMapper)
├── sms/                                       # [NEW] 短信域
│   ├── controller/ (SmsController)
│   └── service/ (WechatMiniappCodeService)
└── admin/                                     # [NEW] 管理后台域
    ├── controller/ (AdminController, DashboardController)
    ├── service/ (AdminService, AdminServiceImpl)
    └── entity/ (Admin)
    └── mapper/ (AdminMapper)
```

### 领域归属映射

| 领域 | Controller | Entity | Mapper | Service | 数据库表 |
| --- | --- | --- | --- | --- | --- |
| user | UserController, UserAddressMpController, ProductFavoriteMpController | User, UserAddress, ProductFavorite | UserMapper, UserAddressMapper, ProductFavoriteMapper | UserService, UserAddressService, ProductFavoriteService | t_user, t_user_address, t_product_favorite |
| product | ProductController, CategoryController | Product, Category | ProductMapper, CategoryMapper | ProductService, CategoryService | t_product, t_category |
| order | OrderController | Order, OrderItem, CartItem, CartItemEntity | OrderMapper, OrderItemMapper, CartItemMapper | OrderService, CartService | t_order, t_order_item, t_cart |
| review | ProductReviewController | ProductReview | ProductReviewMapper | ProductReviewService | t_product_review |
| sms | SmsController | - (无实体) | - | WechatMiniappCodeService | (无表) |
| admin | AdminController, DashboardController | Admin | AdminMapper | AdminService | t_admin |


### Nacos Config 配置设计

**本地 application.yml** 精简为仅保留本地兜底 + 活跃配置标识：

```
spring:
  application:
    name: mall-server
  cloud:
    nacos:
      config:
        enabled: true
        server-addr: 127.0.0.1:8848
        file-extension: yaml
  # 保留本地兜底（Nacos不可用时使用）
  datasource:
    url: jdbc:mysql://localhost:3306/db_mall?...
```

**Nacos 配置 (Data ID: mall-server.yaml)** 包含完整配置：

- server.port
- spring.datasource (url/username/password)
- spring.redis (host/port/database)
- spring.rabbitmq (host/port/virtual-host/username/password)
- mybatis-plus (configuration/mapper-locations/type-aliases-package)
- wechat.miniapp (appid/secret)
- logging.level

### RabbitMQ 事件拓扑设计

```
═══════════════════════════════════════════════════════════════
  交换机1: order.delayed.exchange (x-delayed-message 类型)
═══════════════════════════════════════════════════════════════
  路由键: order.created.delay
    └── 队列: order.cancel.delay.queue
        └── 消费者: OrderEventConsumer
            └── 功能: 15分钟延迟后检查订单状态，未支付则取消

═══════════════════════════════════════════════════════════════
  交换机2: order.domain.exchange (topic 类型)
═══════════════════════════════════════════════════════════════
  路由键: order.created     → 队列: product.stock.deduction.queue
  └── 消费者: ProductEventConsumer.handleOrderCreated()
      └── 功能: 扣减商品库存

  路由键: order.paid        → 队列: product.sales.update.queue
  └── 消费者: ProductEventConsumer.handleOrderPaid()
      └── 功能: 更新商品销量

  路由键: order.cancelled   → 队列: product.stock.restore.queue
  └── 消费者: ProductEventConsumer.handleOrderCancelled()
      └── 功能: 恢复商品库存
```

### 事务一致性方案

使用 Spring Transaction Synchronization 确保事务提交后才发送消息：

```java
// OrderServiceImpl.createOrder 中改造
@Transactional
public Order createOrder(...) {
    // 1. 本地事务：保存订单 + 扣库存(改为本地调用)
    // 2. 注册事务同步
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(new OrderCreatedEvent(order.getId(), items));
            }
        }
    );
    return order;
}
```

### 关键实施难点

1. **消息幂等性**：消费者用 orderId + eventType 在 Redis 做去重标记（SETNX），防止重复扣库存
2. **跨域查询保留**：评价列表查用户昵称、购物车查商品信息等纯读场景，保留直接 Mapper 查询（同 JVM 内不需要强行解耦）
3. **逐步迁移**：第一阶段包重构后业务完全不变，新功能按领域包开发，老功能保持跨域直接调用；第三阶段一次性切到事件驱动
4. **RabbitMQ 延迟队列**：需要安装 rabbitmq_delayed_message_exchange 插件，或使用死信队列方案（无需插件）
5. **Nacos 安装**：本地单机模式部署 Nacos Server，配置写入后应用从 Nacos 拉取

### 目录结构变更清单

```
server/
├── pom.xml                                              # [MODIFY] 新增SCA+Nacos+RabbitMQ依赖
├── src/main/resources/
│   ├── application.yml                                  # [MODIFY] 精简为Nacos连通配置+本地兜底
│   └── mapper/
│       └── OrderMapper.xml                              # [KEEP] 保持不变
├── src/main/java/com/char1234/
│   ├── ServerApplication.java                           # [KEEP] 启动类
│   ├── common/Result.java                               # [KEEP]
│   ├── config/
│   │   ├── JwtAuthInterceptor.java                      # [KEEP]
│   │   ├── Knife4jConfig.java                           # [KEEP]
│   │   ├── MyBatisPlusConfig.java                       # [KEEP]
│   │   ├── WebMvcConfig.java                            # [KEEP]
│   │   ├── WebSocketConfig.java                         # [KEEP]
│   │   ├── WechatMiniappProperties.java                 # [KEEP]
│   │   └── RabbitMQConfig.java                          # [NEW] 交换机/队列/绑定
│   ├── context/JwtContextHolder.java                    # [KEEP]
│   ├── util/
│   │   ├── JwtUtil.java                                 # [KEEP]
│   │   └── JwtPrincipalType.java                        # [KEEP]
│   ├── event/                                           # [NEW] 事件基础设施
│   │   ├── DomainEvent.java                             # [NEW] 事件接口
│   │   ├── DomainEventPublisher.java                    # [NEW] 发布器
│   │   └── EventConsumerConfig.java                     # [NEW] 消费者配置
│   ├── user/                                            # [NEW] 用户域
│   │   ├── controller/UserController.java               # ← 从controller/移入
│   │   ├── controller/UserAddressMpController.java      # ← 从controller/移入
│   │   ├── controller/ProductFavoriteMpController.java  # ← 从controller/移入
│   │   ├── service/UserService.java                     # ← 从service/移入
│   │   ├── service/UserServiceImpl.java                 # ← 从service/impl/移入
│   │   ├── service/UserAddressService.java              # ← 从service/移入
│   │   ├── service/UserAddressServiceImpl.java          # ← 从service/impl/移入
│   │   ├── service/ProductFavoriteService.java          # ← 从service/移入
│   │   ├── service/ProductFavoriteServiceImpl.java      # ← 从service/impl/移入
│   │   ├── entity/User.java                             # ← 从entity/移入
│   │   ├── entity/UserAddress.java                      # ← 从entity/移入
│   │   ├── entity/ProductFavorite.java                  # ← 从entity/移入
│   │   └── mapper/UserMapper.java                       # ← 从mapper/移入
│   │   └── mapper/UserAddressMapper.java                # ← 从mapper/移入
│   │   └── mapper/ProductFavoriteMapper.java            # ← 从mapper/移入
│   ├── product/                                         # [NEW] 商品域
│   │   ├── controller/ProductController.java            # ← 从controller/移入
│   │   ├── controller/CategoryController.java           # ← 从controller/移入
│   │   ├── service/ProductService.java                  # ← 从service/移入
│   │   ├── service/ProductServiceImpl.java              # ← 从service/impl/移入
│   │   ├── service/CategoryService.java                 # ← 从service/移入
│   │   ├── service/CategoryServiceImpl.java             # ← 从service/impl/移入
│   │   ├── entity/Product.java                          # ← 从entity/移入
│   │   ├── entity/Category.java                         # ← 从entity/移入
│   │   ├── mapper/ProductMapper.java                    # ← 从mapper/移入
│   │   ├── mapper/CategoryMapper.java                   # ← 从mapper/移入
│   │   └── event/ProductEventConsumer.java              # [NEW] 订单事件监听→扣库存/恢复/更新销量
│   ├── order/                                           # [NEW] 订单域
│   │   ├── controller/OrderController.java              # ← 从controller/移入
│   │   ├── service/OrderService.java                    # ← 从service/移入
│   │   ├── service/OrderServiceImpl.java                # ← 从service/impl/移入 (MODIFY:事件解耦)
│   │   ├── service/CartService.java                     # ← 从service/移入
│   │   ├── service/CartServiceImpl.java                 # ← 从service/impl/移入
│   │   ├── entity/Order.java                            # ← 从entity/移入
│   │   ├── entity/OrderItem.java                        # ← 从entity/移入
│   │   ├── entity/CartItem.java                         # ← 从entity/移入
│   │   ├── entity/CartItemEntity.java                   # ← 从entity/移入
│   │   ├── mapper/OrderMapper.java                      # ← 从mapper/移入
│   │   ├── mapper/OrderItemMapper.java                  # ← 从mapper/移入
│   │   ├── mapper/CartItemMapper.java                   # ← 从mapper/移入
│   │   └── event/
│   │       ├── OrderCreatedEvent.java                   # [NEW] 订单创建事件
│   │       ├── OrderPaidEvent.java                      # [NEW] 订单支付事件
│   │       ├── OrderCancelledEvent.java                 # [NEW] 订单取消事件
│   │       └── OrderEventConsumer.java                  # [NEW] 延迟队列:超时取消
│   │   └── task/
│   │       └── OrderAutoCancelTask.java                 # ← 从service/task/移入
│   ├── review/                                          # [NEW] 评价域
│   │   ├── controller/ProductReviewController.java      # ← 从controller/移入
│   │   ├── service/ProductReviewService.java            # ← 从service/移入
│   │   ├── service/ProductReviewServiceImpl.java        # ← 从service/impl/移入
│   │   ├── entity/ProductReview.java                    # ← 从entity/移入
│   │   └── mapper/ProductReviewMapper.java              # ← 从mapper/移入
│   ├── sms/                                             # [NEW] 短信域
│   │   ├── controller/SmsController.java                # ← 从controller/移入
│   │   └── service/WechatMiniappCodeService.java        # ← 从service/移入
│   └── admin/                                           # [NEW] 管理后台域
│       ├── controller/AdminController.java              # ← 从controller/移入
│       ├── controller/DashboardController.java          # ← 从controller/移入
│       ├── service/AdminService.java                    # ← 从service/移入
│       ├── service/AdminServiceImpl.java                # ← 从service/impl/移入
│       ├── entity/Admin.java                            # ← 从entity/移入
│       └── mapper/AdminMapper.java                      # ← 从mapper/移入
```

### 旧文件清理清单

包重构完成后，需要删除旧的平铺包目录：

```
server/src/main/java/com/char1234/controller/  (整目录)
server/src/main/java/com/char1234/entity/      (整目录)
server/src/main/java/com/char1234/mapper/      (整目录)
server/src/main/java/com/char1234/service/     (整目录，保留空壳?)
```

## Agent Extensions 使用说明

本计划需要使用以下已提供的扩展能力：

### SubAgent

- **code-explorer**: 用于第一阶段包结构重构时的精确文件扫描，定位所有 Java 文件中的 import 路径和引用关系，确保搬移后 package 声明正确

### Skill

- **improve-codebase-architecture**: 用于第三阶段事件驱动解耦时，分析当前跨域耦合的完整链路，生成精确的拆分解耦方案