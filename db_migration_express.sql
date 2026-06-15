-- ============================================
-- 订单表增加物流字段（快递公司 + 单号）
-- 用于物流追踪功能
-- ============================================
ALTER TABLE `t_order`
  ADD COLUMN `express_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司' AFTER `receiver_address`,
  ADD COLUMN `express_no`     VARCHAR(50) DEFAULT NULL COMMENT '快递单号' AFTER `express_company`;
