# 悦选商城 - Spring Cloud 微服务迁移指南

## 概述

本文档指导如何将现有 Spring Boot 单体应用迁移到 Spring Cloud 微服务架构。

## 一、迁移前准备

### 环境要求
- JDK 17+
- Maven 3.8+
- Docker（用于部署 Nacos、Redis、MySQL）
- Git

### 技术版本
| 组件 | 版本 |
|------|------|
| Spring Cloud | 2023.0.x (对应 Boot 3.2.x) |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos | 2.3.x |
| Sentinel | 1.8.7 |
| Seata | 1.8.0 |
| Zipkin | 3.4.x |

---

## 二、服务拆分方案

将现有单体服务拆分为 **6个微服务**：

```
mall-cloud/
├── pom.xml                  # 父 POM（版本管理）
├── mall-common/             # 公共模块（Result, JwtUtil, 异常处理）
├── mall-gateway/            # API 网关（Spring Cloud Gateway）
├── mall-auth/               # 认证中心（登录/注册/Token颁发）
├── mall-user/               # 用户服务（用户CRUD/地址/收藏）
├── mall-product/            # 商品服务（商品/分类/搜索）
├── mall-order/              # 订单服务（订单/购物车）
└── mall-payment/            # 支付服务（支付对接/回调）
```

### 拆分原则

1. **先读后写**：先拆分只读服务（商品/分类），后拆分写入服务（订单/支付）
2. **垂直拆分**：按业务领域拆分为独立数据库
3. **最终一致性**：跨服务事务使用 Seata 或消息队列

### 数据库拆分

| 原表 | 目标库 | 所属服务 |
|------|--------|----------|
| t_admin | mall_auth | mall-auth |
| t_user, t_user_address, t_product_favorite | mall_user | mall-user |
| t_category, t_product | mall_product | mall-product |
| t_order, t_order_item | mall_order | mall-order |
| 新增：payment_log | mall_payment | mall-payment |

---

## 三、Nacos 部署与配置

### Docker 部署 Nacos

```bash
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  -e MYSQL_SERVICE_HOST=localhost \
  -e MYSQL_SERVICE_PORT=3306 \
  -e MYSQL_SERVICE_DB_NAME=nacos_config \
  -e MYSQL_SERVICE_USER=root \
  -e MYSQL_SERVICE_PASSWORD=990427Zyh \
  nacos/nacos-server:v2.3.0
```

### 注册中心配置

每个微服务需要：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
```

---

## 四、API 网关配置

### mall-gateway 核心配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: mall-auth
          uri: lb://mall-auth
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1

        - id: mall-user
          uri: lb://mall-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1

        - id: mall-product
          uri: lb://mall-product
          predicates:
            - Path=/api/product/**,/api/category/**
          filters:
            - StripPrefix=1

        - id: mall-order
          uri: lb://mall-order
          predicates:
            - Path=/api/order/**,/api/cart/**
          filters:
            - StripPrefix=1

        - id: mall-payment
          uri: lb://mall-payment
          predicates:
            - Path=/api/payment/**
          filters:
            - StripPrefix=1
```

### 网关鉴权（AuthGlobalFilter）

在网关层统一验证 JWT Token，将解析后的用户信息通过 Header 透传到下游服务：

```java
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取 Authorization Header
        // 2. 验证 JWT Token
        // 3. 将 userId, principalType 写入 Header
        // 4. 放行或返回 401
    }
}
```

---

## 五、服务间调用（Feign）

### 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 示例：订单服务调用商品服务

```java
@FeignClient(name = "mall-product", path = "/api/product")
public interface ProductFeignClient {
    @GetMapping("/{id}")
    Result<Product> getProduct(@PathVariable Long id);

    @PutMapping("/{id}/stock")
    Result<Boolean> deductStock(@PathVariable Long id, @RequestParam Integer quantity);
}
```

---

## 六、分布式事务（Seata）

### 部署 Seata Server

```bash
docker run -d \
  --name seata-server \
  -p 8091:8091 \
  -p 7091:7091 \
  -e SEATA_IP=localhost \
  seataio/seata-server:1.8.0
```

### AT 模式配置

在需要分布式事务的方法上添加 `@GlobalTransactional`：

```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
public Order createOrder(Long userId, List<CartItem> items, Long addressId) {
    // 1. 扣减商品库存（调用 mall-product 服务）
    // 2. 创建订单（写入 mall-order 数据库）
    // 3. 创建支付记录（调用 mall-payment 服务）
}
```

### AT 模式适用场景
- 下单扣库存（最核心事务）
- 支付回调更新订单状态

---

## 七、服务容错（Sentinel）

### Dashboard 部署

```bash
docker run -d --name sentinel -p 8080:8080 bladex/sentinel-dashboard:1.8.7
```

### 熔断降级规则（示例）

| 资源 | 阈值 | 统计时长 | 行为 |
|------|------|----------|------|
| 商品查询接口 | 100 QPS | 1s | 直接限流 |
| 下单接口 | 50 QPS | 1s | 排队等待 |
| 库存扣减 | 60% 慢调用 | 5s | 熔断降级 |

---

## 八、全链路追踪（Zipkin）

### 部署

```bash
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin:3.4
```

### 集成

```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 生产环境建议 0.1
  zipkin:
    base-url: http://localhost:9411
```

---

## 九、迁移步骤（推荐顺序）

### 第1步：公共模块
- [x] 创建 `mall-common`，迁移 Result、JwtUtil、全局异常处理
- [ ] 将其安装为本地 Jar 供其他服务引用

### 第2步：部署 Nacos
- [ ] Docker 启动 Nacos
- [ ] 配置命名空间和配置项

### 第3步：认证中心（mall-auth）
- [ ] 拆出管理员/用户登录注册接口
- [ ] 与网关联调 Token 校验

### 第4步：商品服务（mall-product）
- [ ] 拆出商品/分类 CRUD
- [ ] 迁移商品表到 mall_product 库
- [ ] 集成 Redis 缓存

### 第5步：用户服务（mall-user）
- [ ] 拆出用户/地址/收藏接口
- [ ] 迁移用户相关表到 mall_user 库

### 第6步：订单服务（mall-order）
- [ ] 拆出订单/购物车接口
- [ ] 迁移订单表到 mall_order 库
- [ ] 集成 Seata 分布式事务

### 第7步：支付服务（mall-payment）
- [ ] 拆出支付接口
- [ ] 对接支付宝/微信支付沙箱

### 第8步：部署网关
- [ ] 启动 Gateway
- [ ] 配置路由规则
- [ ] 统一鉴权 + 限流

### 第9步：集成 Sentinel
- [ ] 配置限流规则
- [ ] 配置熔断降级规则

### 第10步：集成 Zipkin
- [ ] 添加 Sleuth + Zipkin 依赖
- [ ] 验证链路追踪

---

## 十、注意事项

1. **平滑迁移**：单体和新服务并行运行一段时间，通过网关切换流量
2. **配置集中管理**：所有配置迁移到 Nacos Config，本地仅保留基础连接信息
3. **缓存一致性**：Redis 缓存商品信息，通过 MQ 广播缓存更新
4. **日志聚合**：使用 ELK 统一收集微服务日志
5. **健康检查**：每个服务暴露 `/actuator/health` 端点
6. **API 版本管理**：在路径中添加版本号，如 `/api/v1/product/list`
