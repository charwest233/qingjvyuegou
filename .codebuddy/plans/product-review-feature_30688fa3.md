---
name: product-review-feature
overview: 为商城添加商品评价功能：用户确认收货后可对商品打分评价，商品详情页展示评价，管理后台可审核查看，订单列表新增评价入口。
todos:
  - id: db-migration
    content: 在 db_migration.sql 追加 M002 迁移：创建 t_product_review 表，t_product 增加 avg_rating/review_count 字段
    status: completed
  - id: backend-review-entity
    content: 创建 ProductReview 实体、Mapper、Service 接口和实现（参考 ProductFavorite 模式）
    status: completed
    dependencies:
      - db-migration
  - id: backend-review-api
    content: 创建 ProductReviewController（提交评价/查询商品评价/查询订单评价/管理端列表/删除），更新 JwtAuthInterceptor 鉴权规则
    status: completed
    dependencies:
      - backend-review-entity
  - id: frontend-review-api
    content: 创建前端 review.js API 层，封装所有评价接口调用
    status: completed
    dependencies:
      - backend-review-api
  - id: order-card-review
    content: 修改 OrderCard.vue（修复确认收货按钮条件 bug status=1->2），新增"去评价"按钮和 ReviewDialog 评价弹窗组件
    status: completed
    dependencies:
      - frontend-review-api
  - id: product-detail-reviews
    content: 修改商品详情页 product-detail/index.vue，在描述下方新增评价展示区（平均分+星级+评价列表）
    status: completed
    dependencies:
      - frontend-review-api
  - id: admin-reviews
    content: 新建管理评价页 admin/reviews.vue，新增路由，支持搜索和删除评价
    status: completed
    dependencies:
      - frontend-review-api
---

## 需求概述

为悦选商城添加完整的商品评价功能，覆盖从用户评价到展示的全链路。

## 核心功能

1. **数据库扩展**：新建 `t_product_review` 评价表，`t_product` 表增加平均评分和评价数字段
2. **确认收货后评价**：用户确认收货（订单状态 2->3）后，在订单卡片中显示"去评价"入口，点击弹出评价弹窗，对订单内每个商品进行 1-5 星打分和文字评价
3. **商品详情页查看评价**：在商品详情页"商品描述"卡片下方新增评价展示区，显示平均分、评价列表
4. **管理员后台查看评价**：新增管理端评价页面，支持查看所有评价、按商品搜索、删除评价
5. **扩展我的订单**：已完成订单增加评价按钮和状态

## 技术方案

### 技术栈

- **后端**：Spring Boot + MyBatis-Plus + JWT（沿用项目现有技术栈）
- **前端**：Vue 3 + Element Plus + Lucide 图标（沿用项目现有技术栈）
- **数据库**：MySQL（沿用）

### 实施架构

#### 1. 数据库设计

**新增表 `t_product_review`：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK AUTO | 主键 |
| order_id | bigint NOT NULL | 订单ID（索引） |
| order_item_id | bigint NOT NULL | 订单项ID（索引） |
| product_id | bigint NOT NULL | 商品ID（索引） |
| user_id | bigint NOT NULL | 用户ID |
| rating | tinyint NOT NULL | 评分 1-5 |
| content | text | 评价内容 |
| create_time | datetime | 创建时间 |


**`t_product` 表增加字段：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| avg_rating | decimal(2,1) DEFAULT 0.0 | 平均评分 |
| review_count | int DEFAULT 0 | 评价数 |


#### 2. 后端架构

```
ProductReview (Entity) -> ProductReviewMapper (MyBatis-Plus) -> ProductReviewService (接口+实现) -> ProductReviewController (REST API)
```

**API 设计：**

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| POST | /api/review | 提交评价（批量） | 小程序用户 |
| GET | /api/review/product/{productId} | 查询商品评价列表（分页） | 公开 |
| GET | /api/review/order/{orderId} | 查询订单评价状态 | 小程序用户 |
| GET | /api/review/list | 管理员查询所有评价 | 管理员 |
| DELETE | /api/review/{id} | 管理员删除评价 | 管理员 |


**评价提交流程（影响 `OrderServiceImpl.confirmOrder`）：**

- 确认收货时不再额外操作
- 评价提交时校验：订单存在且 status=3，订单属于当前用户，订单项属于该订单
- 提交评价后更新 `t_product` 表的 `avg_rating` 和 `review_count`

#### 3. 确认收货 BUG 修复

`OrderCard.vue` 中确认收货按钮条件当前错误写为 `Number(order.status) === 1`（已支付），应改为 `Number(order.status) === 2`（已发货）。

#### 4. 前端组件架构

```
OrderCard.vue       -- 增加"去评价"按钮 + 评价弹窗（StarRating + 文本输入 + 提交）
product-detail      -- 在描述下方增加 "商品评价" 区域（平均分 + 星级分布 + 评价列表）
admin/reviews       -- 新增管理评价页面（搜索 + 列表 + 删除）
```

**评价弹窗交互流程：**

1. 用户点击"去评价"按钮
2. 弹出半屏 Dialog，展示该订单所有商品项
3. 每个商品项显示：商品图片、名称、星级评分（点击选择 1-5 星）、评价输入框
4. 底部"提交评价"按钮，批量提交该订单所有已填写评价
5. 提交成功后关闭弹窗，按钮变为"已评价"禁用态

#### 5. 权限配置

`JwtAuthInterceptor.java` 中需要新增：

- 公开放行：`GET /api/review/product/{productId}`（任何人都可查看评价）
- 管理员权限：`GET /api/review/list`、`DELETE /api/review/{id}`
- 用户权限：`POST /api/review`、`GET /api/review/order/{orderId}`
- 管理员禁止：普通用户无法访问管理员评价接口

### 目录结构

```
server/src/main/java/com/char1234/
├── entity/
│   └── ProductReview.java              # [NEW] 评价实体
├── mapper/
│   └── ProductReviewMapper.java        # [NEW] 评价 Mapper
├── service/
│   ├── ProductReviewService.java       # [NEW] 评价 Service 接口
│   └── impl/
│       └── ProductReviewServiceImpl.java # [NEW] 评价 Service 实现
├── controller/
│   └── ProductReviewController.java    # [NEW] 评价 Controller
├── config/
│   └── JwtAuthInterceptor.java         # [MODIFY] 增加评价路由的鉴权

client-front/
├── src/
│   ├── api/
│   │   └── review.js                   # [NEW] 评价 API 封装
│   ├── components/
│   │   ├── OrderCard.vue               # [MODIFY] 增加"去评价"按钮+弹窗
│   │   └── ReviewDialog.vue            # [NEW] 评价弹窗组件
│   ├── views/
│   │   ├── product-detail/
│   │   │   └── index.vue              # [MODIFY] 增加评价展示区
│   │   └── admin/
│   │       └── reviews.vue            # [NEW] 管理员评价管理页
│   └── router/
│       └── index.js                    # [MODIFY] 增加管理评价路由

db_migration.sql                        # [MODIFY] 追加 M002: 新建评价表
```

### 实施要点

- **性能**：商品详情页评价列表使用分页查询（默认加载前 10 条），避免一次加载过多数据
- **一致性**：提交评价时使用 `@Transactional` 保证评价表插入和商品评分更新的原子性
- **可扩展性**：评价表通过 `order_item_id` 关联具体商品项，支持一个订单多项商品分别评价
- **状态追踪**：订单项级评价状态（已评/未评）通过查询 `t_product_review` 表判断，不在订单表加额外字段