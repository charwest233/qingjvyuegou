---
name: modular-monolith-refactor
overview: 将单体 Spring Boot 电商项目改造为「模块化单体 + 事件驱动」架构：保持单 JAR 部署，但按微服务思路拆分域包、用 Nacos Config 管理配置、用 RabbitMQ 延迟队列替代轮询定时任务、用领域事件解耦跨域调用，降低工作量的同时保留未来独立部署任意模块的能力。
todos:
  - id: add-dependencies
    content: "Step 1: pom.xml 新增 Spring Cloud Alibaba + Nacos Config + RabbitMQ 依赖"
    status: pending
  - id: nacos-config
    content: "Step 2: 引入 Nacos Config：创建 bootstrap.yml，迁移配置到 Nacos，本地保留 fallback"
    status: pending
    dependencies:
      - add-dependencies
  - id: rabbitmq-infra
    content: "Step 3: 搭建 RabbitMQ 基础设施：配置类 + 延迟队列 + 交换机/队列定义"
    status: pending
    dependencies:
      - add-dependencies
  - id: domain-event-framework
    content: "Step 4: 创建事件基础设施：DomainEvent 接口 + DomainEventPublisher（事务同步安全发送）+ EventConsumerConfig"
    status: pending
    dependencies:
      - add-dependencies
  - id: restructure-packages
    content: "Step 5: 包结构重构为领域包（平铺 controller/service/entity/mapper 按域分散到 user/product/order/cart/review/sms/admin 7个域包，不修改业务逻辑）"
    status: pending
    dependencies:
      - add-dependencies
  - id: event-driven-decoupling
    content: "Step 6: 域事件解耦核心流程：下单事件→异步扣库存、支付事件→更新销量、取消事件→恢复库存（替换 OrderServiceImpl 和 OrderAutoCancelTask 中的直接跨域调用）"
    status: pending
    dependencies:
      - domain-event-framework
      - rabbitmq-infra
      - restructure-packages
---

## 需求概述

用户希望将当前 Spring Boot 单体电商系统改造成 **"模块化单体 + 事件驱动"** 架构，目标是：

1. **保持单 JAR 部署**（不是拆成 6 个独立服务），减少配置和运维工作量
2. **内部按微服务风格重构**：包结构领域化 + 域事件解耦直接调用 + 基础设施现代化
3. **引入 Nacos Config** 统一管理配置（数据库、Redis、微信等）
4. **引入 RabbitMQ** 替代定时轮询，用于订单超时取消、异步库存恢复、异步评分更新等
5. **未来能方便补充新功能**，且需要独立部署时可低成本拆分单个模块

## 核心思路

- **架构模式**：模块化单体（Modular Monolith）+ 事件驱动（Event-Driven）
- **域事件解耦**：将跨域的直接 Service/Mapper 调用替换为事件发布/订阅
- **基础设施升级**：引入 Nacos（配置中心）和 RabbitMQ（消息队列），不改服务数
- **数据库**：仍为单库，但按表归属划分"逻辑数据库域"

## 技术栈选择

### 当前技术栈（保留）

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 3.2.5 | 主框架 |
| MyBatis Plus | 3.5.5 | ORM |
| MySQL | 8.0.33 | 数据库 |
| Redis | (Spring Data) | 缓存 |
| JWT (jjwt) | 0.9.1 | 鉴权 |
| WebSocket | (Starter) | 实时推送 |


### 新增技术栈

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| **Spring Cloud Alibaba** | 2023.0.x (适配 Spring Boot 3.2) | 微服务基础设施 |
| **Nacos Client** | nacos-config + nacos-discovery | 配置中心（配置集中管理） |
| **RabbitMQ (Spring AMQP)** | (Spring Boot Starter) | 消息队列、事件驱动 |


### 不引入

- **Spring Cloud Gateway**：单服务部署不需要网关
- **OpenFeign**：同 JVM 内用域事件 + 事件总线，无需 RPC
- **Nacos Discovery**：单服务无需注册发现，但可以引入作为未来预留

## 实施策略

### 原则

1. **一次只改一个域**，每个步骤保证可部署可回退
2. **先加基础设施（Nacos + RabbitMQ）**，再加域事件解耦
3. **不重写业务逻辑**，只重构调用方式
4. **Nacos Config 渐进式迁移**：先保留 application.yml 本地配置兜底，Nacos 覆盖

## 架构设计

### 模块化包结构

改造核心：将当前平铺的 `controller/`、`service/`、`entity/`、`mapper/` 改为按**业务域**分组。

```
com.char1234/
├── common/                    # [保留] 公共层
│   ├── Result.java
│   ├── GlobalExceptionHandler.java
│   └── ...
├── user/                      # [新建] 用户域
│   ├── UserController.java    # ← 从 controller/ 移入
│   ├── UserAddressController.java
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   ├── UserAddressService.java
│   ├── UserAddressServiceImpl.java
│   ├── entity/ (User.java, UserAddress.java)
│   └── mapper/ (UserMapper.java, UserAddressMapper.java)
├── product/                   # [新建] 商品域
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── ProductService.java
│   ├── ProductServiceImpl.java
│   ├── entity/ (Product.java, Category.java)
│   └── mapper/ (ProductMapper.java, CategoryMapper.java)
├── cart/                      # [新建] 购物车域
│   ├── CartController.java
│   ├── CartService.java
│   ├── CartServiceImpl.java
│   ├── entity/ (CartItemEntity.java)
│   └── mapper/ (CartItemMapper.java)
├── order/                     # [新建] 订单域
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderServiceImpl.java
│   ├── entity/ (Order.java, OrderItem.java)
│   ├── mapper/ (OrderMapper.java, OrderItemMapper.java)
│   └── event/                 # [新建] 订单领域事件
│       ├── OrderCreatedEvent.java
│       ├── OrderPaidEvent.java
│       ├── OrderCancelledEvent.java
│       └── OrderDomainEventPublisher.java
├── review/                    # [新建] 评价域
│   ├── ReviewController.java
│   ├── ReviewService.java
│   ├── ReviewServiceImpl.java
│   ├── entity/ (ProductReview.java)
│   └── mapper/ (ProductReviewMapper.java)
├── sms/                       # [新建] 短信域
│   ├── SmsController.java
│   └── SmsService.java
├── admin/                     # [新建] 管理后台域
│   ├── AdminController.java
│   ├── DashboardController.java
│   ├── AdminService.java
│   └── AdminServiceImpl.java
├── config/                    # [保留] 配置类
│   ├── JwtAuthInterceptor.java
│   ├── WebMvcConfig.java
│   ├── MyBatisPlusConfig.java
│   ├── WebSocketConfig.java
│   ├── Knife4jConfig.java
│   └── RabbitMQConfig.java    # [新建] RabbitMQ 交换机/队列/绑定
├── context/                   # [保留] JWT 上下文
│   └── JwtContextHolder.java
├── util/                      # [保留] 工具类
│   └── JwtUtil.java
├── task/                      # [保留] 定时任务（逐步废弃）
│   └── OrderAutoCancelTask.java
└── event/                     # [新建] 全局事件基础设施
    ├── DomainEvent.java              # 领域事件接口
    ├── DomainEventPublisher.java      # 事件发布器（封装 RabbitTemplate）
    └── EventConsumerConfig.java       # 事件消费者配置
```

### 域事件解耦设计

当前跨域直接调用链路（需要解耦的）：

| 场景 | 当前做法 | 改造后 |
| --- | --- | --- |
| 下单扣库存 (Order → Product) | 直接调 `productService.updateById()` | 订单服务发 `OrderCreatedEvent` → 商品服务监听，扣库存 |
| 支付更新销量 (Order → Product) | 直接调 `productService.getById()` + `updateById()` | 订单服务发 `OrderPaidEvent` → 商品服务监听，更新销量 |
| 取消恢复库存 (Order → Product) | 直接调 `productService.getById()` + `productMapper.updateById()` | 订单服务发 `OrderCancelledEvent` → 商品服务监听，恢复库存 |
| 评价校验订单 (Review → Order) | 直接调 `orderMapper.selectById()` | 评价服务发 `ReviewValidateEvent` 或通过订单服务 API |
| 评价显示用户信息 (Review → User) | 直接调 `userMapper.selectBatchIds()` | 保持直接查（纯查询场景可保留） |
| 购物车显示商品 (Cart → Product) | 直接调 `productMapper.selectById()` | 保持直接查（纯查询场景可保留） |


### 数据流图

```
┌─────────────────────────────────────────────────────┐
│                 单 JAR 部署单元                        │
│                                                       │
│  User API ──→ user 域 ──┐                             │
│                          │                            │
│  Product API ─→ product 域                             │
│                          │                            │
│  Cart API ───→ cart 域 ──┤                            │
│                          │   事件发布                   │
│  Order API ───→ order 域 ──→── RabbitMQ ──→ product 域│
│                          │   (延迟队列)     → user 域  │
│  Review API ──→ review 域─┘                            │
│                                                       │
│  ┌──────────────────────────────────────┐              │
│  │  Nacos Config（配置中心）              │              │
│  │  DB / Redis / MQ / JWT / 微信         │              │
│  └──────────────────────────────────────┘              │
└─────────────────────────────────────────────────────┘
```

### RabbitMQ 详细设计

```
订单超时取消（延迟队列）：
  order.created.exchange (topic)
  └── routing: order.created.delay (15min)
      └── order.cancel.delay.queue
          └── 消费者：检查订单状态 → 若未支付则取消 + 发 OrderCancelledEvent

事件广播：
  order.domain.event.exchange (topic)
  ├── order.created  → product.stock.deduction.queue   → 扣库存
  ├── order.paid     → product.sales.update.queue      → 更新销量
  ├── order.cancelled → product.stock.restore.queue    → 恢复库存
  └── order.cancelled → review.cleanup.queue           → 删除未评价（可选）
```

### Nacos Config 设计

```
# nacos 配置 data-id: mall-server.yaml, group: DEFAULT_GROUP
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db_mall?...
    username: root
    password: xxx
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    virtual-host: /
    username: guest
    password: guest
wechat:
  miniapp:
    appid: ''
    secret: ''
```

本地 `bootstrap.yml` 只保留从 Nacos 拉取配置的最小配置：

```
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
```

## 实施难点与注意事项

### 难点1：域事件的事务一致性

- **问题**：下单是 `@Transactional`，如果发消息在事务内，消息发出后事务回滚会导致数据不一致
- **方案**：使用 **Transaction Synchronization** + **发件箱模式（Outbox）** 的简化版——在事务提交后再发消息

```java
@Transactional
public Order createOrder(...) {
    // 1. 保存订单
    // 2. 扣库存（改为本地操作）
    // 3. 注册事务同步回调 → 事务提交后发事件
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(new OrderCreatedEvent(orderId, ...));
            }
        }
    );
}
```

### 难点2：纯查询场景

- **问题**：评价列表需要显示用户昵称和商品名，纯粹是"读"查询
- **方案**：评价表冗余存储 `nickname`、`product_name` 字段（读时填充），或者**保留直接 Mapper 查询**（同 JVM 内直接查不是架构问题，不需要为了"微服务"而强行解耦）

### 难点3：改造过程中的回退

- **每次只改一个域**，改完测试全部功能，确保可部署
- RabbitMQ 引入后，消息发送失败要有降级：降级为同步调用（保持功能可用）
- Nacos 配置读取失败时，fallback 到本地 application.yml

## 性能考虑

- **RabbitMQ 延迟队列**替代每分钟轮询：从 O(每次全表扫描) 降到 O(只消费到期消息)，数据库压力大幅降低
- **事件异步化**：下单流程中原本同步扣库存 + 验地址 + 生成订单在一个事务内，拆分为：下单（事务小）→ 异步扣库存，主流程更快
- **消息幂等性**：消费者用 orderId + eventType 做去重（Redis 或数据库唯一索引），防止重复扣库存

## 目录结构

```
server/
├── pom.xml                                      # [MODIFY] 新增 Spring Cloud Alibaba + Nacos + RabbitMQ 依赖
├── src/main/resources/
│   ├── application.yml                          # [MODIFY] 精简，只保留本地兜底配置
│   ├── bootstrap.yml                            # [NEW] Nacos Config 连接配置
│   └── mapper/                                  # [保留] Mapper XML
├── src/main/java/com/char1234/
│   ├── event/                                   # [NEW] 事件基础设施
│   │   ├── DomainEvent.java                     # 领域事件接口
│   │   ├── DomainEventPublisher.java            # 事件发布器（封装 RabbitTemplate）
│   │   └── EventConsumerConfig.java             # 消费者配置
│   ├── order/event/                             # [NEW] 订单域事件
│   │   ├── OrderCreatedEvent.java               # 订单创建事件
│   │   ├── OrderPaidEvent.java                  # 订单支付事件
│   │   ├── OrderCancelledEvent.java             # 订单取消事件
│   │   └── OrderEventConsumer.java              # 订单事件消费者
│   ├── product/event/                           # [NEW] 商品域事件监听
│   │   └── ProductEventConsumer.java            # 监听订单事件，处理库存/销量
│   ├── config/
│   │   └── RabbitMQConfig.java                  # [NEW] 交换机/队列/绑定定义
│   ├── task/
│   │   └── OrderAutoCancelTask.java             # [MODIFY] 保留作为兜底，后续可移除
│   ├── common/                                  # [保留]
│   ├── config/                                  # [保留]
│   ├── context/                                 # [保留]
│   ├── util/                                    # [保留]
│   │   ├── user/                               # [NEW] 用户域（从平铺包转移）
│   │   ├── product/                            # [NEW] 商品域
│   │   ├── order/                              # [NEW] 订单域
│   │   ├── cart/                               # [NEW] 购物车域
│   │   ├── review/                             # [NEW] 评价域
│   │   ├── sms/                                # [NEW] 短信域
│   │   └── admin/                              # [NEW] 管理后台域
│   └── ServerApplication.java                  # [MODIFY] 添加 @EnableConfigurationProperties
```

## 新增依赖

```xml
<!-- pom.xml 新增 -->
<!-- Spring Cloud Alibaba 2023.x（适配 Spring Boot 3.2.x） -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2023.0.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Nacos Config -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

## 注意事项

1. **不要一次性全部重构**：按 6 个步骤渐进实施，每个步骤都可逆
2. **冗余字段策略**：评价表的 `nickname`、`product_name` 等冗余字段在写入时填充，读取时无需跨域查询
3. **消息幂等设计**：用订单ID+事件类型的联合唯一键做去重
4. **RabbitMQ 延迟队列插件**：需要安装 `rabbitmq_delayed_message_exchange` 插件（社区版 RabbitMQ 支持）
5. **Nacos 安装**：需要本地部署 Nacos Server（单机模式即可）