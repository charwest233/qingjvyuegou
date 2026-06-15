-- ============================================
-- 优惠券与每日抽奖系统
-- ============================================

-- 优惠券模板表
CREATE TABLE IF NOT EXISTS `t_coupon` (
  `id`          BIGINT          NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100)    NOT NULL COMMENT '优惠券名称',
  `description` VARCHAR(500)    DEFAULT NULL COMMENT '描述',
  `type`        TINYINT         NOT NULL DEFAULT 1 COMMENT '类型:1-满减券,2-折扣券',
  `value`       DECIMAL(10,2)   NOT NULL COMMENT '面值（满减金额/折扣率如0.9）',
  `min_amount`  DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '最低使用金额',
  `valid_days`  INT             NOT NULL DEFAULT 7 COMMENT '有效期天数（从领取算起）',
  `tier`        TINYINT         NOT NULL COMMENT '档位:1-5',
  `weight`      INT             NOT NULL DEFAULT 100 COMMENT '抽奖权重（越大越容易抽到）',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态:0-下架,1-上架',
  `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tier` (`tier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `t_user_coupon` (
  `id`          BIGINT          NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT          NOT NULL COMMENT '用户ID',
  `coupon_id`   BIGINT          NOT NULL COMMENT '优惠券模板ID',
  `coupon_no`   VARCHAR(50)     NOT NULL COMMENT '优惠券编号',
  `name`        VARCHAR(100)    NOT NULL COMMENT '优惠券名称（快照）',
  `type`        TINYINT         NOT NULL DEFAULT 1 COMMENT '类型（快照）',
  `value`       DECIMAL(10,2)   NOT NULL COMMENT '面值（快照）',
  `min_amount`  DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '最低使用金额（快照）',
  `status`      TINYINT         NOT NULL DEFAULT 0 COMMENT '状态:0-未使用,1-已使用,2-已过期',
  `order_id`    BIGINT          DEFAULT NULL COMMENT '使用的订单ID',
  `expires_at`  DATETIME        NOT NULL COMMENT '过期时间',
  `used_at`     DATETIME        DEFAULT NULL COMMENT '使用时间',
  `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_no` (`coupon_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- 每日抽奖记录表
CREATE TABLE IF NOT EXISTS `t_coupon_draw` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `draw_date`   DATE        NOT NULL COMMENT '抽奖日期',
  `draw_count`  INT         NOT NULL DEFAULT 0 COMMENT '已抽次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `draw_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日抽奖记录';

-- 初始化5档优惠券模板
INSERT IGNORE INTO `t_coupon` (`id`, `name`, `description`, `type`, `value`, `min_amount`, `valid_days`, `tier`, `weight`) VALUES
(1, '满10减2', '满10元减2元', 1, 2.00, 10.00, 7, 1, 350),
(2, '满30减5', '满30元减5元', 1, 5.00, 30.00, 15, 2, 280),
(3, '满50减10', '满50元减10元', 1, 10.00, 50.00, 15, 3, 200),
(4, '满100减25', '满100元减25元', 1, 25.00, 100.00, 30, 4, 120),
(5, '满200减60', '满200元减60元', 1, 60.00, 200.00, 30, 5, 50);
