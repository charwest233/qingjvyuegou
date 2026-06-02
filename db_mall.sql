/*
 Navicat Premium Data Transfer
 Source Schema: db_mall
 Date: 2026-04-29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_admin
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(MD5)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员';

INSERT INTO `t_admin` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '2026-04-27 09:49:25');

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `sort` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类';

INSERT INTO `t_category` VALUES (1, '手机数码', 1);
INSERT INTO `t_category` VALUES (2, '电脑办公', 2);
INSERT INTO `t_category` VALUES (3, '家用电器', 3);
INSERT INTO `t_category` VALUES (4, '美妆护肤', 4);
INSERT INTO `t_category` VALUES (5, '食品饮料', 5);

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(100) NOT NULL COMMENT '微信openid',
  `union_id` varchar(100) DEFAULT NULL COMMENT '微信unionid(多端统一时可填)',
  `nickname` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(100) DEFAULT NULL COMMENT '密码(MD5)',
  `type` tinyint NOT NULL DEFAULT 2 COMMENT '1-管理员 2-普通用户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表(微信+注册)';

INSERT INTO `t_user` (`id`, `openid`, `union_id`, `nickname`, `avatar_url`, `phone`, `email`, `password`, `type`, `create_time`) VALUES
(1, 'o_test_u_001', NULL, '小明', 'https://via.placeholder.com/120x120.png?text=U1', NULL, NULL, NULL, 2, '2026-04-27 10:00:00'),
(2, 'o_test_u_002', NULL, '小红', 'https://via.placeholder.com/120x120.png?text=U2', NULL, NULL, NULL, 2, '2026-04-28 12:20:00'),
(3, 'o_test_u_003', NULL, '小李', 'https://via.placeholder.com/120x120.png?text=U3', NULL, NULL, NULL, 2, '2026-04-29 09:40:00');

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int DEFAULT 0,
  `main_image` text DEFAULT NULL,
  `description` text,
  `sales_count` int DEFAULT 0 COMMENT '销量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品';

INSERT INTO `t_product` (`category_id`, `name`, `price`, `stock`, `main_image`, `description`, `sales_count`) VALUES
(1, '智能手机 Pro Max', 5999.00, 120, '', '全面屏旗舰机型', 0),
(1, '无线蓝牙耳机', 399.00, 500, '', '降噪长续航', 0),
(2, '轻薄笔记本电脑', 7299.00, 45, '', '轻薄办公性能本', 0),
(3, '空气循环扇', 299.00, 200, '', '静音三档风量', 0),
(4, '保湿洁面乳', 89.00, 800, '', '氨基酸温和洁面', 0),
(5, '混合坚果礼盒', 128.00, 350, '', '每日坚果补充能量', 0);

-- ----------------------------
-- 用户收货地址
-- ----------------------------
DROP TABLE IF EXISTS `t_user_address`;
CREATE TABLE `t_user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `detail_address` varchar(500) NOT NULL COMMENT '详细地址',
  `label` varchar(20) DEFAULT NULL COMMENT '标签如家/公司',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '1默认',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址';

-- ----------------------------
-- 商品收藏
-- ----------------------------
DROP TABLE IF EXISTS `t_product_favorite`;
CREATE TABLE `t_product_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏';

-- ----------------------------
-- Table structure for t_cart（购物车，MySQL存储）
-- ----------------------------
DROP TABLE IF EXISTS `t_cart`;
CREATE TABLE `t_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(255) NOT NULL COMMENT '商品名称',
  `product_image` varchar(512) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT 1 COMMENT '1-选中 0-未选中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `status` int DEFAULT 0 COMMENT '-1-已取消, 0-待支付, 1-已支付, 2-已发货, 3-已完成',
  `address_id` bigint DEFAULT NULL COMMENT '下单时选用的地址ID快照来源',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货电话',
  `receiver_address` varchar(500) DEFAULT NULL COMMENT '收货地址全文',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_address_id` (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单';

INSERT INTO `t_order` (`id`, `order_no`, `user_id`, `total_amount`, `status`, `address_id`, `receiver_name`, `receiver_phone`, `receiver_address`, `create_time`) VALUES
(1, 'ORD202604280001AA', 1, 6398.00, 1, NULL, '小明', '13800000001', '上海市 浦东新区 张江路 100 号', '2026-04-28 11:05:00'),
(2, 'ORD202604280002BB', 2, 299.00, 3, NULL, '小红', '13800000002', '杭州市 西湖区 文三路 8 号', '2026-04-28 15:35:00'),
(3, 'ORD202604290001CC', 3, 728.00, 2, NULL, '小李', '13800000003', '北京市 海淀区 中关村大街 66 号', '2026-04-29 09:15:00'),
(4, 'ORD202604290002DD', 1, 5999.00, 0, NULL, '小明', '13800000001', '上海市 浦东新区 张江路 100 号', '2026-04-29 13:26:00'),
(5, 'ORD202604290003EE', 2, 178.00, -1, NULL, '小红', '13800000002', '杭州市 西湖区 文三路 8 号', '2026-04-29 14:02:00');

-- ----------------------------
-- Table structure for t_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(255) NOT NULL COMMENT '商品名称',
  `product_image` varchar(512) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10, 2) NOT NULL COMMENT '商品单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品项表';

INSERT INTO `t_order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`) VALUES
(1, 1, 1, '智能手机 Pro Max', '', 5999.00, 1),
(2, 1, 2, '无线蓝牙耳机', '', 399.00, 1),
(3, 2, 4, '空气循环扇', '', 299.00, 1),
(4, 3, 2, '无线蓝牙耳机', '', 399.00, 1),
(5, 3, 6, '混合坚果礼盒', '', 128.00, 2),
(6, 4, 1, '智能手机 Pro Max', '', 5999.00, 1),
(7, 5, 6, '混合坚果礼盒', '', 128.00, 1),
(8, 5, 5, '保湿洁面乳', '', 89.00, 1);

UPDATE `t_product` p
LEFT JOIN (
  SELECT product_id, SUM(quantity) AS total_qty
  FROM t_order_item
  GROUP BY product_id
) s ON p.id = s.product_id
SET p.sales_count = IFNULL(s.total_qty, 0);

-- ===============================
-- 补充测试数据：商品图片
-- ===============================
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod1/400/400' WHERE id = 1;
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod2/400/400' WHERE id = 2;
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod3/400/400' WHERE id = 3;
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod4/400/400' WHERE id = 4;
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod5/400/400' WHERE id = 5;
UPDATE t_product SET main_image = 'https://picsum.photos/seed/prod6/400/400' WHERE id = 6;

-- ===============================
-- 补充测试数据：收货地址
-- ===============================
INSERT INTO t_user_address (user_id, receiver_name, phone, province, city, district, detail_address, label, is_default) VALUES
(1, '小明', '13800000001', '上海市', '上海市', '浦东新区', '张江路100号', '家', 1),
(1, '小明公司', '13800000001', '上海市', '上海市', '徐汇区', '漕溪北路88号', '公司', 0),
(2, '小红', '13800000002', '浙江省', '杭州市', '西湖区', '文三路8号', '家', 1),
(3, '小李', '13800000003', '北京市', '北京市', '海淀区', '中关村大街66号', '学校', 1);

-- ===============================
-- 补充测试数据：商品收藏
-- ===============================
INSERT INTO t_product_favorite (user_id, product_id) VALUES
(1, 1), (1, 3), (2, 2), (3, 5);

-- ===============================
-- 补充测试数据：前台注册用户（密码 123456）
-- ===============================
-- 手机号 13800000100 / 密码 MD5=e10adc3949ba59abbe56e057f20f883e（123456）
INSERT INTO t_user (openid, nickname, avatar_url, phone, email, password, type)
VALUES ('test_reg_001', '测试用户', '', '13800000100', '', 'e10adc3949ba59abbe56e057f20f883e', 2);

SET FOREIGN_KEY_CHECKS = 1;
