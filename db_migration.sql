-- ===============================
-- 数据库迁移文件
-- 每次修改数据库结构时，在此追加新的 ALTER / UPDATE 语句
-- 复制到 Navicat 中执行即可
-- ===============================

-- ===============================
-- M001: 用户表增加 type/phone/email/password 字段 + openid 可空
-- ===============================
ALTER TABLE t_user
    ADD COLUMN `phone`    VARCHAR(20)  DEFAULT NULL COMMENT '手机号' AFTER `avatar_url`,
    ADD COLUMN `email`    VARCHAR(100) DEFAULT NULL COMMENT '邮箱'    AFTER `phone`,
    ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(MD5)' AFTER `email`,
    ADD COLUMN `type`     TINYINT      NOT NULL DEFAULT 2 COMMENT '1-管理员 2-普通用户' AFTER `password`,
    MODIFY COLUMN `openid` varchar(100) DEFAULT NULL COMMENT '微信openid（注册用户为NULL）';

-- 如果提示 "Duplicate key name" 可忽略，表示唯一键已存在
ALTER TABLE t_user ADD UNIQUE KEY `uk_phone` (`phone`);
ALTER TABLE t_user ADD UNIQUE KEY `uk_email` (`email`);

-- 现有微信用户默认 type=2
UPDATE t_user SET type = 2 WHERE type IS NULL OR type = 0;

-- 添加测试注册账号（密码 123456）
INSERT IGNORE INTO t_user (openid, nickname, avatar_url, phone, email, password, type)
VALUES (NULL, '测试用户', '', '13800000100', '', 'e10adc3949ba59abbe56e057f20f883e', 2);

-- ===============================
-- M002: 创建商品评价表 + 商品表增加评分字段
-- ===============================
CREATE TABLE IF NOT EXISTS `t_product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_item_id` bigint NOT NULL COMMENT '订单项ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` text COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_item_id` (`order_item_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_order_item` (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价';

ALTER TABLE `t_product`
    ADD COLUMN `avg_rating` decimal(2,1) DEFAULT 0.0 COMMENT '平均评分' AFTER `description`,
    ADD COLUMN `review_count` int DEFAULT 0 COMMENT '评价数' AFTER `avg_rating`;

-- ===============================
-- 后续迁移在此追加（M003、M004...）
-- 格式：
-- -- M003: 说明
-- ALTER TABLE ...
